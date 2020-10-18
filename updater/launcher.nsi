!include LogicLib.nsh

Icon "logo.ico"
Unicode True
SilentInstall silent
RequestExecutionLevel user
ShowInstDetails hide

OutFile "launcher.exe"

Section
  StrCpy $INSTDIR `$PROFILE\pdx_unlimiter\launcher`
  nsExec::Exec '$INSTDIR\bin\Updater.bat'
SectionEnd