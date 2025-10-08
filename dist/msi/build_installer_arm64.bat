CALL "C:\Program Files\Microsoft Visual Studio\2022\Enterprise\VC\Auxiliary\Build\vcvarsarm64.bat" || CALL "C:\Program Files\Microsoft Visual Studio\2022\Community\VC\Auxiliary\Build\vcvarsarm64.bat"
if %errorlevel% neq 0 exit /b %errorlevel%

msbuild "%~dp0\WindowsInstaller.sln" -t:Restore
msbuild "%~dp0\WindowsInstaller.sln" -property:Configuration=Release
if %errorlevel% neq 0 exit /b %errorlevel%

copy "%~dp0\..\build\dist\installers\windows\Release\en-us\msi-installer.msi" "%~dp0\..\build\dist\artifacts\%ARTIFACT_NAME%-installer-windows-arm64.msi"