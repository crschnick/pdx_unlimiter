!include LogicLib.nsh
!include WinMessages.nsh
!include FileFunc.nsh

Name "Pdx-Unlimiter"
Icon "logo.ico"
Unicode True
RequestExecutionLevel user
OutFile "pdxu_installer.exe"

 Function .onInit
  StrCpy $INSTDIR `$PROFILE\pdx_unlimiter`
 FunctionEnd

PageEx license
  LicenseText "GNU General Public License" "Ok"
  LicenseData "license_file.txt"
PageExEnd

Page directory
Page instfiles checkReinstall


Var LAUNCHERDIR

Function checkReinstall
  StrCpy $LAUNCHERDIR `$INSTDIR\launcher`
  ${If} ${FileExists} $LAUNCHERDIR
     MessageBox MB_YESNO "Do you want to reinstall the Pdx-Unlimiter launcher?" IDYES Reinstall
       Abort
     Reinstall:
       RMDir /r $LAUNCHERDIR
  ${EndIf}
FunctionEnd

Section
    SetOverwrite off
    SetOutPath $LAUNCHERDIR
    File /r "build\image\*"
    File "logo.ico"
    File /oname=bin\sentry.properties sentry_prod.properties
    File build\bin\launcher.exe

    CreateShortCut "$INSTDIR\Pdx-Unlimiter.lnk" "$LAUNCHERDIR\launcher.exe" "" `$LAUNCHERDIR\logo.ico` 0

    MessageBox MB_YESNO "Create desktop shortcut?" IDNO No
      CreateShortCut "$DESKTOP\Pdx-Unlimiter.lnk" "$LAUNCHERDIR\launcher.exe" "" `$LAUNCHERDIR\logo.ico` 0
    No:
SectionEnd

Function .onInstSuccess
  nsExec::Exec "$LAUNCHERDIR\launcher.exe -installed"
FunctionEnd