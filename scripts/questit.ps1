[CmdletBinding()]
param(
    [ValidateSet('run', 'test')]
    [string] $Action = 'run'
)

$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path $PSScriptRoot -Parent

function Find-MavenCommand {
    $installedMaven = Get-Command mvn.cmd -ErrorAction SilentlyContinue
    if ($installedMaven) {
        return $installedMaven.Source
    }

    # Utiliza la distribucion del wrapper ya descargada si esta disponible.
    $propertiesPath = Join-Path $projectRoot '.mvn/wrapper/maven-wrapper.properties'
    $properties = Get-Content -LiteralPath $propertiesPath -Raw | ConvertFrom-StringData
    $distributionName = [System.IO.Path]::GetFileNameWithoutExtension($properties.distributionUrl)
    $distributionDirectory = Join-Path $env:USERPROFILE ".m2/wrapper/dists/$distributionName"
    if (Test-Path -LiteralPath $distributionDirectory) {
        $cachedMaven = Get-ChildItem -LiteralPath $distributionDirectory -Filter mvn.cmd -Recurse |
            Select-Object -First 1
        if ($cachedMaven) {
            return $cachedMaven.FullName
        }
    }

    return Join-Path $projectRoot 'mvnw.cmd'
}

$mavenCommand = Find-MavenCommand
$previousPassword = [Environment]::GetEnvironmentVariable('DB_PASSWORD', 'Process')
$securePassword = $null
$passwordPointer = [IntPtr]::Zero
$exitCode = 1

Push-Location $projectRoot
try {
    if ([string]::IsNullOrEmpty($previousPassword)) {
        $securePassword = Read-Host 'Contrasena de PostgreSQL (entrada oculta)' -AsSecureString
        if ($securePassword.Length -eq 0) {
            throw 'La contrasena no puede estar vacia.'
        }

        # JDBC necesita texto en el entorno del proceso; nunca se pasa como argumento.
        $passwordPointer = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($securePassword)
        [Environment]::SetEnvironmentVariable('DB_PASSWORD',
            [Runtime.InteropServices.Marshal]::PtrToStringBSTR($passwordPointer), 'Process')
        [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($passwordPointer)
        $passwordPointer = [IntPtr]::Zero
    }

    $mavenArguments = @("-Dmaven.repo.local=$env:USERPROFILE/.m2/repository")
    if ($Action -eq 'test') {
        $mavenArguments += 'test'
    } else {
        $mavenArguments += 'spring-boot:run'
    }

    & $mavenCommand @mavenArguments
    $exitCode = $LASTEXITCODE
} finally {
    [Environment]::SetEnvironmentVariable('DB_PASSWORD', $previousPassword, 'Process')
    if ($passwordPointer -ne [IntPtr]::Zero) {
        [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($passwordPointer)
    }
    if ($securePassword) {
        $securePassword.Dispose()
    }
    Pop-Location
}

exit $exitCode
