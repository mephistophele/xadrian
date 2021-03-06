!include "MUI.nsh"

!define JRE_VERSION "1.6"
!define JRE_URL "http://javadl.sun.com/webapps/download/AutoDL?BundleId=58134"

!define MyApp_AppUserModelId "Ailis.${project.name}"

!include "FileFunc.nsh"
!insertmacro GetFileVersion
!insertmacro GetParameters
!include "WordFunc.nsh"
!insertmacro VersionCompare
!include Util.nsh

Name "${project.name}"
OutFile "${project.build.directory}\${project.artifactId}-${project.version}-windows.exe"
RequestExecutionLevel admin
SetCompressor bzip2
InstallDir $PROGRAMFILES64\${project.name}

!insertmacro MUI_PAGE_WELCOME
!insertmacro MUI_PAGE_DIRECTORY
!insertmacro MUI_PAGE_INSTFILES
!insertmacro MUI_PAGE_FINISH

!insertmacro MUI_UNPAGE_CONFIRM
!insertmacro MUI_UNPAGE_INSTFILES
!insertmacro MUI_UNPAGE_FINISH

!insertmacro MUI_LANGUAGE "English"
!insertmacro MUI_LANGUAGE "German"

LangString JavaInstall ${LANG_ENGLISH} "${project.name} needs the Java Runtime Environment version ${JRE_VERSION} or newer but it is not installed on your system. Do you want to automatically download and install it? Press 'No' if you want to install Java manually later."
LangString JavaInstall ${LANG_GERMAN} "${project.name} braucht die Java Laufzeit Umgebung Version ${JRE_VERSION} oder neuer. Wollen Sie sie jetzt automatisch herunterladen und installieren? Klicken Sie 'Nein' um Java nach der Installation von ${project.name} selbst zu installieren."

LangString X3CExtension ${LANG_ENGLISH} "Xadrian Factory Complex"
LangString X3CExtension ${LANG_GERMAN} "Xadrian Fabrik-Komplex"

Function GetJRE
  MessageBox MB_YESNO|MB_ICONQUESTION $(JavaInstall) IDNO done
  StrCpy $2 "$TEMP\Java Runtime Environment.exe"
  nsisdl::download /TIMEOUT=30000 ${JRE_URL} $2 
  Pop $R0
  StrCmp $R0 "success" +3
  MessageBox MB_OK "Error: $R0"
  Quit
  ExecWait "$2"
  Delete "$2"
  done:
FunctionEnd

Function DetectJRE
  ${GetFileVersion} "$SYSDIR\javaw.exe" $R1
  ${VersionCompare} ${JRE_VERSION} $R1 $R2
  StrCmp $R2 0 done
  StrCmp $R2 2 done
  Call GetJRE
  done:
FunctionEnd
 
!macro RegisterExtension Exe Ext Desc Icon
  Push $0
  Push $1
  ReadRegStr $1 HKCR ${Ext} ""  ; read current file association
  StrCmp "$1" "" NoBackup  ; is it empty
  StrCmp "$1" "${Desc}" NoBackup  ; is it our own
  WriteRegStr HKCR ${Ext} "backup_val" "$1"  ; backup current value
NoBackup:
  WriteRegStr HKCR ${Ext} "" "${Desc}"  ; set our file association
  ReadRegStr $0 HKCR ${Desc} ""
  StrCmp $0 "" 0 Skip
  WriteRegStr HKCR "${Desc}" "" "${Desc}"
  WriteRegStr HKCR "${Desc}\shell" "" "open"
  WriteRegStr HKCR "${Desc}\DefaultIcon" "" "${Icon}"
Skip:
  WriteRegStr HKCR "${Desc}\shell\open\command" "" '"${Exe}" "%1"'
!macroend
 
!macro UnRegisterExtension Ext Desc
  Push $0
  Push $1
  ReadRegStr $1 HKCR ${Ext} ""
  StrCmp $1 ${Desc} 0 NoOwn ; only do this if we own it
  ReadRegStr $1 HKCR ${Ext} "backup_val"
  StrCmp $1 "" 0 Restore ; if backup="" then delete the whole key
  DeleteRegKey HKCR ${Ext}
  DeleteRegKey HKCR ${Desc}
  Goto NoOwn
Restore:
  WriteRegStr HKCR ${Ext} "" $1
  DeleteRegValue HKCR ${Ext} "backup_val"
  DeleteRegKey HKCR ${Desc} ;Delete key with association name settings
NoOwn:
  Pop $1
  Pop $0
!macroend

Section
  Call DetectJRE
  SetOutPath $INSTDIR
  WriteUninstaller "$INSTDIR\Uninstall.exe"
  
  File ${basedir}\LICENSE.txt
  File /oname=${project.name}.exe ${project.build.directory}\${project.artifactId}-${project.version}.exe
  SetOutPath $INSTDIR\lib
  File lib\*.jar
  File /oname=${project.artifactId}.jar ${project.build.directory}\${project.artifactId}-${project.version}.jar
  CreateShortCut "$SMPROGRAMS\${project.name}.lnk" \
    "$INSTDIR\${project.name}.exe" "" "$INSTDIR\${project.name}.exe" 0 SW_SHOWNORMAL
  CreateShortCut "$DESKTOP\${project.name}.lnk" \
    "$INSTDIR\${project.name}.exe" "" "$INSTDIR\${project.name}.exe" 0 SW_SHOWNORMAL
  WinShell::SetLnkAUMI "$SMPrograms\${project.name}.lnk" "${MyApp_AppUserModelId}"
  WinShell::SetLnkAUMI "$DESKTOP\${project.name}.lnk" "${MyApp_AppUserModelId}"
    
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${project.name}" "DisplayName" "${project.name}"
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${project.name}" "DisplayVersion" "${project.version}"
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${project.name}" "DisplayIcon" "$INSTDIR\${project.name}.exe,0"
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${project.name}" "UninstallString" "$INSTDIR\Uninstall.exe"
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${project.name}" "Publisher" "${project.organization.name}"
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${project.name}" "HelpLink" "${project.url}"
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${project.name}" "URLInfoAbout" "${project.url}"
  WriteRegDWORD HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${project.name}" "NoModify" 1
  WriteRegDWORD HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${project.name}" "NoRepair" 1
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${project.name}" "InstallLocation" "$INSTDIR"
  ${GetSize} "$INSTDIR" "/S=0K" $0 $1 $2
  IntFmt $0 "0x%08X" $0
  WriteRegDWORD HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${project.name}" "EstimatedSize" $0
  !insertmacro RegisterExtension "$INSTDIR\${project.name}.exe" ".x3c" "$(X3CExtension)" "$INSTDIR\${project.name}.exe,0"
SectionEnd

Section "Uninstall"
  WinShell::UninstAppUserModelId "${MyApp_AppUserModelId}"
  !insertmacro UnregisterExtension ".x3c" "$(X3CExtension)"
  RMDir /r "$INSTDIR\lib"
  Delete "$INSTDIR\${project.name}.exe"
  Delete "$INSTDIR\LICENSE.txt"
  Delete "$INSTDIR\Uninstall.exe"
  RMDir "$INSTDIR"
  Delete "$SMPROGRAMS\${project.name}.lnk"
  Delete "$DESKTOP\${project.name}.lnk"
  DeleteRegKey HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${project.name}"
SectionEnd
