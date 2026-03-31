"""
Bootstrapper + Installer Launcher
----------------------------------
- ถ้ามีโปรแกรมติดตั้งแล้ว: เปิดเลย
- ถ้าไม่มี: ให้ผู้ใช้เลือก path, แตก zip, สร้าง shortcut แล้วเปิด

build:
    pyinstaller --onefile --noconsole --icon=logo_board.ico --add-data "EasyPencil.zip;." launcher.py
"""

import sys
import zipfile
import subprocess
import winreg
from pathlib import Path
import tkinter as tk
from tkinter import filedialog, messagebox, ttk
import threading
import time

# ============================================================
# ตั้งค่าตรงนี้
APP_NAME     = "EasyPencil"
ZIP_NAME     = "EasyPencil.zip"
DEFAULT_DIR  = r"C:\Users\Public\EasyPencil"  # ไม่ต้อง admin rights
REGISTRY_KEY = r"Software\EasyPencil"

# ชื่อ .exe ข้างใน zip
# ถ้าไม่แน่ใจชื่อ ให้ตั้งเป็น "" โปรแกรมจะหา .exe ให้อัตโนมัติ
EXE_FILENAME = ""
# ============================================================


def find_exe_in_dir(install_dir: Path) -> Path | None:
    """หา .exe ในโฟลเดอร์ติดตั้ง"""
    if EXE_FILENAME:
        p = install_dir / EXE_FILENAME
        return p if p.exists() else None
    for p in sorted(install_dir.rglob("*.exe")):
        return p
    return None


def get_bundled_zip() -> Path:
    """หา zip ที่ฝังมากับ exe (PyInstaller _MEIPASS)"""
    if getattr(sys, "frozen", False):
        base = Path(sys._MEIPASS)
    else:
        base = Path(__file__).parent
    return base / ZIP_NAME


def get_installed_path() -> Path | None:
    """อ่าน install path จาก Registry แล้วหา exe"""
    try:
        key = winreg.OpenKey(winreg.HKEY_CURRENT_USER, REGISTRY_KEY)
        value, _ = winreg.QueryValueEx(key, "InstallPath")
        winreg.CloseKey(key)
        return find_exe_in_dir(Path(value))
    except FileNotFoundError:
        return None


def save_installed_path(install_dir: Path):
    """บันทึก install path ลง Registry"""
    key = winreg.CreateKey(winreg.HKEY_CURRENT_USER, REGISTRY_KEY)
    winreg.SetValueEx(key, "InstallPath", 0, winreg.REG_SZ, str(install_dir))
    winreg.CloseKey(key)


def create_shortcut(target: Path, name: str, location: str = "desktop"):
    """สร้าง .lnk shortcut (ต้องมี pywin32)"""
    try:
        from win32com.client import Dispatch
        shell = Dispatch("WScript.Shell")
        folder = Path(shell.SpecialFolders(
            "Desktop" if location == "desktop" else "Programs"
        ))
        lnk = folder / f"{name}.lnk"
        sc = shell.CreateShortCut(str(lnk))
        sc.Targetpath = str(target)
        sc.WorkingDirectory = str(target.parent)
        sc.Description = name
        sc.save()
        return True
    except Exception as e:
        print(f"[shortcut error] {e}")
        return False


def launch_app(exe_path: Path):
    """
    เปิดโปรแกรมแบบ detached จาก launcher
    DETACHED_PROCESS ทำให้ process ลูกไม่ผูกกับ launcher
    """
    DETACHED_PROCESS       = 0x00000008
    CREATE_NEW_PROC_GROUP  = 0x00000200

    try:
        subprocess.Popen(
            [str(exe_path)],
            cwd=str(exe_path.parent),
            creationflags=DETACHED_PROCESS | CREATE_NEW_PROC_GROUP,
            close_fds=True,
        )
        time.sleep(0.5)  # รอให้ process เริ่มก่อนปิด
    except Exception as e:
        messagebox.showerror(
            APP_NAME,
            f"ไม่สามารถเปิดโปรแกรมได้\n\n"
            f"Path: {exe_path}\n"
            f"Error: {e}"
        )
        return
    sys.exit(0)


# ============================================================
# GUI: หน้าติดตั้ง
# ============================================================

class InstallerGUI(tk.Tk):
    def __init__(self, zip_path: Path):
        super().__init__()
        self.zip_path = zip_path
        self.title(f"ติดตั้ง {APP_NAME}")
        self.resizable(False, False)
        self.configure(bg="#1e1e2e")
        self._center_window(480, 310)

        tk.Label(self, text=f"ติดตั้ง {APP_NAME}",
                 font=("Segoe UI", 18, "bold"),
                 fg="#cdd6f4", bg="#1e1e2e").pack(pady=(24, 4))

        tk.Label(self, text="เลือกโฟลเดอร์ที่ต้องการติดตั้ง",
                 font=("Segoe UI", 10),
                 fg="#a6adc8", bg="#1e1e2e").pack()

        frame = tk.Frame(self, bg="#1e1e2e")
        frame.pack(pady=16, padx=32, fill="x")

        self.path_var = tk.StringVar(value=DEFAULT_DIR)
        tk.Entry(frame, textvariable=self.path_var,
                 font=("Segoe UI", 10), width=38,
                 bg="#313244", fg="#cdd6f4",
                 insertbackground="#cdd6f4",
                 relief="flat", bd=6).pack(side="left", fill="x", expand=True)

        tk.Button(frame, text="เลือก…", command=self._browse,
                  bg="#585b70", fg="#cdd6f4", font=("Segoe UI", 9),
                  relief="flat", padx=10, pady=4,
                  cursor="hand2").pack(side="left", padx=(8, 0))

        self.desktop_var   = tk.BooleanVar(value=True)
        self.startmenu_var = tk.BooleanVar(value=True)

        opt = tk.Frame(self, bg="#1e1e2e")
        opt.pack(pady=4)
        for text, var in [("สร้าง Shortcut บน Desktop", self.desktop_var),
                          ("สร้าง Shortcut ใน Start Menu", self.startmenu_var)]:
            tk.Checkbutton(opt, text=text, variable=var,
                           bg="#1e1e2e", fg="#a6adc8",
                           selectcolor="#313244",
                           activebackground="#1e1e2e",
                           font=("Segoe UI", 9)).pack(anchor="w")

        self.progress     = ttk.Progressbar(self, mode="indeterminate", length=320)
        self.status_label = tk.Label(self, text="", font=("Segoe UI", 9),
                                     fg="#a6adc8", bg="#1e1e2e")

        self.install_btn = tk.Button(
            self, text=f"ติดตั้ง {APP_NAME}",
            command=self._start_install,
            bg="#89b4fa", fg="#1e1e2e",
            font=("Segoe UI", 11, "bold"),
            relief="flat", padx=20, pady=8, cursor="hand2")
        self.install_btn.pack(pady=(8, 20))

    def _center_window(self, w, h):
        self.update_idletasks()
        x = (self.winfo_screenwidth()  - w) // 2
        y = (self.winfo_screenheight() - h) // 2
        self.geometry(f"{w}x{h}+{x}+{y}")

    def _browse(self):
        chosen = filedialog.askdirectory(title="เลือกโฟลเดอร์ติดตั้ง",
                                         initialdir=self.path_var.get())
        if chosen:
            self.path_var.set(chosen)

    def _start_install(self):
        install_dir = Path(self.path_var.get())
        try:
            install_dir.mkdir(parents=True, exist_ok=True)
        except PermissionError:
            messagebox.showerror(
                "สิทธิ์ไม่เพียงพอ",
                f"ไม่สามารถสร้างโฟลเดอร์:\n{install_dir}\n\n"
                "ลองเลือกโฟลเดอร์อื่น เช่น:\n"
                r"C:\Users\Public\EasyPencil")
            return

        self.install_btn.config(state="disabled")
        self.progress.pack(pady=4)
        self.status_label.pack()
        self.progress.start()
        self._center_window(480, 360)

        threading.Thread(target=self._do_install,
                         args=(install_dir,), daemon=True).start()

    def _do_install(self, install_dir: Path):
        try:
            # 1. แตก zip
            self._set_status("กำลังแตกไฟล์…")
            with zipfile.ZipFile(self.zip_path, "r") as z:
                z.extractall(install_dir)

            # 2. หา exe
            exe_path = find_exe_in_dir(install_dir)
            if not exe_path:
                raise FileNotFoundError(
                    f"ไม่พบไฟล์ .exe ใน:\n{install_dir}\n\n"
                    "ตรวจสอบว่าไฟล์ zip มี .exe อยู่ข้างใน")

            # 3. บันทึก registry
            save_installed_path(install_dir)

            # 4. สร้าง shortcut
            if self.desktop_var.get():
                self._set_status("กำลังสร้าง Shortcut บน Desktop…")
                create_shortcut(exe_path, APP_NAME, "desktop")
            if self.startmenu_var.get():
                self._set_status("กำลังสร้าง Shortcut ใน Start Menu…")
                create_shortcut(exe_path, APP_NAME, "startmenu")

            # 5. เปิดโปรแกรม
            self._set_status("ติดตั้งสำเร็จ! กำลังเปิดโปรแกรม…")
            self.after(1000, lambda: launch_app(exe_path))

        except Exception as e:
            self.progress.stop()
            messagebox.showerror("เกิดข้อผิดพลาด", str(e))
            self._set_status("")
            self.install_btn.config(state="normal")

    def _set_status(self, text: str):
        self.after(0, lambda: self.status_label.config(text=text))


# ============================================================
# main
# ============================================================

def main():
    exe_path = get_installed_path()
    if exe_path:
        launch_app(exe_path)
        return

    zip_path = get_bundled_zip()
    if not zip_path.exists():
        tk.Tk().withdraw()
        messagebox.showerror(APP_NAME,
                             f"ไม่พบไฟล์ {ZIP_NAME}\n"
                             "กรุณาวางไฟล์ zip ในโฟลเดอร์เดียวกับ launcher")
        sys.exit(1)

    app = InstallerGUI(zip_path)
    app.mainloop()


if __name__ == "__main__":
    main()