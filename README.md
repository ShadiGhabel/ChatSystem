# Chat System – Java Socket Programming


---

## 📋 فهرست مطالب

* [ویژگی‌ها](#-ویژگیها)
* [نحوه اجرا](#-نحوه-اجرا)
* [دستورات موجود](#-دستورات-موجود)
* [ساختار پروژه](#-ساختار-پروژه)
* [پروتکل ارتباطی](#-پروتکل-ارتباطی)
* [مثال‌های استفاده](#-مثالهای-استفاده)
* [مدیریت خطا](#-مدیریت-خطا)
* [Concurrency و Thread Safety](#-concurrency-و-thread-safety)
* [محدودیت‌ها](#-محدودیتها)
* [نکات مهم](#-نکات-مهم)

---

## ✨ ویژگی‌ها

* ✅ **Authentication**: ثبت‌نام و ورود کاربران
* ✅ **Room Management**: ساخت، join و مدیریت اتاق‌های چت
* ✅ **Real-time Messaging**: ارسال پیام به تمام اعضای اتاق
* ✅ **File Transfer**: آپلود و دانلود فایل‌های `.txt` (<200KB)
* ✅ **Export Chat**: خروجی JSON از تاریخچه پیام‌ها
* ✅ **Multi-threaded Server**: پشتیبانی همزمان از چندین کلاینت
* ✅ **Custom Exception Handling**: مدیریت خطاهای اختصاصی

---

## 🚀 نحوه اجرا

### راه‌اندازی Server

#### روش 1: IntelliJ IDEA

1. فایل `src/server/MainServer.java` را باز کنید
2. راست‌کلیک → `Run 'MainServer.main()'`

#### روش 2: Command Line

```bash
# کامپایل
javac -d bin src/**/*.java

# اجرا
java -cp bin server.MainServer
```

خروجی:

```
Server starting on port 8080
Server is running...
```

---

### راه‌اندازی Client (چند نمونه)

#### روش 1: IntelliJ IDEA

1. `Run → Edit Configurations...`
2. گزینه **Allow multiple instances** را فعال کنید
3. فایل `src/client/MainClient.java` را چند بار اجرا کنید

#### روش 2: Command Line

```bash
# Client 1
java -cp bin client.MainClient

# Client 2
java -cp bin client.MainClient

# Client 3
java -cp bin client.MainClient
```

خروجی هر Client:

```
=== Chat Client ===
Connected!
Type /help for available commands
```

---

## 📝 دستورات موجود

### 🔐 Authentication

| دستور       | توضیحات            | مثال              |
| ----------- | ------------------ | ----------------- |
| `/register` | ثبت‌نام کاربر جدید | `/register alice` |
| `/login`    | ورود به سیستم      | `/login alice`    |


---

### 🏠 Room Management

| دستور     | توضیحات                      | مثال             |
| --------- | ---------------------------- | ---------------- |
| `/create` | ساخت اتاق جدید و join خودکار | `/create gaming` |
| `/join`   | پیوستن به اتاق موجود         | `/join gaming`   |
| `/leave`  | خروج از اتاق فعلی            | `/leave`         |
| `/rooms`  | نمایش لیست اتاق‌ها           | `/rooms`         |
| `/users`  | نمایش کاربران اتاق فعلی      | `/users`         |

---

### 💬 Messaging

| دستور     | توضیحات               |
| --------- | --------------------- |
| پیام ساده | ارسال پیام (بدون `/`) |



---

### 📁 File Transfer

| دستور       | توضیحات             | مثال                            |
| ----------- | ------------------- | ------------------------------- |
| `/upload`   | آپلود فایل `.txt`   | `/upload document.txt`          |
| `/download` | دانلود فایل از سرور | `/download <FileID> myfile.txt` |

**محدودیت‌ها:**

* فقط فایل‌های `.txt`
* حداکثر حجم: **200KB**
* مسیر ذخیره: `server_storage/`

---

### 📤 Export

| دستور          | توضیحات                  | مثال                        |
| -------------- | ------------------------ | --------------------------- |
| `/export last` | خروجی JSON از N پیام آخر | `/export last 10 chat.json` |

 `1 ≤ N ≤ 200`

نمونه خروجی JSON:

```json
{
  "room": "lobby",
  "exportedAt": "2026-01-03T12:30:00Z",
  "messages": [
    {
      "id": "uuid",
      "sender": "alice",
      "timestamp": 1704200000000,
      "type": "TEXT",
      "content": "Hello!"
    }
  ]
}
```

---

### 🛠️ Other

| دستور   | توضیحات        |
| ------- | -------------- |
| `/help` | نمایش راهنما   |
| `/exit` | خروج از برنامه |

---

## 📂 ساختار پروژه

```
ChatSystem/
├── src/
│   ├── client/
│   │   ├── MainClient.java
│   │   ├── ReceiverThread.java
│   │   └── SenderThread.java
│   ├── server/
│   │   ├── MainServer.java
│   │   ├── ClientHandler.java
│   │   ├── Room.java
│   │   ├── RoomService.java
│   │   ├── UserSession.java
│   │   └── FileStorageService.java
│   ├── common/
│   │   ├── Message.java
│   │   ├── ExportData.java
│   │   ├── FileMetadata.java
│   │   ├── FileData.java
│   │   └── exceptions/
│   │       ├── RoomNotFoundException.java
│   │       ├── DuplicateUsernameException.java
│   │       ├── FileTransferException.java
│   │       ├── InvalidCommandException.java
│   │       └── UserNotLoggedInException.java
│   └── network/
│       ├── Packet.java
│       └── PacketType.java
├── server_storage/
├── bin/
└── README.md
```

---

## 🔌 پروتکل ارتباطی

```java
class Packet<T> {
    PacketType type;   
    T payload;         
}
```

**PacketType :**

* REGISTER, LOGIN
* CREATE_ROOM, JOIN_ROOM, LEAVE_ROOM
* CHAT
* FILE_UPLOAD_REQ / FILE_UPLOAD_RES
* FILE_DOWNLOAD_REQ / FILE_DOWNLOAD_RES
* EXPORT_REQ / EXPORT_RES
* ERROR

---

## 💡 مثال‌های استفاده

### مثال 1: چت ساده

Client 1:

```
/register alice
/login alice
Hello Bob!
```

Client 2:

```
/register bob
/login bob
Hi Alice!
```

---

### مثال 2: ساخت اتاق

```
/create project-team
/users
```

خروجی:

```
alice
bob
charlie
```

---

### مثال 3: آپلود و دانلود فایل

```bash
echo "Project requirements" > requirements.txt
```

```
/upload requirements.txt
```

```
/download <FileID> my-requirements.txt
```

---

### مثال 4: Export تاریخچه

```
/export last 4 chat-history.json
```

---

## 🛡️ مدیریت خطا

| Exception                  | کاربرد            |
| -------------------------- | ----------------- |
| RoomNotFoundException      | اتاق پیدا نشد     |
| DuplicateUsernameException | نام کاربری تکراری |
| FileTransferException      | خطای انتقال فایل  |
| InvalidCommandException    | دستور نامعتبر     |
| UserNotLoggedInException   | کاربر لاگین نکرده |

---

## 🔧 Concurrency و Thread Safety

* **Thread-per-Client Model**
* استفاده از `synchronized`
* استفاده از `ConcurrentHashMap`

---

## 📊 محدودیت‌ها

| مورد        | محدودیت        |
| ----------- | -------------- |
| فرمت فایل   | فقط `.txt`     |
| حجم فایل    | حداکثر 200KB   |
| Export پیام | 1 تا 200       |
| نام کاربری  | یکتا و غیرخالی |
| نام اتاق    | یکتا و غیرخالی |

---

## 📝 نکات مهم

* همیشه ابتدا `register` 
* سپس `login`
* برای ارسال پیام باید داخل اتاق باشید
* `FileID` را دقیق کپی کنید
* فایل‌های export و download در root پروژه ذخیره می‌شوند

---


