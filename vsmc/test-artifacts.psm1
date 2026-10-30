function Test-Artifacts {
    [CmdletBinding()]
    param (
        [string]$Path = $PSScriptRoot
    )

    # Resolve full target path
    $targetPath = [System.IO.Path]::GetFullPath($Path)

    # Define target paths
    $zipFile  = Join-Path $targetPath "XMCS.zip"
    $folderO1 = Join-Path $targetPath "amhs_emulator"
    $folderO2 = Join-Path $targetPath "xmcs_server"
    $folderO3 = Join-Path $targetPath "xmcs-monitor"

    # Check existence
    $hasZip = Test-Path -Path $zipFile -PathType Leaf
    $hasO1  = Test-Path -Path $folderO1 -PathType Container
    $hasO2  = Test-Path -Path $folderO2 -PathType Container
    $hasO3  = Test-Path -Path $folderO3 -PathType Container

    # Condition evaluation
    $allFoldersExist = $hasO1 -and $hasO2 -and $hasO3
    $isOk = $hasZip -or $allFoldersExist

    # Display findings
    $foundTxt = "`e[32mFOUND`e[0m"
    $notFoundTxt = "`e[31mNOT FOUND`e[0m"
    Write-Host "`n--- Status Check ---" -ForegroundColor Yellow
    Write-Host "  - XMCS.zip : $(if ($hasZip) { $foundTxt } else { $notFoundTxt })"
    Write-Host "  - amhs_emulator folder: $(if ($hasO1) { $foundTxt } else { $notFoundTxt })"
    Write-Host "  - xmcs_server folder: $(if ($hasO2) { $foundTxt } else { $notFoundTxt })"
    Write-Host "  - xmcs-monitor folder: $(if ($hasO3) { $foundTxt } else { $notFoundTxt })"
    Write-Host ""

    if ($hasZip) {
        Expand-Archive -Path "$zipFile" -DestinationPath "$PSScriptRoot" -Force
    }

    return [bool]$isOk
}
Export-ModuleMember -Function Test-Artifacts
