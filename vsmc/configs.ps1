Import-Module "$PSScriptRoot/import-env.psm1"
Import-Module "$PSScriptRoot/import-yaml.psm1"

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

$config = Import-Yaml -DirectoryPath $configDir
