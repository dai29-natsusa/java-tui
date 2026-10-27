function Install-LocalYamlModule {
    [CmdletBinding()]
    param (
        [Parameter]
        [string]$Path = "$PSScriptRoot/powershell-yaml.0.4.12.nupkg",

        [Parameter]
        [string]$LocalRepoPath = "$env:USERPROFILE/nupkg-modules"
    )

    $ModuleName = "powershell-yaml"

    # 1. Check if the module is already installed
    Write-Host "Checking if $ModuleName is installed..." -ForegroundColor Cyan
    $module = Get-Module -Name $ModuleName -ListAvailable

    if ($module) {
        Write-Host "$ModuleName is already installed (Version: $($module.Version))." -ForegroundColor Green
        return
    }

    Write-Host "$ModuleName not found. Proceeding with local .nupkg installation..." -ForegroundColor Yellow

    # 2. Validate the .nupkg file exists
    if (-not (Test-Path -Path $Path)) {
        throw "Error: The file at '$Path' does not exist."
    }

    # 3. Create a clean temporary local repository folder
    if (-not (Test-Path -Path $LocalRepoPath)) {
        New-Item -ItemType Directory -Path $LocalRepoPath -Force | Out-Null
    }

    # Clean out any old files in the local repo folder and copy the new nupkg
    Get-ChildItem -Path $LocalRepoPath -Filter *.nupkg | Remove-Item -Force
    Copy-Item -Path $Path -Destination $LocalRepoPath -Force

    # 4. Register the temporary repository
    $RepoName = "LocalRepo"
    if (-not (Get-PSRepository -Name $RepoName -ErrorAction SilentlyContinue)) {
        Register-PSRepository -Name $RepoName -SourceLocation $LocalRepoPath -InstallationPolicy Trusted
    }

    # 5. Install the module
    try {
        Write-Host "Installing $ModuleName from local repository..." -ForegroundColor Cyan
        Install-Module -Name $ModuleName -Repository $RepoName -Scope CurrentUser -Force
        Write-Host "$ModuleName successfully installed!" -ForegroundColor Green
    }
    catch {
        Write-Error "Failed to install module: $_"
    }
    finally {
        # 6. Always clean up and unregister the repository shortcut
        if (Get-PSRepository -Name $RepoName -ErrorAction SilentlyContinue) {
            Unregister-PSRepository -Name $RepoName
        }
    }
}
Export-ModuleMember -Function Install-LocalYamlModule
