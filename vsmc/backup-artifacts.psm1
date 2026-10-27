function Backup-Artifacts {
    [CmdletBinding()]
    param (
        [string]$Path = "$PSScriptRoot/backup"
    )

    # Resolve full target path
    $targetPath = [System.IO.Path]::GetFullPath($Path)

    # Define target paths
    $folderO1 = Join-Path $targetPath "amhs_emulator"
    $backupO1 = Join-Path $targetPath "bak/amhs_emulator"
    $folderO2 = Join-Path $targetPath "xmcs_server"
    $backupO2 = Join-Path $targetPath "bak/xmcs_server"
    $folderO3 = Join-Path $targetPath "xmcs-monitor"
    $backupO3 = Join-Path $targetPath "bak/xmcs-monitor"

    # Check existence
    Copy-Item -Force -Recurse $folderO1 $backupO1
    Copy-Item -Force -Recurse $folderO2 $backupO2
    Copy-Item -Force -Recurse $folderO3 $backupO3
}
Export-ModuleMember -Function Backup-Artifacts
