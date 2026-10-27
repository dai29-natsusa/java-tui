param (
    [Parameter(Mandatory = $true)]
    [string]$Unit
)

. "$PSScriptRoot/configs.ps1"

$ServerIp   = $Units[$Unit].Host

$HostExists = $false

if (Test-Path $knownHosts)
{
    $HostExists = Get-Content $KnownHosts |
            Select-String -SimpleMatch $ServerIp -Quiet
}

if (-not $HostExists)
{
    ssh-keyscan $ServerIp 2>$null |
        Out-File `
            -Append `
            -Encoding ascii `
            $KnownHosts
}

$PubKey = (Get-Content $publicKey -Raw).Trim()
$PubKeyEscaped = $PubKey.Replace("'", "'\''")

$RemoteCommand = "mkdir -p ~/.ssh && " +
"chmod 700 ~/.ssh && " +
"touch ~/.ssh/authorized_keys && " +
"chmod 600 ~/.ssh/authorized_keys && " +
"grep -qxF '$PubKeyEscaped' ~/.ssh/authorized_keys || echo '$PubKeyEscaped' >> ~/.ssh/authorized_keys"

$SshPass  = "$PSScriptRoot/sshp.exe"

foreach ($user in $Users) {
    Write-Host "$($user.Value)"
    $password = Get-Content "Env:\$($user.Value)_p"
    & $SshPass `
        -p $password `
        ssh `
        -o StrictHostKeyChecking=no `
        -o UserKnownHostsFile="$KnownHosts" `
        "$($user.Value)@$ServerIp" `
        $RemoteCommand

    ssh `
        -i $privateKey `
        -o BatchMode=yes `
        -o StrictHostKeyChecking=yes `
        -o UserKnownHostsFile="$KnownHosts" `
        "$($user.Value)@$ServerIp" `
        "echo $($user.Value)_SSH_KEY_AUTH_OK" | Tee-Object "$KeyDir/$($user.Value).txt"
}
