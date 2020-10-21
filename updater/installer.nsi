!include LogicLib.nsh
!include WinMessages.nsh
!include FileFunc.nsh

Name "Pdx-Unlimiter"
Icon "logo.ico"
Unicode True
RequestExecutionLevel user
OutFile "pdxu_installer.exe"

 Function .onInit
  StrCpy $INSTDIR `$PROFILE\pdx_unlimiter\launcher`
  ${If} ${FileExists} $INSTDIR
     MessageBox MB_YESNO "Do you want to reinstall Pdx-Unlimiter?" IDYES Reinstall
       Abort
     Reinstall:
       RMDir /r $INSTDIR
  ${EndIf}
 FunctionEnd

PageEx license
  LicenseText "GNU General Public License" "Ok"
  LicenseData "license_file.txt"
PageExEnd




 Function askForStart
   MessageBox MB_YESNO "Do you want to start the installation?" IDYES Install
     Abort
   Install:
 FunctionEnd

Page instfiles askForStart

Section
  SetOverwrite off

  ${If} ${FileExists} $INSTDIR
  ${Else}
    SetOutPath $INSTDIR
    File /r "build\image\*"
    File "logo.ico"
    File /oname=bin\sentry.properties sentry_prod.properties
    File build\bin\launcher.exe

    MessageBox MB_YESNO "Create desktop shortcut?" IDNO No
      CreateShortCut "$DESKTOP\Pdx-Unlimiter.lnk" "$INSTDIR\launcher.exe" "" `$INSTDIR\logo.ico` 0
    No:
  ${EndIf}
SectionEnd

Function .onInstSuccess
  nsExec::Exec "$INSTDIR\launcher.exe -installed"
FunctionEnd