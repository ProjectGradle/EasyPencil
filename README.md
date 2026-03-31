# EasyPencil
Lightweight 2D pencil drawing tool built with Java, LWJGL, and OpenGL

![](https://github.com/ProjectGradle/EasyPencil/blob/aum_debug_viewMode/app/src/main/resources/asset/logo_board.png?raw=true)

โปรแกรมวาดบนจอคอมพิวเตอร์ เครื่องมือผู้ช่วยสำหรับการวาดเส้น เขียนข้อความ พิมพ์ แก้ไข เปลี่ยนสีและลบได้ เหมาะสำหรับอาจารย์หรือคุณครูที่ต้องการหาเครื่องมือประกอบการสอนเพื่อประสิทธิภาพการสอนที่ดีมากยิ่งขึ้น ตัวโปรแกรมมีรูปแบบการใช้ที่ง่ายและสะดวกสบาย

---

### 📥 การติดตั้ง (Installation)

<p align="center">
  <a href="Installer/EasyPencil_Setup.exe?raw=true">
    <img src="https://img.shields.io/badge/Download-EasyPencil_Installer-blue?style=for-the-badge&logo=windows" alt="Download EasyPencil">
  </a>
</p>

<p align="center">
  <i>คลิกที่ปุ่มด้านบนเพื่อดาวน์โหลดตัวติดตั้ง (.exe)</i>
</p>

## Install Environment (สำหรับ Developer)

1. Download [Java JDK](https://www.oracle.com/java/technologies/downloads/)
2. Open VSCode and open a new terminal:
```
set JAVA_HOME=C:\Program Files\Java\jdk-26
set PATH=%JAVA_HOME%\bin;%PATH%
```
3. Run:
```
gradlew clean run
```

## Build

Windows:
```
gradlew createExe
```

Mac/Linux:
```
gradlew createExe
```

## Build (`/build/jpackage/EasyPencil/.exe`)
```
gradlew packageExe
```


# How to use EasyPencil
โปรแกรมนี้เป็นแอปพลิเคชันที่พัฒนาด้วย Java และสามารถใช้งานได้ทันทีโดย ไม่ต้องติดตั้ง Java เพิ่ม 
ให้ดาวน์โหลดไฟล์ ZIP เพียงไฟล์เดียว แล้วใช้งานได้เลย

1. ไปที่หน้า Releases

2. ดาวน์โหลดไฟล์:

    EasyPencil.7z

3. แตกไฟล์ (Extract) เข้าไปในโฟลเดอร์ที่แตกไฟล์แล้ว

4. ดับเบิลคลิกที่:

    EasyPencil.exe