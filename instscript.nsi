; example2.nsi
;
; This script is based on example1.nsi, but it remember the directory, 
; has uninstall support and (optionally) installs start menu shortcuts.
;
; It will install example2.nsi into a directory that the user selects,

;--------------------------------

; The name of the installer
Name "Directory Transfer Client"

Icon "picasso_logo_base_64.ico"

; The file to write
OutFile "dtc_installer.exe"

; The default installation directory
InstallDir C:\Picasso\DirectoryTransferClient

; Registry key to check for directory (so if you install again, it will 
; overwrite the old one automatically)
InstallDirRegKey HKLM "Software\DirectoryTransferClient" "Install_Dir"

; Request application privileges for Windows Vista
RequestExecutionLevel admin

;--------------------------------

; Pages

Page components
Page directory
Page instfiles

UninstPage uninstConfirm
UninstPage instfiles

;--------------------------------

; The stuff to install
Section "Directory Transfer Client (required)"

  SectionIn RO
  
  ; Set output path to the installation directory.
  SetOutPath $INSTDIR
  File "app.exe"
  File "properties.cfg"
  
  SetOutPath $INSTDIR\7zip
  File "7zip\7za.dll"
  File "7zip\7za.exe"
  File "7zip\7zxa.dll"
  
  SetOutPath $INSTDIR\lib
  File "target\directory-transfer-client-1.0-SNAPSHOT.jar"
  File "lib\afterburner.fx-1.7.0.jar"
  File "lib\fontawesomefx-8.9.jar"
  File "lib\gson-2.8.5.jar"
  
  ; Write the installation path into the registry
  WriteRegStr HKLM SOFTWARE\DirectoryTransferClient "Install_Dir" "$INSTDIR"
  
  ; Write the uninstall keys for Windows
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\DirectoryTransferClient" "DisplayName" "Directory Transfer Client"
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\DirectoryTransferClient" "UninstallString" '"$INSTDIR\uninstall.exe"'
  WriteRegDWORD HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\DirectoryTransferClient" "NoModify" 1
  WriteRegDWORD HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\DirectoryTransferClient" "NoRepair" 1
  WriteUninstaller "$INSTDIR\uninstall.exe"
  
SectionEnd

Section "Explorer integration"

	WriteRegStr HKCR "Folder\shell\DirectoryTransferClient" "" "Send to server"
	WriteRegStr HKCR "Folder\shell\DirectoryTransferClient\command" "" "$INSTDIR\app.exe %1"
	
SectionEnd

; Optional section (can be disabled by the user)
Section "Start Menu Shortcuts"

  CreateDirectory "$SMPROGRAMS\Directory Transfer Client"
  CreateShortcut "$SMPROGRAMS\Directory Transfer Client\Directory Transfer Client.lnk" "$INSTDIR\app.exe" "" "$INSTDIR\app.exe" 0
  CreateShortcut "$SMPROGRAMS\Directory Transfer Client\Uninstall.lnk" "$INSTDIR\uninstall.exe" "" "$INSTDIR\uninstall.exe" 0
  
SectionEnd

;--------------------------------

; Uninstaller

Section "Uninstall"
  
  ; Remove registry keys
  DeleteRegKey HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\DirectoryTransferClient"
  DeleteRegKey HKLM SOFTWARE\DirectoryTransferClient
  DeleteRegKey HKCR "Folder\shell\DirectoryTransferClient\command"
  DeleteRegKey HKCR "Folder\shell\DirectoryTransferClient"

  ; Remove files and uninstaller
  Delete $INSTDIR\7zip\7za.dll
  Delete $INSTDIR\7zip\7za.exe
  Delete $INSTDIR\7zip\7zxa.dll
  Delete $INSTDIR\lib\afterburner.fx-1.7.0.jar
  Delete $INSTDIR\lib\directory-transfer-client.jar
  Delete $INSTDIR\lib\fontawesomefx-8.9.jar
  Delete $INSTDIR\lib\gson-2.8.5.jar
  Delete $INSTDIR\app.exe
  Delete $INSTDIR\properties.cfg
  Delete $INSTDIR\logs\*.*
  Delete $INSTDIR\uninstall.exe

  ; Remove shortcuts, if any
  Delete "$SMPROGRAMS\Directory Transfer Client\*.*"

  ; Remove directories used
  RMDir "$SMPROGRAMS\Directory Transfer Client"
  RMDir "$INSTDIR\logs"
  RMDir "$INSTDIR\7zip"
  RMDir "$INSTDIR\lib"
  RMDir "$INSTDIR"

SectionEnd
