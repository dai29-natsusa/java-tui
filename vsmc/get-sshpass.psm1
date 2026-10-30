$ErrorActionPreference = 'Stop'

# Repository
$Repo = "chuccp/win-sshpass"

# Working directory
$DownloadDir = Join-Path $PSScriptRoot "tools"
New-Item -ItemType Directory -Path $DownloadDir -Force | Out-Null

# Get latest release
$Release = Invoke-RestMethod `
    -Uri "https://api.github.com/repos/$Repo/releases/latest"

# Find amd64 zip asset
$Asset = $Release.assets |
    Where-Object {
        $_.name -match '^win-sshpass-.*-amd64\.zip$'
    } |
    Select-Object -First 1

if (-not $Asset) {
    Write-Error "Could not find amd64 zip asset."
    exit 1
}

# Download zip
$ZipFile = Join-Path $DownloadDir $Asset.name

Write-Host "Downloading $($Asset.name)..."

Invoke-WebRequest `
    -Uri $Asset.browser_download_url `
    -OutFile $ZipFile

# Extract
$ExtractDir = Join-Path $DownloadDir "win-sshpass"

if (Test-Path $ExtractDir) {
    Remove-Item $ExtractDir -Recurse -Force
}

Expand-Archive `
    -Path $ZipFile `
    -DestinationPath $ExtractDir `
    -Force

# Locate exe
$Exe = Get-ChildItem `
    -Path $ExtractDir `
    -Filter "*.exe" `
    -Recurse |
    Select-Object -First 1

if (-not $Exe) {
    Write-Error "No executable found after extraction."
    exit 1
}

# Rename to sshp.exe
$FinalExe = Join-Path $PSScriptRoot "sshp.exe"

if (Test-Path $FinalExe) {
    Remove-Item $FinalExe -Force
}

Copy-Item `
    -Path $Exe.FullName `
    -Destination $FinalExe `
    -Force

Write-Host "Created: $FinalExe"

# Optional cleanup
Remove-Item $ZipFile -Force

Write-Host "Completed."
