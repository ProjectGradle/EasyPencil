# EasyPencil
Lightweight 2D pencil drawing tool built with Java, LWJGL, and OpenGL

![](https://github.com/ProjectGradle/EasyPencil/blob/aum_debug_viewMode/app/src/main/resources/asset/logo_board.png?raw=true)

โปรแกรมวาดบนจอคอมพิวเตอร์ เครื่่องมือผู้ช่วยสำหรับการวาดเส้น เขียนข้อความ  พิมพ์ แก้ไข เปลี่ยนสีและลบได้ เหมาะสำหรับอาจารย์หรือคุณครูที่ต้องการหาเครื่องสำหรับประกอบการสอนเพื่อประสิทธิภาพการสอนที่ดีมากยิ่งขึ้น ตัวโปรแกรมมีรูปแบบการใช้ที่ง่ายและสะดวกสะบาย

# Install Envirunment
1. Download Java JDK
2. Open VScode and new terminal :
```
set JAVA_HOME=C:\Program Files\Java\jdk-26
set PATH=%JAVA_HOME%\bin;%PATH%
```
3. run : 
```
gradlew clean run
```
# Build 
Windows: 
```
gradlew createExe
```
Mac/Linux: 
```
gradlew createExe
```


# Build (/build/jpackage/EasyPencil/.exe)
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