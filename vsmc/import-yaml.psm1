$Script:GlobalYamlCache = $null

function Import-Yaml {
    [CmdletBinding()]
    param(
        [Parameter(Mandatory = $true)]
        [string]$DirectoryPath,

        [Parameter(Mandatory = $false)]
        [switch]$Force
    )

    if ($null -ne $Script:GlobalYamlCache -and -not $Force) {
        return $Script:GlobalYamlCache
    }
    if (-not (Test-Path -Path $DirectoryPath)) {
        throw "Directory not found: $DirectoryPath"
    }

    if (-not (Get-Module -ListAvailable -Name "powershell-yaml")) {
        Write-Warning "The 'powershell-yaml' module is required.`n Installing..."
        Set-PSRepository -Name 'PSGallery' -InstallationPolicy Trusted
        Install-Module powershell-yaml -Force -AcceptLicense -Scope CurrentUser
    }
    Import-Module powershell-yaml -ErrorAction Stop

    $yamlFiles = Get-ChildItem -Path $DirectoryPath -Filter "*.yaml" -File
    if ($yamlFiles.Count -eq 0) {
        $yamlFiles = Get-ChildItem -Path $DirectoryPath -Filter "*.yml" -File
    }
    if ($yamlFiles.Count -eq 0) {
        throw "No YAML files found in directory: $DirectoryPath"
    }

    $masterConfig = [ordered]@{
        "unit" = [ordered]@{
            "units" = [ordered]@{}
        }
    }

    foreach ($file in $yamlFiles) {
        $rawContent = Get-Content -Path $file.FullName -Raw
        $parsed = ConvertFrom-Yaml $rawContent

        if ($parsed.unit -and $parsed.unit.units) {
            foreach ($unitKey in $parsed.unit.units.Keys) {
                $masterConfig.unit.units[$unitKey] = $parsed.unit.units[$unitKey]
            }
        }
    }

    # Helper function to traverse object/dictionary paths dynamically
    function Resolve-PathValue {
        param($targetPath, $rootObj)
        $parts = $targetPath -split '\.'
        $current = $rootObj
        foreach ($part in $parts) {
            if ($null -eq $current) { return $null }
            if ($current -is [System.Collections.IDictionary]) {
                if ($current.Contains($part)) { $current = $current[$part] } else { return $null }
            } elseif ($current -is [PSCustomObject]) {
                if ($current.psobject.properties.Name -contains $part) { $current = $current.$part } else { return $null }
            } else { return $null }
        }
        return $current
    }

    # Resolver for ${...} variables (Environment variables or internal references)
    function Resolve-String {
        param($text, $root)
        $matchedItems = [regex]::Matches($text, '\$\{([^}]+)\}')
        if ($matchedItems.Count -eq 0) { return $text }

        $resolvedText = $text
        foreach ($match in $matchedItems) {
            $expr = $match.Groups[1].Value
            $replacement = $null

            $envVal = [System.Environment]::GetEnvironmentVariable($expr)
            if (-not [string]::IsNullOrEmpty($envVal)) {
                $replacement = $envVal
            } else {
                $internalVal = Resolve-PathValue -targetPath $expr -root $root
                if ($null -ne $internalVal) { $replacement = [string]$internalVal }
            }

            if ($null -ne $replacement) {
                $resolvedText = $resolvedText.Replace($match.Value, $replacement)
            }
        }
        return $resolvedText
    }

    # Recursive walker supporting Dictionaries, Custom Objects, and Lists
    function Update-Strings($node, $root) {
        if ($node -is [System.Collections.IDictionary]) {
            foreach ($key in @($node.Keys)) {
                $val = $node[$key]
                if ($val -is [string]) {
                    $newVal = Resolve-String -text $val -root $root
                    if ($newVal -ne $val) { $node[$key] = $newVal; $Script:hasChanges = $true }
                } else {
                    Update-Strings -node $val -root $root
                }
            }
        } elseif ($node -is [PSCustomObject]) {
            foreach ($prop in $node.psobject.properties) {
                $val = $prop.Value
                if ($val -is [string]) {
                    $newVal = Resolve-String -text $val -root $root
                    if ($newVal -ne $val) { $node.($prop.Name) = $newVal; $Script:hasChanges = $true }
                } else {
                    Update-Strings -node $val -root $root
                }
            }
        } elseif ($node -is [System.Collections.IList] -and $node -isnot [string]) {
            for ($i = 0; $i -lt $node.Count; $i++) {
                $val = $node[$i]
                if ($val -is [string]) {
                    $newVal = Resolve-String -text $val -root $root
                    if ($newVal -ne $val) { $node[$i] = $newVal; $Script:hasChanges = $true }
                } else {
                    Update-Strings -node $val -root $root
                }
            }
        }
    }

    $maxPasses = 5
    $pass = 0
    $changed = $true

    while ($changed -and ($pass -lt $maxPasses)) {
        $pass++
        $changed = $false
        $Script:hasChanges = $false
        Update-Strings -node $masterConfig -root $masterConfig
        $changed = $Script:hasChanges
    }

    $Script:GlobalYamlCache = $masterConfig
    return $Script:GlobalYamlCache
}
Export-ModuleMember -Function Import-Yaml
