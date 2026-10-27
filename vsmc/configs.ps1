Import-Module "$PSScriptRoot/import-env.psm1"

Import-Env

$configDir  = Join-Path -Path $PSScriptRoot -ChildPath "config"
$privateKey = Join-Path -Path $configDir    -ChildPath "id_ed25519"
$publicKey  = Join-Path -Path $configDir    -ChildPath "id_ed25519.pub"
$knownHosts = Join-Path -Path $configDir    -ChildPath "known_hosts"

$sshBase = @(
    "-i", $privateKey,
    "-o", "BatchMode=yes",
    "-o", "StrictHostKeyChecking=yes",
    "-o", "UserKnownHostsFile=$knownHosts",
    "${env:xmcs_u}@${env:unit01_host}"
)

$Units = @{
    "unit01" = @{
        Host = "${env:unit01_host}"
        Folder = @("amhs_emulator", "xmcs_server", "xmcs-monitor")
    }

    "unit02" = @{
        Host = "${env:unit02_host}"
        Folder = @("xmcs_server", "xmcs-monitor")
    }
}

$Users = (Get-ChildItem Env: | Where-Object Name -match '_u$')
