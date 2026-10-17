@echo off

where dotnet >nul 2>nul
if %errorlevel% neq 0 (
  echo Unable to find the .NET SDK, which is required to build the msi installer
  exit /b 1
)

dotnet build "%~dp0\WindowsInstaller.wixproj" -c Release -p:Platform=ARM64
if %errorlevel% neq 0 exit /b %errorlevel%

copy "%~dp0\..\build\dist\installers\windows\Release\en-us\msi-installer.msi" "%~dp0\..\build\dist\artifacts\%ARTIFACT_NAME%-installer-windows-arm64.msi"