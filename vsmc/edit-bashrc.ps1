. "$PSScriptRoot/configs.ps1"

$appConfig = $Global:AppConfig

foreach ($unitKey in $appConfig.unit.units.Keys) {
    if ($appConfig.unit.units[$unitKey].status -eq "offline") {
        Write-Host "$unitKey is OFFLINE" -ForegroundColor DarkYellow
        continue
    }

    $xbashLocal = Join-Path -Path $PSScriptRoot -ChildPath "bash/xmcs_bashrc.sh"
    $xbashRemote = '~/xmcs_bashrc.sh'
    $username = $appConfig.unit.units[$unitKey].users.runner.username
    $targetUserIp = "$($username)@$($appConfig.unit.units[$unitKey].ip)"

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
if [ "`$USER" = "$username" ]; then
    source "`$HOME/xmcs_bashrc.sh"
fi
$endMarker
"@

    ssh @sshBase $targetUserIp "grep -qF '$startMarker' ~/.bashrc"

    if ($LASTEXITCODE -ne 0) {
        Write-Host "Appending to .bashrc" -ForegroundColor Gray
        $blockContent -replace "`r`n", "`n" | ssh @sshBase "$targetUserIp" "tr -d '\r' >> ~/.bashrc"
    }
}
