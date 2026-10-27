function Import-Env {
    param(
        [string]$Path = ".env"
    )

    if (Test-Path $Path) {
        # Case 1: .env file exists -> Load from file
        Get-Content $Path | Where-Object { $_ -match '=' -and $_ -notmatch '^#' } | ForEach-Object {
            $name, $value = $_.Split('=', 2)
            $name = $name.Trim()
            $value = $value.Trim().Trim('"').Trim("'")
            Set-Content "env:\$name" $value
        }
    } else {
        # Case 2: No .env file -> Collect input manually, load them, and save to file
        Write-Host "No $Path file found." -ForegroundColor Yellow
        Write-Host "Please enter your environment variables in 'user=password' format (e.g., xmcs=xmcs123)." -ForegroundColor Cyan
        Write-Host "Type 'q' and press Enter to save and exit." -ForegroundColor DarkGray

        # Array to temporarily hold lines for the new .env file
        $envLines = [System.Collections.Generic.List[string]]::new()

        while ($true) {
            $userInput = Read-Host "Enter user/pass"

            # Check if user wants to quit
            if ($userInput.Trim() -eq 'q') { break }

            # Validate format contains '='
            if ($userInput -match '=') {
                $name, $value = $userInput.Split('=', 2)
                $name = $name.Trim()
                $value = $value.Trim()

                # Load into current session immediately
                Set-Content "env:\$name" ($value.Trim('"').Trim("'"))

                # Add to our list to save later
                $envLines.Add("$name=$value")
            } else {
                Write-Host "  Invalid format. Please use 'key=value' or type 'q' to quit." -ForegroundColor Red
            }
        }

        # Save to .env file if the user entered anything
        if ($envLines.Count -gt 0) {
            $envLines | Out-File -FilePath $Path -Encoding utf8
            Write-Host "$Path file created and loaded!" -ForegroundColor Green
        } else {
            Write-Host "No variables entered. No file created." -ForegroundColor Yellow
        }
    }
}

Export-ModuleMember -Function Import-Env
