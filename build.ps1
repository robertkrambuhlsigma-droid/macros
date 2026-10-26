$ErrorActionPreference = 'Stop'
$root = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $root

try {
    $javaInfo = (& java -version 2>&1 | Out-String)
} catch {
    Write-Host 'Java was not found. Install a JDK 25 or newer and make sure java.exe is on PATH.' -ForegroundColor Red
    exit 1
}
$match = [regex]::Match($javaInfo, 'version "(\d+)')
if (-not $match.Success) {
    $match = [regex]::Match($javaInfo, 'openjdk (\d+)')
}
if (-not $match.Success -or [int]$match.Groups[1].Value -lt 25) {
    Write-Host 'Minecraft 26.2 mod builds require JDK 25 or newer.' -ForegroundColor Red
    Write-Host $javaInfo
    exit 1
}

$gradleVersion = '9.5.1'
$toolDir = Join-Path $root '.gradle-tools'
$gradleHome = Join-Path $toolDir "gradle-$gradleVersion"
$gradleExe = Join-Path $gradleHome 'bin\gradle.bat'

if (-not (Test-Path $gradleExe)) {
    New-Item -ItemType Directory -Force -Path $toolDir | Out-Null
    $zipPath = Join-Path $toolDir "gradle-$gradleVersion-bin.zip"
    $url = "https://services.gradle.org/distributions/gradle-$gradleVersion-bin.zip"
    Write-Host "Downloading Gradle $gradleVersion..."
    Invoke-WebRequest -Uri $url -OutFile $zipPath
    Expand-Archive -Path $zipPath -DestinationPath $toolDir -Force
    Remove-Item $zipPath -Force
}

Write-Host 'Building Command Macros for Minecraft Java 26.2...'
& $gradleExe --no-daemon build
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

Write-Host ''
Write-Host "Success. Mod JAR(s) are in: $root\build\libs" -ForegroundColor Green
Write-Host 'Copy commandmacros-1.0.0.jar into your Minecraft instance mods folder.'
