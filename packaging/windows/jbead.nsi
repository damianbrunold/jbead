; JBead Qt 6 port Windows installer (NSIS).
;
; Assumes dist/windeployqt/ has been populated by packaging/windows/
; build_installer.ps1, which runs cmake --install, copies the built
; executable, runs `windeployqt jbead.exe`, and drops LICENSE +
; README alongside.
;
; Build with:
;     makensis jbead.nsi
;
; Output: jbead_setup.exe in the current directory.

!define APPNAME    "JBead"
!define COMPANY    "Brunold Software"
; APPVER is normally injected by build_installer.ps1 via
; `makensis /DAPPVER=<x.y.z>` (parsed from the project()
; VERSION line in the top-level CMakeLists.txt). The fallback
; below only kicks in when makensis is invoked by hand.
!ifndef APPVER
    !define APPVER "0.0.0"
!endif
!define EXE        "jbead.exe"
!define SRCDIR     "..\..\dist\windeployqt"

Unicode true
Name "${APPNAME}"
OutFile "jbead_setup.exe"
InstallDir "$PROGRAMFILES64\${APPNAME}"
InstallDirRegKey HKLM "Software\${COMPANY}\${APPNAME}" "Install_Dir"
RequestExecutionLevel admin
SetCompressor /SOLID lzma
VIProductVersion "${APPVER}.0"
VIAddVersionKey "ProductName"     "${APPNAME}"
VIAddVersionKey "CompanyName"     "${COMPANY}"
VIAddVersionKey "FileDescription" "JBead bead-pattern designer"
VIAddVersionKey "FileVersion"     "${APPVER}"
VIAddVersionKey "LegalCopyright"  "GPL v3 or later"

Page directory
Page instfiles
UninstPage uninstConfirm
UninstPage instfiles

; -------------------- install ------------------------------------

Section "${APPNAME}"
    SectionIn RO
    SetOutPath "$INSTDIR"
    File /r "${SRCDIR}\*"

    WriteRegStr HKLM "Software\${COMPANY}\${APPNAME}" "Install_Dir" "$INSTDIR"

    ; .jbb file association.
    WriteRegStr HKCR ".jbb" "" "JBead.Pattern"
    WriteRegStr HKCR "JBead.Pattern" "" "JBead bead pattern"
    WriteRegStr HKCR "JBead.Pattern\DefaultIcon" "" "$INSTDIR\${EXE},0"
    WriteRegStr HKCR "JBead.Pattern\shell\open\command" "" '"$INSTDIR\${EXE}" "%1"'

    WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${APPNAME}" "DisplayName"     "${APPNAME}"
    WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${APPNAME}" "DisplayVersion"  "${APPVER}"
    WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${APPNAME}" "Publisher"       "${COMPANY}"
    WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${APPNAME}" "UninstallString" '"$INSTDIR\uninstall.exe"'
    WriteRegDWORD HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${APPNAME}" "NoModify" 1
    WriteRegDWORD HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${APPNAME}" "NoRepair" 1
    WriteUninstaller "$INSTDIR\uninstall.exe"
SectionEnd

Section "Start Menu Shortcuts"
    SetShellVarContext all
    CreateDirectory "$SMPROGRAMS\${APPNAME}"
    CreateShortCut  "$SMPROGRAMS\${APPNAME}\${APPNAME}.lnk"  "$INSTDIR\${EXE}"
    CreateShortCut  "$SMPROGRAMS\${APPNAME}\Uninstall.lnk"   "$INSTDIR\uninstall.exe"
    IfFileExists "$INSTDIR\LICENSE.txt" 0 +2
    CreateShortCut  "$SMPROGRAMS\${APPNAME}\License.lnk"     "$INSTDIR\LICENSE.txt"
SectionEnd

Section "Desktop Shortcut"
    SetShellVarContext all
    CreateShortCut "$DESKTOP\${APPNAME}.lnk" "$INSTDIR\${EXE}"
SectionEnd

; -------------------- uninstall ----------------------------------

Section "Uninstall"
    SetShellVarContext all
    DeleteRegKey HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${APPNAME}"
    DeleteRegKey HKLM "Software\${COMPANY}\${APPNAME}"
    DeleteRegKey HKCR ".jbb"
    DeleteRegKey HKCR "JBead.Pattern"

    Delete "$DESKTOP\${APPNAME}.lnk"
    Delete "$SMPROGRAMS\${APPNAME}\*.lnk"
    RMDir  "$SMPROGRAMS\${APPNAME}"

    RMDir /r "$INSTDIR"
SectionEnd
