!include LogicLib.nsh
!include WinMessages.nsh
!include FileFunc.nsh

Unicode True
SilentInstall silent
RequestExecutionLevel user
ShowInstDetails hide

OutFile "pdxu.exe"
VIProductVersion 2.67.0.00000
VIAddVersionKey ProductName "TemplateFx"
VIAddVersionKey LegalCopyright "Copyright (c) 2011-2018 Chris Mason"
VIAddVersionKey FileDescription "Dynamic Templating Tool"
VIAddVersionKey FileVersion 2.67.0.00000
VIAddVersionKey ProductVersion "2.67 / OpenJRE 10.0.2 (x64)"
VIAddVersionKey InternalName "Templatefx"
VIAddVersionKey OriginalFilename "TemplateFx.exe"

Section
  SetOverwrite off

  ${If} ${FileExists} `$TEMP\pdxu\jre-image`
  ${Else}
    SetOutPath "$TEMP\pdxu\jre-image"
    File /r "jre-image\*"
  ${EndIf}

  SetOutPath $TEMP
  ${GetParameters} $R0
  nsExec::Exec '"$TEMP\pdxu\jre-image\bin\Updater.bat" $R0'
SectionEnd