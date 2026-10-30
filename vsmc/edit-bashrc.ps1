param (
    [Parameter(Mandatory = $true)]
    [string]$Unit,

    [Parameter(Mandatory = $true)]
    [string]$Role
)

. "$PSScriptRoot/configs.ps1"

if ($Global:AppConfig.unit.units[$Unit].status -eq "offline") {
    exit 0
}

$xbashLocal = Join-Path -Path $PSScriptRoot -ChildPath "bash/xmcs_bashrc.sh"
$xbashRemote = '~/xmcs_bashrc.sh'
$targetUserIp = "$($Global:AppConfig.unit.units[$Unit].users[$Role].username)@$($Global:AppConfig.unit.units[$Unit].ip)"

$scpArgs = $sshBase + @(
    "-q",
    $xbashLocal,
    "$($targetUserIp):${xbashRemote}"
)

& scp @scpArgs

# 1. Define the block content to check and append
$startMarker = "# {{ xMCS specific"
$endMarker   = "# }} xMCS specific"

$blockContent = @"
$startMarker
if [ "`$USER" = "$User" ]; then
    source "`$HOME/xmcs_bashrc.sh"
fi
$endMarker
"@

ssh @sshBase $targetUserIp "grep -qF '$startMarker' ~/.bashrc"

if ($LASTEXITCODE -ne 0) {
    Write-Host "Appending to .bashrc" -ForegroundColor Gray
    $blockContent -replace "`r`n", "`n" | ssh @sshBase "$targetUserIp" "tr -d '\r' >> ~/.bashrc"
}
