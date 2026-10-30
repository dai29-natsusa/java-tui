param (
    [Parameter(Mandatory = $true)]
    [string]$Unit
)

. "$PSScriptRoot/configs.ps1"

$ServerIp   = $Global:AppConfig.unit.units[$Unit].ip

$HostExists = $false

if (Test-Path $knownHosts)
{
    $HostExists = Get-Content $knownHosts |
            Select-String -SimpleMatch $ServerIp -Quiet
}

if (-not $HostExists)
{
    ssh-keyscan $ServerIp 2>$null |
        Out-File `
            -Append `
            -Encoding ascii `
            $knownHosts
}

if ($Global:AppConfig.unit.units[$Unit].status -eq "offline") {
    exit 0
}

$PubKey = (Get-Content $publicKey -Raw).Trim()
$PubKeyEscaped = $PubKey.Replace("'", "'\''")

$RemoteCommand = "mkdir -p ~/.ssh && " +
"chmod 700 ~/.ssh && " +
"touch ~/.ssh/authorized_keys && " +
"chmod 600 ~/.ssh/authorized_keys && " +
"grep -qxF '$PubKeyEscaped' ~/.ssh/authorized_keys || echo '$PubKeyEscaped' >> ~/.ssh/authorized_keys"

$SshPass  = "$PSScriptRoot/sshp.exe"

$Users = $Global:AppConfig.unit.units[$Unit].users
foreach ($role in $Users.Keys) {
    $user = $Users[$role]
    Write-Host "$($user.username)"
    $password = $user.password
    & $SshPass `
        -p $password `
        ssh `
        -o StrictHostKeyChecking=no `
        -o UserKnownHostsFile="$knownHosts" `
        "$($user.username)@$ServerIp" `
        $RemoteCommand

    if ($LASTEXITCODE -eq 0) {
        ssh `
            -i $privateKey `
            -o BatchMode=yes `
            -o StrictHostKeyChecking=yes `
            -o UserKnownHostsFile="$knownHosts" `
            "$($user.username)@$ServerIp" `
            "echo $($user.username)_SSH_KEY_AUTH_OK" | Tee-Object "$configDir/$($user.username).txt"
    } else {
        Write-Host "User: [$($user.username)] doesn't exist" -ForegroundColor Red
    }
}
