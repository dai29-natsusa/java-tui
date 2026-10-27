param (
    [Parameter(Mandatory = $true)]
    [string]$Path,

    [switch]$Quiet
)

# Ensure the path exists
if (-not (Test-Path -Path $Path)) {
    if ($Quiet) {
        Write-Error "The specified directory does not exist: $Path"
    }
    exit 1
}

# Find all matching files recursively
$files = Get-ChildItem -Path $Path -Recurse -File -Include "*.sh", "*.properties", "*.ini", "*.json", "*.xml", "*.sql", "*.command", "*.yaml"

if ($Quiet) {
    Write-Host "Found $($files.Count) file(s) matching criteria." -ForegroundColor Cyan
}

foreach ($file in $files) {
    # Read the file content as a raw string to preserve line breaks
    $content = Get-Content -Path $file.FullName -Raw

    # Check if the file contains CRLF (`r`n)
    if ($content -match "`r`n") {
        # Replace CRLF with LF
        $newContent = $content -replace "`r`n", "`n"

        # Write back to file with UTF-8 encoding without BOM using standard .NET I/O
        [System.IO.File]::WriteAllText($file.FullName, $newContent, (New-Object System.Text.UTF8Encoding($false)))
        if ($Quiet) {
            Write-Host "Converted: $($file.FullName)" -ForegroundColor Green
        }
    } else {
        if ($Quiet) {
            Write-Host "Skipped (already LF): $($file.FullName)" -ForegroundColor Gray
        }
    }
}

if ($Quiet) {
    Write-Host "Processing complete." -ForegroundColor Cyan
}
