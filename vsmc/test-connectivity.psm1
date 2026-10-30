function Test-UnitConnectivity {
    [CmdletBinding()]
    param(
        [Parameter(Mandatory = $true)]
        [object]$Config
    )

    Write-Host "Checking connectivity for all remote units..." -ForegroundColor Cyan

    foreach ($unitKey in $Config.unit.units.Keys) {
        $unitObj = $Config.unit.units[$unitKey]

        if ($unitObj -and $unitObj['ip']) {
            $targetIp = $unitObj['ip']
            Write-Host "  -> [$unitKey] ($targetIp)..." -NoNewline

            $isOnline = $false
            try {
                $null = ($netTest = Test-NetConnection -ComputerName $targetIp -Port 22 -InformationLevel Quiet -WarningAction SilentlyContinue -ErrorAction Stop)
                if ($netTest) { $isOnline = $true }
            } catch {
                $isOnline = $false
            }

            $statusValue = if ($isOnline) { 'online' } else { 'offline' }

            if ($unitObj -is [System.Collections.IDictionary]) {
                $unitObj['status'] = $statusValue
            } else {
                $unitObj | Add-Member -NotePropertyName 'status' -NotePropertyValue $statusValue -Force
            }

            if ($isOnline) {
                Write-Host " [ONLINE]" -ForegroundColor Green
            } else {
                Write-Host " [OFFLINE]" -ForegroundColor Red
            }
        }
    }
}
Export-ModuleMember -Function Test-UnitConnectivity
