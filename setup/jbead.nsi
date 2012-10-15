; NSIS script for installer for jbead

Name "jbead"

LoadLanguageFile "${NSISDIR}\Contrib\Language files\English.nlf"
LoadLanguageFile "${NSISDIR}\Contrib\Language files\German.nlf"

OutFile "jbead_1.0.24_setup.exe"
InstallDir $PROGRAMFILES\jbead
InstallDirRegKey HKLM "Software\bsoft\jbead" "Install_Dir"

Page directory
Page instfiles

UninstPage uninstConfirm
UninstPage instfiles

Section "jbead"

  SectionIn RO
  
  SetOutPath $INSTDIR
  File ..\jbead.exe
  File ..\jbead.jar
  File ..\LICENSE.txt
  
  SetOutPath $INSTDIR\samples
  File ..\samples\*.*
  
  WriteRegStr HKLM SOFTWARE\jbead "Install_Dir" "$INSTDIR"
  
  WriteRegStr HKCR .jbb "" "jbead"
  WriteRegStr HKCR .dbb "" "jbead"
  WriteRegStr HKCR "jbead\shell\open\command" "" '"$INSTDIR\jbead.exe" "%1"'
  WriteRegStr HKCR "jbead\shell\print\command" "" '"$INSTDIR\jbead.exe" "/p" "%1"'

  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\jbead" "DisplayName" "jbead"
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\jbead" "UninstallString" '"$INSTDIR\uninstall.exe"'
  WriteRegDWORD HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\jbead" "NoModify" 1
  WriteRegDWORD HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\jbead" "NoRepair" 1
  WriteUninstaller "uninstall.exe"
  
SectionEnd

Section "Start Menu Shortcuts"

  SetShellVarContext current

  CreateDirectory "$SMPROGRAMS\jbead"
  CreateShortCut "$SMPROGRAMS\jbead\Uninstall.lnk" "$INSTDIR\uninstall.exe" "" "$INSTDIR\uninstall.exe" 0
  CreateShortCut "$SMPROGRAMS\jbead\jbead.lnk" "$INSTDIR\jbead.exe" "" "$INSTDIR\jbead.exe" 0
  CreateShortCut "$DESKTOP\jbead.lnk" "$INSTDIR\jbead.exe" "" "$INSTDIR\jbead.exe" 0
  
SectionEnd

Section "Uninstall"
  
  SetShellVarContext current

  DeleteRegKey HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\jbead"
  DeleteRegKey HKLM SOFTWARE\jbead
  DeleteRegKey HKCR ".jbb"
  DeleteRegKey HKCR ".dbb"
  DeleteRegKey HKCR "jbead"

  Delete $INSTDIR\jbead.exe
  Delete $INSTDIR\jbead.jar
  Delete $INSTDIR\LICENSE.txt
  Delete $INSTDIR\uninstall.exe

  Delete "$SMPROGRAMS\jbead\Uninstall.lnk"
  Delete "$SMPROGRAMS\jbead\jbead.lnk"
  Delete "$DESKTOP\jbead.lnk"

  RMDir "$SMPROGRAMS\jbead"
  RMDir /r /REBOOTOK "$INSTDIR\samples"
  RMDir "$INSTDIR"

SectionEnd
