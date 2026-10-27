function New-SshKey {
    [CmdletBinding()]
    param (
        [switch]$Force
    )

    # Define paths relative to the current script
    $configDir  = Join-Path -Path $PSScriptRoot -ChildPath "config"
    $privateKey = Join-Path -Path $configDir    -ChildPath "id_ed25519"
    $publicKey  = Join-Path -Path $configDir    -ChildPath "id_ed25519.pub"

    # Ensure config directory exists
    if (-not (Test-Path $configDir)) {
        New-Item -Path $configDir -ItemType Directory -Force | Out-Null
        Write-Host "Created directory: $configDir" -ForegroundColor Cyan
    }

    $keyExists =
        (Test-Path $privateKey) -and
        (Test-Path $publicKey)

    # Force mode
    if ($Force -and $keyExists) {
        Remove-Item $privateKey -Force -ErrorAction SilentlyContinue
        Remove-Item $publicKey  -Force -ErrorAction SilentlyContinue

        $keyExists = $false
    }

    # Skip generation if keys already exist
    if ($keyExists) {
        Write-Host "SSH key pair already exists" -ForegroundColor Green
        return
    }

    Write-Host "Generating new ed25519 SSH key pair..." -ForegroundColor Yellow

    $comment = "$(Get-Date -Format 'yyyyMMddHHmmss')-daifuku"

    $sshKeyArgs = @(
        "-t", "ed25519",
        "-f", $privateKey,
        "-N", "",
        "-C", $comment,
        "-q"
    )

    $process = Start-Process `
        -FilePath "ssh-keygen" `
        -ArgumentList $sshKeyArgs `
        -NoNewWindow `
        -Wait `
        -PassThru

    if ($process.ExitCode -eq 0) {
        Write-Host "Successfully generated SSH key pair." -ForegroundColor Green
        Write-Host "Comment     : $comment"
    } else {
        throw "ssh-keygen failed with exit code $($process.ExitCode)"
    }
}

Export-ModuleMember -Function New-SshKey
