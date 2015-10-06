#include "build.iss"

[Setup]
AppID={{87675B24-5E67-45CF-A769-EE93FD8C4B28}
AppName={#AppName}
AppVersion={#AppVersion}
AppVerName={#AppName} {#AppVersion}
AppPublisher={#AppVendor}
AppPublisherURL={#AppHomepage}
ArchitecturesAllowed={#AppArch}
ArchitecturesInstallIn64BitMode={#AppArch}
DefaultDirName={pf}\{#AppName}
DisableProgramGroupPage=yes
LicenseFile=license.txt
OutputDir=..\..
OutputBaseFilename={#AppFile}-{#AppVersion}-{#AppPlatform}
Compression=lzma2/Normal
SolidCompression=false
ChangesAssociations=true
ShowLanguageDialog=auto
AppCopyright={#AppVendor}
SetupIconFile=lookingglass-installer.ico
UninstallIconFile=lookingglass-installer.ico
UninstallDisplayIcon={app}\{#AppName}.exe
WizardImageFile=wizard.bmp
WizardSmallImageFile=wizard-small.bmp
WizardImageBackColor=clWhite
WizardImageStretch=false

[Languages]
Name: "english"; MessagesFile: "compiler:Default.isl"
Name: "brazilianportuguese"; MessagesFile: "compiler:Languages\BrazilianPortuguese.isl"
Name: "catalan"; MessagesFile: "compiler:Languages\Catalan.isl"
Name: "czech"; MessagesFile: "compiler:Languages\Czech.isl"
Name: "danish"; MessagesFile: "compiler:Languages\Danish.isl"
Name: "dutch"; MessagesFile: "compiler:Languages\Dutch.isl"
Name: "finnish"; MessagesFile: "compiler:Languages\Finnish.isl"
Name: "french"; MessagesFile: "compiler:Languages\French.isl"
Name: "german"; MessagesFile: "compiler:Languages\German.isl"
Name: "hebrew"; MessagesFile: "compiler:Languages\Hebrew.isl"
Name: "hungarian"; MessagesFile: "compiler:Languages\Hungarian.isl"
Name: "italian"; MessagesFile: "compiler:Languages\Italian.isl"
Name: "japanese"; MessagesFile: "compiler:Languages\Japanese.isl"
Name: "norwegian"; MessagesFile: "compiler:Languages\Norwegian.isl"
Name: "polish"; MessagesFile: "compiler:Languages\Polish.isl"
Name: "portuguese"; MessagesFile: "compiler:Languages\Portuguese.isl"
Name: "russian"; MessagesFile: "compiler:Languages\Russian.isl"
Name: "slovenian"; MessagesFile: "compiler:Languages\Slovenian.isl"
Name: "spanish"; MessagesFile: "compiler:Languages\Spanish.isl"

[Tasks]
Name: "desktopicon"; Description: "{cm:CreateDesktopIcon}"; GroupDescription: "{cm:AdditionalIcons}"; 

[Files]
; NOTE: Don't use "Flags: ignoreversion" on any shared system files
Source: ..\staging\*; DestDir: {app}; Flags: IgnoreVersion recursesubdirs;

[Icons]
Name: "{commonprograms}\{#AppName}"; Filename: "{app}\{#AppName}.exe"
Name: "{commondesktop}\{#AppName}"; Filename: "{app}\{#AppName}.exe"; Tasks: desktopicon

[Run]
Filename: "{app}\{#AppName}.exe"; Description: "{cm:LaunchProgram,{#AppName}}"; Flags: nowait postinstall skipifsilent

[Registry]
; Looking Glass Project handler
Root: HKCR; SubKey: ".lgp"; ValueType: string; ValueName: ""; ValueData: "LookingGlassProject"; Flags: UninsDeleteValue;
Root: HKCR; SubKey: "LookingGlassProject"; ValueType: string; ValueName: ""; ValueData: "{#AppName} Project"; Flags: UninsDeleteValue;
Root: HKCR; Subkey: "LookingGlassProject\DefaultIcon"; ValueType: string; ValueName: ""; ValueData: "{app}\application\lookingglass-project.ico"
Root: HKCR; Subkey: "LookingGlassProject\shell\open\command"; ValueType: string; ValueName: ""; ValueData: """{app}\{#AppName}.exe"" ""%1"""

