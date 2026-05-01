<#
    Builds JBead on Windows, assembles the redistributable tree
    via windeployqt, and produces jbead_<version>_setup.exe with NSIS.

    Prerequisites:
        * Qt 6.5+ MSVC or MinGW installation on PATH (qmake, cmake,
          windeployqt all reachable from the VS Dev Prompt or a
          qt-env shell).
        * CMake 3.21+ and Ninja (or MSBuild).
        * NSIS (makensis) on PATH.

    Usage:
        powershell -File packaging\windows\build_installer.ps1
#>

$ErrorActionPreference = 'Stop'

$Root     = Resolve-Path (Join-Path $PSScriptRoot '..\..')
$BuildDir = Join-Path $Root 'build-windows'
$StageDir = Join-Path $Root 'dist\windeployqt'
$DistDir  = Join-Path $Root 'dist'

Write-Host '==> configure'
cmake -S $Root -B $BuildDir -G Ninja `
    '-DCMAKE_BUILD_TYPE=Release' `
    '-DJBEAD_BUILD_TESTS=OFF'

Write-Host '==> build'
cmake --build $BuildDir

Write-Host '==> install + windeployqt'
if (Test-Path $StageDir) { Remove-Item $StageDir -Recurse -Force }
cmake --install $BuildDir --prefix $StageDir

$Exe = Join-Path $StageDir 'jbead.exe'
if (-not (Test-Path $Exe)) { throw "Built executable not found at $Exe" }

windeployqt --release --no-opengl-sw --no-system-d3d-compiler $Exe

Copy-Item (Join-Path $Root 'LICENSE.txt') $StageDir -Force
if (Test-Path (Join-Path $Root 'README.txt')) { Copy-Item (Join-Path $Root 'README.txt') $StageDir -Force }

Write-Host '==> makensis'
# Pull the project VERSION out of CMakeLists.txt so the installer
# metadata stays in sync with the single source of truth.
$CMakeLists = Get-Content (Join-Path $Root 'CMakeLists.txt') -Raw
$VersionMatch = [regex]::Match($CMakeLists, 'project\s*\(\s*jbead[^)]*?VERSION\s+([0-9][0-9.]*)', 'IgnoreCase, Singleline')
if (-not $VersionMatch.Success) { throw 'Could not parse VERSION from CMakeLists.txt' }
$AppVer = $VersionMatch.Groups[1].Value
Write-Host "    APPVER = $AppVer"

New-Item -ItemType Directory -Force -Path $DistDir | Out-Null
Push-Location (Join-Path $Root 'packaging\windows')
try {
    & makensis "/DAPPVER=$AppVer" 'jbead.nsi'
    if ($LASTEXITCODE -ne 0) { throw 'makensis failed' }
    $InstallerName = "jbead_${AppVer}_setup.exe"
    Move-Item -Force $InstallerName $DistDir
} finally {
    Pop-Location
}

Write-Host "==> done. Installer in $DistDir\$InstallerName"
