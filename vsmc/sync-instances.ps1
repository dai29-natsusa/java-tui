param(
    [Parameter(Mandatory = $false)]
    [string]$Role = "runner",

    [Parameter(Mandatory = $false)]
    [string]$Path = "$PSScriptRoot",

    [Parameter(Mandatory = $false)]
    [switch]$Force
)

. "$PSScriptRoot/configs.ps1"

$appConfig = $Global:AppConfig

foreach ($unitKey in $appConfig.unit.units.Keys) {
    $UnitConfig = $appConfig.unit.units[$unitKey]

    if ($UnitConfig.status -eq "offline") {
        Write-Host "$unitKey is OFFLINE" -ForegroundColor DarkYellow
        continue
    }

    $userMap = $UnitConfig.users
    if ($userMap -and -not $userMap.ContainsKey($Role)) {
        continue
    }

    $workdir = $UnitConfig.users[$Role].workdir
    Write-Host "Synchronizing to workdir: $workdir" -ForegroundColor Cyan

    $targetUserIp = "$($UnitConfig.users[$Role].username)@$($UnitConfig.ip)"
    # Ensure the parent workdir exists remotely
    $mkdirArgs = $sshBase + @("$targetUserIp", "mkdir -p $workdir")
    & ssh @mkdirArgs | Out-Null

    # 1. Get the list of instance names defined in the YAML configuration
    $configInstanceNames = @()
    if ($UnitConfig.users[$Role].instances) {
        foreach ($inst in $UnitConfig.users[$Role].instances) {
            $configInstanceNames += $inst.name
        }
    }

    # 2. Query the remote host to find what instance folders currently exist inside $workdir
    $listRemoteDirsScript = @"
if [ -d "$workdir" ]; then
    find "$workdir" -mindepth 1 -maxdepth 1 -type d -exec basename {} \;;
fi
"@

    $listArgs = $sshBase + @("$targetUserIp", $listRemoteDirsScript)
    $remoteDirsOutput = & ssh @listArgs

    # Parse remote directories into an array
    $remoteInstanceNames = @()
    if ($LASTEXITCODE -eq 0 -and -not [string]::IsNullOrWhiteSpace($remoteDirsOutput)) {
        $remoteInstanceNames = $remoteDirsOutput -split "`r?`n" | Where-Object { -not [string]::IsNullOrWhiteSpace($_) }
    }

    Write-Host "  -> Configured instances: $($configInstanceNames -join ', ')" -ForegroundColor DarkGray
    Write-Host "  -> Remote existing dirs:  $($remoteInstanceNames -join ', ')" -ForegroundColor DarkGray

    # 3. REMOVE EXTRA INSTANCES (Remote has it, but Config does NOT)
    foreach ($remoteDir in $remoteInstanceNames) {
        if ($configInstanceNames -notcontains $remoteDir) { 
            Write-Host "  -> Extra unequals instance found on remote ('$remoteDir'). Removing..." -ForegroundColor Yellow

            $removePath = "$workdir/$remoteDir"
            $removeScript = "rm -rf '$removePath'"
            $removeArgs = $sshBase + @("$targetUserIp", $removeScript)

            & ssh @removeArgs
            if ($LASTEXITCODE -eq 0) {
                Write-Host "  -> Successfully removed extra instance folder: $remoteDir" -ForegroundColor Green
            } else {
                Write-Host "  -> Failed to remove extra instance folder: $remoteDir" -ForegroundColor Red
            }
        }
    }

    # 4. UPLOAD INSTANCES (Filtered: Excluding .log and .gz)
    foreach ($inst in $UnitConfig.users[$Role].instances) {
        $instName = $inst.name
        $localInstPath = Join-Path $Path $instName

        if ($remoteInstanceNames -notcontains $instName -or $Force) {
            if (-not (Test-Path -Path $localInstPath)) {
                Write-Warning "  -> Local instance source folder not found at: $localInstPath"
                continue
            }

            # --- LOCAL STAGING & FILTERING ---
            # Create a temporary staging directory to filter out unwanted extensions (*.log, *.gz)
            $stagingBase = Join-Path $env:TEMP "AppSyncStaging"
            $stagingInstPath = Join-Path $stagingBase $instName

            if (Test-Path $stagingInstPath) { 
                Remove-Item -Path $stagingInstPath -Recurse -Force -ErrorAction SilentlyContinue 
            }

            # Use robocopy to mirror local files to staging while excluding logs and archives
            # (/E = copy subdirectories including empty ones, /XF = exclude files)
            robocopy $localInstPath $stagingInstPath /E /XF *.log *.gz /NJH /NJS /NC /NS | Out-Null
            # (Robocopy exit codes 0-7 indicate success or normal file copy states)
            # ---------------------------------

            # Upload the filtered staging folder via scp
            $scpArgs = $sshBase + @("-r", $stagingInstPath, "$($targetUserIp):$workdir/")
            & scp @scpArgs

            if ($LASTEXITCODE -eq 0) {
                Write-Host "  -> Successfully uploaded instances '$instName'." -ForegroundColor Green
            } else {
                Write-Host "  -> Failed to upload instance folder '$instName'." -ForegroundColor Red
            }

            # Clean up local staging temp folder immediately after scp completes
            Remove-Item -Path $stagingInstPath -Recurse -Force -ErrorAction SilentlyContinue

            $chmodScript = @"
if [ -d "$workdir" ]; then
    echo "Setting executable permissions for .sh files"
    find "$workdir" -type f -name "*.sh" -exec chmod 760 {} +
    echo "Permissions updated successfully."
fi
"@
            $chmodArgs = $SshBaseOptions + @("$targetUserIp", $chmodScript)
            & ssh @chmodArgs

            if ($LASTEXITCODE -eq 0) {
                Write-Host "All script permissions has been updated." -ForegroundColor Green
            } else {
                Write-Host "Failed to update script permissions." -ForegroundColor Red
            }
        } else {
            Write-Host "  -> Instance '$instName' already exists on remote. (In sync)" -ForegroundColor Green
        }
    }

    Write-Host "Instance synchronization completed successfully." -ForegroundColor Green
}
