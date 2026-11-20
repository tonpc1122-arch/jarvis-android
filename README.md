# JARVIS Android Starter (v0.1)

โปรเจกต์เริ่มต้นสำหรับสร้าง APK ผ่าน GitHub Actions โดยไม่ต้องใช้คอมพิวเตอร์

## สิ่งที่มีในรุ่นนี้
- หน้าจอ JARVIS โทน HUD สีฟ้า
- ปุ่มกดเพื่อพูดภาษาไทย (Android Speech Recognizer)
- ตอบกลับด้วยเสียงผ่าน Android Text-to-Speech
- คำตอบตัวอย่างแบบออฟไลน์ (ยังไม่ได้เชื่อมต่อ AI API)
- Workflow สำหรับ Build APK บน GitHub Actions

## วิธี Build ผ่านมือถือ
1. สร้าง GitHub repository ใหม่ชื่อ `jarvis-android`
2. แตกไฟล์ ZIP นี้ แล้วอัปโหลดไฟล์และโฟลเดอร์ทั้งหมดเข้า repository โดยให้ `settings.gradle.kts` อยู่ที่รากโปรเจกต์
3. เปิดแท็บ **Actions** ใน repository แล้วเลือก **Build JARVIS APK** หรือรอให้ workflow เริ่มหลัง push
4. เปิดงานที่สำเร็จ (สีเขียว) แล้วดาวน์โหลด Artifact ชื่อ `JARVIS-debug-APK`
5. แตกไฟล์ artifact ZIP จะได้ `app-debug.apk` แล้วติดตั้งบน Android

## ข้อจำกัดที่ต้องทราบ
- รุ่นนี้เป็นต้นแบบที่กดปุ่มเพื่อพูด ยังไม่ใช่การฟังคำปลุกตลอดเวลา
- Speech Recognition อาจใช้บริการ Google และอินเทอร์เน็ต ขึ้นกับอุปกรณ์
- เสียงไทยขึ้นกับข้อมูลเสียง TTS ที่ติดตั้งใน Android
- ยังไม่ได้เชื่อมต่อโมเดล AI ออนไลน์ เพราะต้องเลือกผู้ให้บริการและตั้งค่า API key อย่างปลอดภัย
- การควบคุมแอปอื่นและการทำงานเบื้องหลังจะเพิ่มในรุ่นถัดไป โดยต้องขอสิทธิ์ Android ที่เหมาะสม

Package: `com.example.jarvis`
Min Android: Android 8.0 (API 26)
