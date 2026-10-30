function Sync-Instances {
    [CmdletBinding()]
    param(
        [Parameter(Mandatory = $true)]
        [string]$Unit,

        [Parameter(Mandatory = $false)]
        [string]$LocalInstancesPath = "$PSScriptRoot",

        [Parameter(Mandatory = $false)]
        [switch]$Force
    )

    . "$PSScriptRoot/configs.ps1"

    $UnitConfig = $Global:AppConfig.unit.units[$Unit]
    $workdir = $UnitConfig.workdir
    Write-Host "Synchronizing instances for unit at workdir: $workdir" -ForegroundColor Cyan

    $targetUserIp = "$($UnitConfig.users[$Role].username)@$($UnitConfig.ip)"
    # Ensure the parent workdir exists remotely
    $mkdirArgs = $sshBase + @("$targetUserIp", "mkdir -p $workdir")
    & ssh @mkdirArgs | Out-Null

    # 1. Get the list of instance names defined in the YAML configuration
    $configInstanceNames = @()
    if ($UnitConfig.instances) {
        foreach ($inst in $UnitConfig.instances) {
            $configInstanceNames += $inst.name
        }
    }

    # 2. Query the remote host to find what instance folders currently exist inside $workdir
    # We list only directories inside the workdir to compare
    $listRemoteDirsScript = @"
if [ -d "$workdir" ]; then
    find "$workdir" -mindepth 1 -maxdepth 1 -type d -exec basename {} \;
fi
"@

    $listArgs = $sshBase + @("$targetUserIp", $listRemoteDirsScript)
    $remoteDirsOutput = & ssh @listArgs

    # Parse remote directories into an array (handling potential whitespace/newlines)
    $remoteInstanceNames = @()
    if ($LASTEXITCODE -eq 0 -and -not [string]::IsNullOrWhiteSpace($remoteDirsOutput)) {
        $remoteInstanceNames = $remoteDirsOutput -split "`r?`n" | Where-Object { -not [string]::IsNullOrWhiteSpace($_) }
    }

    Write-Host "  -> Configured instances: $($configInstanceNames -join ', ')" -ForegroundColor DarkGray
    Write-Host "  -> Remote existing dirs:  $($remoteInstanceNames -join ', ')" -ForegroundColor DarkGray

    # ==========================================
    # 3. REMOVE EXTRA INSTANCES (Remote has it, but Config does NOT)
    # ==========================================
    foreach ($remoteDir in $remoteInstanceNames) {
        # Check if this remote directory is NOT in our YAML configuration
        if ($configInstanceNames -notcontains $remoteDir) {
            Write-Host "  -> Extra instance found on remote ('$remoteDir') which is missing from YAML config. Removing..." -ForegroundColor Yellow

            $removePath = "$workdir/$remoteDir"
            $removeScript = "rm -rf '$removePath'"
            $removeArgs = $sshBase + @("$targetUserIp", $removeScript)

            & ssh @removeArgs
            if ($LASTEXITCODE -eq 0) {
                Write-Host "  -> Successfully removed extra instance folder: $remoteDir" -ForegroundColor Green
            } else {
                Write-Error "  -> Failed to remove extra instance folder: $remoteDir"
            }
        }
    }

    # ==========================================
    # 4. UPLOAD MISSING INSTANCES (Config has it, but Remote does NOT)
    # ==========================================
    foreach ($inst in $UnitConfig.instances) {
        $instName = $inst.name
        $localInstPath = Join-Path $LocalInstancesPath $instName

        # Check if it's missing on the remote side
        if ($remoteInstanceNames -notcontains $instName -or $Force) {
            Write-Host "  -> Instance '$instName' is missing on remote. Uploading via scp..." -ForegroundColor Yellow

            if (-not (Test-Path -Path $localInstPath)) {
                Write-Error "  -> Local instance source folder not found at: $localInstPath"
                continue
            }

            # Upload folder recursively using scp
            $scpArgs = $sshBase + @("-r", $localInstPath, "$($targetUserIp):$workdir/")
            & scp @scpArgs

            if ($LASTEXITCODE -eq 0) {
                Write-Host "  -> Successfully uploaded instance folder '$instName'." -ForegroundColor Green
            } else {
                Write-Error "  -> Failed to upload instance folder '$instName'."
            }

            $chmodScript = @"
if [ -d "$workdir" ]; then
    echo "Setting executable permissions for .sh files in $workdir..."
    find "$workdir" -type f -name "*.sh" -exec chmod 760 {} +
    echo "Permissions updated successfully."
fi
"@
            $chmodArgs = $SshBaseOptions + @("$targetUserIp", $chmodScript)
            & ssh @chmodArgs

            if ($LASTEXITCODE -eq 0) {
                Write-Host "All shell scripts are now executable." -ForegroundColor Green
            } else {
                Write-Error "Failed to update script permissions."
            }
        } else {
            Write-Host "  -> Instance '$instName' already exists on remote. (In sync)" -ForegroundColor Green
        }
    }

    Write-Host "Instance synchronization completed successfully." -ForegroundColor Green
}
