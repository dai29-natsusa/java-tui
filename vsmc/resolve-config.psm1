function Resolve-ConfigObject {

    param(
        [object]$Object
    )

    if ($Object -is [string]) {
        return [regex]::Replace(
            $Object,
            '\$\{([A-Za-z0-9_]+)\}',
            {
                param($match)

                $envName = $match.Groups[1].Value
                [System.Environment]::GetEnvironmentVariable($envName)
            }
        )
    }

    if ($Object -is [System.Collections.IDictionary]) {

        foreach ($key in @($Object.Keys)) {
            $Object[$key] = Resolve-ConfigObject $Object[$key]
        }

        return $Object
    }

    foreach ($property in $Object.PSObject.Properties) {
        $property.Value = Resolve-ConfigObject $property.Value
    }

    return $Object
}
Export-ModuleMember -Function Resolve-ConfigObject
