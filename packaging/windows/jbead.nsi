; JBead Qt 6 port Windows installer (NSIS, MUI2 + MultiUser).
;
; A single dual-mode installer: prompts at launch for per-user vs
; per-machine. Per-user installs to %LOCALAPPDATA%\Programs\JBead
; (no UAC, HKCU); per-machine installs to %ProgramFiles%\JBead
; (UAC, HKLM). The previous version is auto-detected (in either
; hive) and uninstalled silently before laying down new files, so
; upgrades don't accumulate orphan Qt plugin DLLs.
;
; Build:    makensis /DAPPVER=<x.y.z> jbead.nsi
; Output:   jbead_<x.y.z>_setup.exe in the current directory.
;
; Driven by packaging/windows/build_installer.ps1 which extracts
; APPVER from the top-level CMakeLists.txt (single source of truth
; for the project version) and stages dist/windeployqt/.

!define APPNAME    "JBead"
!define COMPANY    "Brunold Software"
!define EXE        "jbead.exe"
!define SRCDIR     "..\..\dist\windeployqt"
!define ICON       "..\..\resources\windows\jbead.ico"
!define HOMEPAGE   "http://www.brunoldsoftware.ch"
!define UNINST_KEY "Software\Microsoft\Windows\CurrentVersion\Uninstall\${APPNAME}"
!define APP_KEY    "Software\${COMPANY}\${APPNAME}"

!ifndef APPVER
    !define APPVER "0.0.0"
!endif

; ---- MultiUser: pick per-user vs per-machine at launch -----------
; "Highest" is required by stock MultiUser.nsh for mixed-mode
; installs (Standard is rejected with "a mixed-mode installation
; requires MULTIUSER_EXECUTIONLEVEL to be set to Admin, Power or
; Highest"). Practical effect of highestAvailable: standard users
; get no UAC and can install per-user; admin users see UAC at
; launch and can pick either mode. Truly deferring UAC until the
; per-machine choice would need the third-party NsisMultiUser
; plugin. The installer writes its choice under
; HKCU\Software\Brunold Software\JBead\InstallMode so a later
; run defaults to the same mode.
!define MULTIUSER_EXECUTIONLEVEL Highest
!define MULTIUSER_MUI
!define MULTIUSER_INSTALLMODE_COMMANDLINE
!define MULTIUSER_INSTALLMODE_INSTDIR "${APPNAME}"
!define MULTIUSER_INSTALLMODE_INSTDIR_REGISTRY_KEY "${APP_KEY}"
!define MULTIUSER_INSTALLMODE_INSTDIR_REGISTRY_VALUENAME "Install_Dir"
!define MULTIUSER_INSTALLMODE_DEFAULT_REGISTRY_KEY "${APP_KEY}"
!define MULTIUSER_INSTALLMODE_DEFAULT_REGISTRY_VALUENAME "InstallMode"
; jbead.exe is 64-bit; install per-machine into C:\Program Files,
; not the 32-bit-installer default of C:\Program Files (x86).
!define MULTIUSER_USE_PROGRAMFILES64

; ---- MUI2 styling -----------------------------------------------
!define MUI_ICON                "${ICON}"
!define MUI_UNICON              "${ICON}"
!define MUI_ABORTWARNING

!include "MUI2.nsh"
!include "MultiUser.nsh"
!include "FileFunc.nsh"
!include "LogicLib.nsh"

Unicode true
Name        "${APPNAME} ${APPVER}"
OutFile     "jbead_${APPVER}_setup.exe"
SetCompressor /SOLID lzma
BrandingText "${APPNAME} ${APPVER}"

VIProductVersion "${APPVER}.0"
VIAddVersionKey "ProductName"     "${APPNAME}"
VIAddVersionKey "CompanyName"     "${COMPANY}"
VIAddVersionKey "FileDescription" "JBead bead-pattern designer"
VIAddVersionKey "FileVersion"     "${APPVER}"
VIAddVersionKey "ProductVersion"  "${APPVER}"
VIAddVersionKey "LegalCopyright"  "GPL v3 or later"

; ---- Pages -------------------------------------------------------
; Minimal flow: install-mode picker, then progress. No welcome,
; license, components or directory page; the install path is
; fixed by MultiUser based on the chosen mode.
!insertmacro MULTIUSER_PAGE_INSTALLMODE
!insertmacro MUI_PAGE_INSTFILES

!insertmacro MUI_UNPAGE_INSTFILES

!insertmacro MUI_LANGUAGE "English"
!insertmacro MUI_LANGUAGE "German"

; ---- Helpers -----------------------------------------------------

; Detect a previous install in either hive, run its uninstaller
; silently, then continue. _?=$0 keeps the uninstaller in place so
; ExecWait actually waits for it -- without it, the uninstaller
; copies itself to %TEMP% and returns immediately.
Function .onInit
    !insertmacro MULTIUSER_INIT

    ReadRegStr $0 HKLM "${APP_KEY}" "Install_Dir"
    StrCmp $0 "" tryHKCU
    Goto haveOld
tryHKCU:
    ReadRegStr $0 HKCU "${APP_KEY}" "Install_Dir"
    StrCmp $0 "" done
haveOld:
    IfFileExists "$0\uninstall.exe" 0 done
    DetailPrint "Removing previous install at $0"
    ExecWait '"$0\uninstall.exe" /S _?=$0'
    Delete "$0\uninstall.exe"
    RMDir  "$0"
done:
FunctionEnd

Function un.onInit
    !insertmacro MULTIUSER_UNINIT
FunctionEnd

; ---------- install ----------------------------------------------

Section "-${APPNAME}"
    SetOutPath "$INSTDIR"
    ; Everything windeployqt assembled: the exe, its DLLs, the Qt
    ; plugins tree, plus docs.
    File /r "${SRCDIR}\*"

    ; SHCTX is HKLM for per-machine, HKCU for per-user (set up by
    ; MultiUser.nsh). Same logical key in both cases; only the
    ; hive differs.
    WriteRegStr SHCTX "${APP_KEY}" "Install_Dir" "$INSTDIR"
    WriteRegStr SHCTX "${APP_KEY}" "InstallMode" "$MultiUser.InstallMode"

    ; .jbb file association. Schema is intentionally unchanged from
    ; the legacy installer; deeper shell integration (OpenWithProgids,
    ; Applications\jbead.exe, Default Apps Capabilities) is deferred
    ; to a separate change.
    WriteRegStr SHCTX "Software\Classes\.jbb"                              "" "JBEAD.Pattern"
    WriteRegStr SHCTX "Software\Classes\JBEAD.Pattern"                    "" "JBead bead pattern"
    WriteRegStr SHCTX "Software\Classes\JBEAD.Pattern\DefaultIcon"        "" "$INSTDIR\${EXE},0"
    WriteRegStr SHCTX "Software\Classes\JBEAD.Pattern\shell\open\command" "" '"$INSTDIR\${EXE}" "%1"'

    ; Add/Remove Programs registration.
    WriteRegStr   SHCTX "${UNINST_KEY}" "DisplayName"          "${APPNAME}"
    WriteRegStr   SHCTX "${UNINST_KEY}" "DisplayVersion"       "${APPVER}"
    WriteRegStr   SHCTX "${UNINST_KEY}" "Publisher"            "${COMPANY}"
    WriteRegStr   SHCTX "${UNINST_KEY}" "DisplayIcon"          "$INSTDIR\${EXE},0"
    WriteRegStr   SHCTX "${UNINST_KEY}" "InstallLocation"      "$INSTDIR"
    WriteRegStr   SHCTX "${UNINST_KEY}" "URLInfoAbout"         "${HOMEPAGE}"
    WriteRegStr   SHCTX "${UNINST_KEY}" "UninstallString"      '"$INSTDIR\uninstall.exe"'
    WriteRegStr   SHCTX "${UNINST_KEY}" "QuietUninstallString" '"$INSTDIR\uninstall.exe" /S'
    WriteRegDWORD SHCTX "${UNINST_KEY}" "NoModify" 1
    WriteRegDWORD SHCTX "${UNINST_KEY}" "NoRepair" 1

    ; Show the "size on disk" column in Add/Remove Programs.
    ; ${GetSize} returns KB in $0 (the /S=0K switch).
    ${GetSize} "$INSTDIR" "/S=0K" $0 $1 $2
    WriteRegDWORD SHCTX "${UNINST_KEY}" "EstimatedSize" $0

    WriteUninstaller "$INSTDIR\uninstall.exe"

    ; Tell Explorer the file-association table changed so the .jbb
    ; icon appears immediately instead of after the next logon.
    System::Call 'shell32::SHChangeNotify(i 0x08000000, i 0, i 0, i 0)'
SectionEnd

Section "-Start Menu Shortcuts"
    CreateDirectory "$SMPROGRAMS\${APPNAME}"
    CreateShortCut  "$SMPROGRAMS\${APPNAME}\${APPNAME}.lnk"  "$INSTDIR\${EXE}"
    CreateShortCut  "$SMPROGRAMS\${APPNAME}\Uninstall.lnk"   "$INSTDIR\uninstall.exe"
    IfFileExists "$INSTDIR\LICENSE.txt" 0 +2
    CreateShortCut  "$SMPROGRAMS\${APPNAME}\License.lnk"     "$INSTDIR\LICENSE.txt"
SectionEnd

Section "-Desktop Shortcut"
    CreateShortCut "$DESKTOP\${APPNAME}.lnk" "$INSTDIR\${EXE}"
SectionEnd

; ---------- uninstall --------------------------------------------

Section "Uninstall"
    DeleteRegKey SHCTX "${UNINST_KEY}"
    DeleteRegKey SHCTX "${APP_KEY}"
    DeleteRegKey SHCTX "Software\Classes\.jbb"
    DeleteRegKey SHCTX "Software\Classes\JBEAD.Pattern"

    Delete "$DESKTOP\${APPNAME}.lnk"
    Delete "$SMPROGRAMS\${APPNAME}\*.lnk"
    RMDir  "$SMPROGRAMS\${APPNAME}"

    ; windeployqt wrote many files. Blow away the install dir
    ; recursively; the user was warned on the UninstConfirm page.
    RMDir /r "$INSTDIR"

    System::Call 'shell32::SHChangeNotify(i 0x08000000, i 0, i 0, i 0)'
SectionEnd
