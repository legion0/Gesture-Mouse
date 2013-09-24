; Script generated by the Inno Setup Script Wizard.
; SEE THE DOCUMENTATION FOR DETAILS ON CREATING INNO SETUP SCRIPT FILES!

#define MyAppName "Gesture Mouse Server"
#define MyAppVersion "1.0"
#define MyAppURL "http://gesturemouse.us.to"
#include "local_settings.iss"

[Setup]
; NOTE: The value of AppId uniquely identifies this application.
; Do not use the same AppId value in installers for other applications.
; (To generate a new GUID, click Tools | Generate GUID inside the IDE.)
AppId={{8C732056-5F9F-4B24-B91C-459AA9320A5F}
AppName={#MyAppName}
AppVersion={#MyAppVersion}
;AppVerName={#MyAppName} {#MyAppVersion}
AppPublisherURL={#MyAppURL}
AppSupportURL={#MyAppURL}
AppUpdatesURL={#MyAppURL}
DefaultDirName={pf}\{#MyAppName}
DefaultGroupName={#MyAppName}
AllowNoIcons=yes
OutputBaseFilename=setup
Compression=lzma
SolidCompression=yes
SetupIconFile=C:\postpc_workspace\Gesture-Mouse\server\icon.ico

[Languages]
Name: "english"; MessagesFile: "compiler:Default.isl"

[Tasks]
Name: "desktopicon"; Description: "{cm:CreateDesktopIcon}"; GroupDescription: "{cm:AdditionalIcons}"; Flags: unchecked
Name: "quicklaunchicon"; Description: "{cm:CreateQuickLaunchIcon}"; GroupDescription: "{cm:AdditionalIcons}"; Flags: unchecked; OnlyBelowVersion: 0,6.1

[Files]
Source: "{#MyAppSrcDir}\python\*"; DestDir: "{app}\python"; Flags: ignoreversion recursesubdirs createallsubdirs
Source: "{#MyAppSrcDir}\*.py"; DestDir: "{app}"; Flags: ignoreversion
Source: "{#MyAppSrcDir}\*.ico"; DestDir: "{app}"; Flags: ignoreversion
Source: "{#MyAppSrcDir}\*.dll"; DestDir: "{app}"; Flags: ignoreversion
Source: "{#MyAppSrcDir}\keyboard\*"; DestDir: "{app}\keyboard"; Flags: ignoreversion recursesubdirs createallsubdirs
Source: "{#MyAppSrcDir}\protocol\*"; DestDir: "{app}\protocol"; Flags: ignoreversion recursesubdirs createallsubdirs
Source: "{#MyAppSrcDir}\settings\*"; DestDir: "{app}\settings"; Flags: ignoreversion recursesubdirs createallsubdirs


[Icons]
Name: "{group}\{#MyAppName}"; Filename: "{app}\python\pythonw.exe"; WorkingDir: "{app}"; Parameters: """{app}\server.py"""; IconFilename: "{app}\icon.ico"
Name: "{group}\{cm:ProgramOnTheWeb,{#MyAppName}}"; Filename: "{#MyAppURL}"
Name: "{group}\{cm:UninstallProgram,{#MyAppName}}"; Filename: "{uninstallexe}"
Name: "{commondesktop}\{#MyAppName}"; Filename: "{app}\python\pythonw.exe"; WorkingDir: "{app}"; Parameters: """{app}\server.py"""; IconFilename: "{app}\icon.ico"; Tasks: desktopicon
Name: "{userappdata}\Microsoft\Internet Explorer\Quick Launch\{#MyAppName}"; Filename: "{app}\python\pythonw.exe"; WorkingDir: "{app}"; Parameters: """{app}\server.py"""; IconFilename: "{app}\icon.ico"; Tasks: quicklaunchicon

[Run]
Filename: "{app}\python\pythonw.exe"; WorkingDir: "{app}"; Parameters: """{app}\server.py"""; Description: "{cm:LaunchProgram,{#StringChange(MyAppName, '&', '&&')}}"; Flags: nowait postinstall skipifsilent

