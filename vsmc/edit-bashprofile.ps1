. "./configs.ps1"

$xBash = Join-Path -Path $PSScriptRoot -ChildPath "bash/xmcs_bash_profile.sh"

& ssh @sshBase "grep -q '# xMCS specific' ~/.bash_profile"

if ($LASTEXITCODE -ne 0) {
    # In case nothing configured in `.bash_profile`
    "`n$(Get-Content -Path $xBash -Raw)" | & ssh @sshBase "cat >> ~/.bash_profile"
}
