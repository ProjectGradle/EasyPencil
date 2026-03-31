import os
import sys
import json
import zipfile
import subprocess
import winreg
import ctypes
from pathlib import Path
import tkinter as tk
from tkinter import filedialog, messagebox, ttk
import threading

APP_NAME        = "EasyPencil"      
EXE_FILENAME    = "EasyPencil.exe" 
ZIP_NAME        = "EasyPencil.zip" 
DEFAULT_DIR     = r"C:\Program Files\EasyPencil"
REGISTRY_KEY    = r"Software\EasyPencil" 


def get_bundled_zip() -> Path:
    if getattr(sys, "frozen", False):
        base = Path(sys._MEIPASS)
    else:
        base = Path(__file__).parent
    return base / ZIP_NAME


def get_installed_path() -> Path | None:
    try:
        key = winreg.OpenKey(winreg.HKEY_CURRENT_USER, REGISTRY_KEY)
        value, _ = winreg.QueryValueEx(key, "InstallPath")
        winreg.CloseKey(key)
        p = Path(value) / EXE_FILENAME
        return p if p.exists() else None
    except FileNotFoundError:
        return None


def save_installed_path(install_dir: Path):
    key = winreg.CreateKey(winreg.HKEY_CURRENT_USER, REGISTRY_KEY)
    winreg.SetValueEx(key, "InstallPath", 0, winreg.REG_SZ, str(install_dir))
    winreg.CloseKey(key)


def create_shortcut(target: Path, name: str, location: str = "desktop"):
    try:
        from win32com.client import Dispatch
        shell = Dispatch("WScript.Shell")

        if location == "desktop":
            folder = Path(shell.SpecialFolders("Desktop"))
        else:
            folder = Path(shell.SpecialFolders("Programs"))

        link_path = folder / f"{name}.lnk"
        shortcut = shell.CreateShortCut(str(link_path))
        shortcut.Targetpath = str(target)
        shortcut.WorkingDirectory = str(target.parent)
        shortcut.Description = name
        shortcut.save()
        return True
    except Exception as e:
        print(f"[shortcut error] {e}")
        return False


def launch_app(exe_path: Path):
    subprocess.Popen([str(exe_path)], cwd=str(exe_path.parent))
    sys.exit(0)


class InstallerGUI(tk.Tk):
    def __init__(self, zip_path: Path):
        super().__init__()
        self.zip_path = zip_path
        self.title(f"{APP_NAME} — ติดตั้งโปรแกรม")
        self.resizable(False, False)
        self.configure(bg="#1e1e2e")
        self._center_window(480, 300)

        tk.Label(self, text=f"ติดตั้ง {APP_NAME}",
                 font=("Segoe UI", 18, "bold"),
                 fg="#cdd6f4", bg="#1e1e2e").pack(pady=(24, 4))

        tk.Label(self, text="เลือกโฟลเดอร์ที่ต้องการติดตั้ง",
                 font=("Segoe UI", 10),
                 fg="#a6adc8", bg="#1e1e2e").pack()

        frame = tk.Frame(self, bg="#1e1e2e")
        frame.pack(pady=16, padx=32, fill="x")

        self.path_var = tk.StringVar(value=DEFAULT_DIR)
        entry = tk.Entry(frame, textvariable=self.path_var,
                         font=("Segoe UI", 10), width=38,
                         bg="#313244", fg="#cdd6f4",
                         insertbackground="#cdd6f4",
                         relief="flat", bd=6)
        entry.pack(side="left", fill="x", expand=True)

        tk.Button(frame, text="เลือก…",
                  command=self._browse,
                  bg="#585b70", fg="#cdd6f4",
                  font=("Segoe UI", 9),
                  relief="flat", padx=10, pady=4,
                  cursor="hand2").pack(side="left", padx=(8, 0))

        self.desktop_var   = tk.BooleanVar(value=True)
        self.startmenu_var = tk.BooleanVar(value=True)

        opt_frame = tk.Frame(self, bg="#1e1e2e")
        opt_frame.pack(pady=4)
        for text, var in [("สร้าง shortcut บน Desktop", self.desktop_var),
                          ("สร้าง shortcut ใน Start Menu", self.startmenu_var)]:
            tk.Checkbutton(opt_frame, text=text, variable=var,
                           bg="#1e1e2e", fg="#a6adc8",
                           selectcolor="#313244",
                           activebackground="#1e1e2e",
                           font=("Segoe UI", 9)).pack(anchor="w")

        self.progress = ttk.Progressbar(self, mode="indeterminate", length=300)
        self.status_label = tk.Label(self, text="",
                                     font=("Segoe UI", 9),
                                     fg="#a6adc8", bg="#1e1e2e")

        self.install_btn = tk.Button(self, text=f"ติดตั้ง {APP_NAME}",
                                     command=self._start_install,
                                     bg="#89b4fa", fg="#1e1e2e",
                                     font=("Segoe UI", 11, "bold"),
                                     relief="flat", padx=20, pady=8,
                                     cursor="hand2")
        self.install_btn.pack(pady=(8, 20))

    def _center_window(self, w, h):
        self.update_idletasks()
        x = (self.winfo_screenwidth() - w) // 2
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
            messagebox.showerror("สิทธิ์ไม่เพียงพอ",
                                 f"ไม่สามารถสร้างโฟลเดอร์ {install_dir}\n"
                                 "ลองเลือกโฟลเดอร์อื่น เช่น C:\\Users\\<ชื่อ>\\AppData")
            return

        self.install_btn.config(state="disabled")
        self.progress.pack(pady=4)
        self.status_label.pack()
        self.progress.start()
        self._center_window(480, 340)

        threading.Thread(target=self._do_install,
                         args=(install_dir,), daemon=True).start()

    def _do_install(self, install_dir: Path):
        try:
            self.status_label.config(text="กำลังแตกไฟล์…")
            with zipfile.ZipFile(self.zip_path, "r") as z:
                z.extractall(install_dir)

            save_installed_path(install_dir)

            exe_path = install_dir / EXE_FILENAME
            if self.desktop_var.get():
                self.status_label.config(text="กำลังสร้าง shortcut บน Desktop…")
                create_shortcut(exe_path, APP_NAME, "desktop")
            if self.startmenu_var.get():
                self.status_label.config(text="กำลังสร้าง shortcut ใน Start Menu…")
                create_shortcut(exe_path, APP_NAME, "startmenu")

            # 4. เปิดโปรแกรม
            self.status_label.config(text="ติดตั้งสำเร็จ! กำลังเปิดโปรแกรม…")
            self.after(800, lambda: launch_app(exe_path))

        except Exception as e:
            self.progress.stop()
            messagebox.showerror("เกิดข้อผิดพลาด", str(e))
            self.install_btn.config(state="normal")


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
                             "กรุณาวางไฟล์ zip ไว้ในโฟลเดอร์เดียวกันกับ launcher")
        sys.exit(1)

    app = InstallerGUI(zip_path)
    app.mainloop()


if __name__ == "__main__":
    main()