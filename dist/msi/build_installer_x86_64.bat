@echo off

for /f "usebackq tokens=*" %%i in (`"%ProgramFiles(x86)%\Microsoft Visual Studio\Installer\vswhere.exe" -latest -prerelease -products * -requires Microsoft.Component.MSBuild Microsoft.VisualStudio.Component.VC.Tools.x86.x64 -property installationPath`) do (
  set InstallDir=%%i
)
echo Found install dir at %InstallDir%

call "%InstallDir%\Common7\Tools\VsDevCmd.bat" -arch=amd64 -host_arch=amd64
if %errorlevel% neq 0 exit /b %errorlevel%

"%InstallDir%\MSBuild\Current\Bin\msbuild.exe" "%~dp0\WindowsInstaller.sln" -t:Restore
"%InstallDir%\MSBuild\Current\Bin\msbuild.exe" "%~dp0\WindowsInstaller.sln" -property:Configuration=Release -property:Platform=x64
if %errorlevel% neq 0 exit /b %errorlevel%

copy "%~dp0\..\build\dist\installers\windows\Release\en-us\msi-installer.msi" "%~dp0\..\build\dist\artifacts\%ARTIFACT_NAME%-installer-windows-x86_64.msi"