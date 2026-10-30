# Import Modules
Import-Module "$PSScriptRoot/ssh-key.psm1"
Import-Module "$PSScriptRoot/import-env.psm1"
Import-Module "$PSScriptRoot/import-yaml.psm1"
Import-Module "$PSScriptRoot/test-connectivity.psm1"
Import-Module "$PSScriptRoot/test-artifacts.psm1"

. "$PSScriptRoot/configs.ps1"

# Step 0 - check all required file for running
$sshpFile = "$PSScriptRoot/sshp.exe"
if (-not (Test-Path $sshpFile)) {
    Write-Error "Exit Error: sshp.exe is not found - cannot proceed further."
    Write-Error "Download ZIP file at https://github.com/chuccp/win-sshpass/releases"
    Write-Error "After downloaded, extract it and rename it to 'sshp.exe'"
    exit 1
}
if (-not (Test-Artifacts)) {
    Write-Error "Exit Error: No XMCS.zip file or amhs_emulator, xmcs_server, xmcs-monitor folders are missing."
    exit 1
}
# Step 1 - always make newline of every files to be Linux newline `\r`
& "$PSScriptRoot/line-end.ps1" -Path "$PSScriptRoot/bash" -Quiet
& "$PSScriptRoot/line-end.ps1" -Path "$PSScriptRoot/amhs_emulator" -Quiet
& "$PSScriptRoot/line-end.ps1" -Path "$PSScriptRoot/xmcs_server" -Quiet
& "$PSScriptRoot/line-end.ps1" -Path "$PSScriptRoot/xmcs-monitor" -Quiet
# Step 2 - create ssh 'ed25519' keys file
New-SshKey
# Step 3 - check existing remote user/pass/host-ip
Import-Env
$Global:AppConfig = Import-Yaml -DirectoryPath $configDir
Test-UnitConnectivity -Config $Global:AppConfig
# Step 4 - register public key to remote
& "$PSScriptRoot/ssh-register.ps1" -Unit unit01
& "$PSScriptRoot/ssh-register.ps1" -Unit unit02
# Step 5 modify RUNNER user's bashrc
& "$PSScriptRoot/edit-bashrc.ps1" -Unit unit01 -Role runner
& "$PSScriptRoot/edit-bashrc.ps1" -Unit unit02 -Role runner
