[Setup]
; ข้อมูลโปรแกรม
AppName=EasyPencil
AppVersion=1.0
DefaultDirName={autopf}\EasyPencil
DefaultGroupName=EasyPencil
; ปรับแต่งชื่อไฟล์ตัวติดตั้งที่จะได้
OutputBaseFilename=EasyPencil_Setup
Compression=lzma2
SolidCompression=yes
; ป้องกันการลงซ้ำซ้อนถ้ามีโปรแกรมเปิดอยู่
CloseApplications=yes

[Tasks]
Name: "desktopicon"; Description: "{cm:CreateDesktopIcon}"; GroupDescription: "{cm:AdditionalIcons}"; Flags: unchecked

[Files]
[Files]
; 1. ไฟล์ EXE หลัก (อยู่ที่ชั้นนอกสุดของโฟลเดอร์ติดตั้ง)
Source: "C:\Users\CyberLAB04\Documents\GitHub\EasyPencil\EasyPencil\EasyPencil.exe"; DestDir: "{app}"; Flags: ignoreversion

; 2. นำเนื้อหา "ข้างใน" โฟลเดอร์ app ไปวางใน {app}\app
; ใช้ \* เพื่อบอกว่าเอาทุกอย่างข้างในไปวาง ไม่ใช่เอาตัวโฟลเดอร์ app ไปวางซ้อน
Source: "C:\Users\CyberLAB04\Documents\GitHub\EasyPencil\EasyPencil\app\*"; DestDir: "{app}\app"; Flags: ignoreversion recursesubdirs createallsubdirs

; 3. นำเนื้อหา "ข้างใน" โฟลเดอร์ runtime ไปวางใน {app}\runtime
Source: "C:\Users\CyberLAB04\Documents\GitHub\EasyPencil\EasyPencil\runtime\*"; DestDir: "{app}\runtime"; Flags: ignoreversion recursesubdirs createallsubdirs


[Icons]
; สร้าง Shortcut ที่ Start Menu
Name: "{group}\EasyPencil"; Filename: "{app}\EasyPencil.exe"
; สร้าง Shortcut ที่ Desktop
Name: "{autodesktop}\EasyPencil"; Filename: "{app}\EasyPencil.exe"; Tasks: desktopicon

[Run]
; เมื่อติดตั้งเสร็จ ให้มี Checkbox ถามว่าจะเปิดโปรแกรมเลยไหม
Filename: "{app}\EasyPencil.exe"; Description: "{cm:LaunchProgram,EasyPencil}"; Flags: nowait postinstall skipifsilent