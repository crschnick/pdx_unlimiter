!include LogicLib.nsh
!include WinMessages.nsh
!include FileFunc.nsh

Icon "logo.ico"
Unicode True
#SilentInstall silent
RequestExecutionLevel user
#ShowInstDetails hide

OutFile "pdxu.exe"
VIProductVersion 2.67.0.00000
VIAddVersionKey ProductName "TemplateFx"
VIAddVersionKey LegalCopyright "Copyright (c) 2011-2018 Chris Mason"
VIAddVersionKey FileDescription "Dynamic Templating Tool"
VIAddVersionKey FileVersion 2.67.0.00000
VIAddVersionKey ProductVersion "2.67 / OpenJRE 10.0.2 (x64)"
VIAddVersionKey InternalName "Templatefx"
VIAddVersionKey OriginalFilename "TemplateFx.exe"

 Function .onInit
  StrCpy $INSTDIR `$PROFILE\pdx_unlimiter\launcher`
  ${If} ${FileExists} $INSTDIR
  ${Else}
   MessageBox MB_YESNO "Do you want to start the installation?" IDYES NoAbort
     Abort ; causes installer to quit.
   NoAbort:
  ${EndIf}
 FunctionEnd

Section
  SetOverwrite off

  ${If} ${FileExists} $INSTDIR
  ${Else}
    SetOutPath $INSTDIR
    File /r "build\image\*"
    File "logo.ico"

MessageBox MB_YESNO "Create desktop shortcut?" IDNO No
  CreateShortCut "$DESKTOP\Pdx-Unlimiter.lnk" "$INSTDIR\launcher.exe" "" `$INSTDIR\logo.ico` 0
No:
  ${EndIf}
SectionEnd

Function .onInstSuccess
  ${GetParameters} $R0
  nsExec::Exec 'START /MIN CMD.EXE /C  "$INSTDIR\bin\Updater.bat" $R0'
FunctionEnd