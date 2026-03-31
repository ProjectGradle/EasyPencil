@echo off
chcp 65001 >nul
set APP=launcher
set ZIP=EasyPencil.zip
set ICON=logo_board.ico

echo [*] ตรวจสอบ dependencies...
pip install pyinstaller pywin32 --quiet

echo [*] Building %APP%.exe ...
pyinstaller --onefile --noconsole --name "%APP%" --icon "%ICON%" --add-data "%ZIP%;." %APP%.py

echo.
if %ERRORLEVEL% == 0 (
    echo [OK] สร้างสำเร็จ: dist\%APP%.exe
    echo      แจกจ่ายไฟล์นี้ไฟล์เดียวได้เลย (%ZIP% ถูกฝังไว้ข้างใน)
    explorer dist
) else (
    echo [ERROR] build ล้มเหลว ดู log ด้านบน
)
pause