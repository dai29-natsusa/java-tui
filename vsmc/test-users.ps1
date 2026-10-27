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

$SshPass  = "$PSScriptRoot/sshp.exe"

foreach ($user in $Users) {
    Write-Host "$($user.Value)"
    $password = Get-Content "Env:\$($user.Value)_p"
    $remoteCommand = "getend passwd $($user.Value)"
    & $SshPass `
        -p $password `
        ssh `
        -o StrictHostKeyChecking=no `
        -o UserKnownHostsFile="$KnownHosts" `
        "$($user.Value)@$ServerIp" `
        $remoteCommand 2>$null
}
