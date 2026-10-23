# 🛡️ AttackDLC — Протект (защита от кряка и слива)

## Что сделано

### 1. Native Guard (attackdlc-guard.dll)
**Путь:** `src/main/resources/cpp/attackdlc-guard.cpp` → компилируется в `win32-x86-64/attackdlc-guard.dll`

```
cl /LD /Fe:attackdlc-guard.dll attackdlc-guard.cpp
с путями: /I"%JAVA_HOME%\include" /I"%JAVA_HOME%\include\win32"
```

**Что делает:**
- `isDebugged()` — проверяет `IsDebuggerPresent`, `CheckRemoteDebuggerPresent`, `NtQueryInformationProcess(ProcessDebugPort)`
- `verifyIntegrity()` — сравнивает SHA-256 хеши ключевых классов с зашитыми эталонами
- `licenseOk()` — финальный вердикт: принимает uid/hwid/key и подтверждает валидность

**Собрано и вшито в jar** ✅

---

### 2. Integrity Check (SHA-256 хеши классов)
В `NativeGuard.java` зашиты хеши трёх ключевых классов:

| Класс | Хеш |
|---|---|
| `LicenseManager.class` | `f5e545a9...` |
| `Hwid.class` | `14a83afe...` |
| `AttackDlcClient.class` | `d643ed7d...` |

При любом патчинге класса хеш не совпадёт → `fail("интегрити нарушена")`.

**Команда для обновления хешей после изменений:**
```
.\gradlew.bat dumpClassHashes
```

---

### 3. Split License Logic
`LicenseManager.check()` разбит на **6 независимых шагов**:
1. Anti-debug (native)
2. Integrity check (native + java)
3. Чтение файла сессии
4. Проверка профиля по Firebase
5. Проверка ключа (owner/hwid/days)
6. Native final verdict

Нет одной функции `isLicensed()` — чтобы обойти, нужно патчить весь чейн.

---

### 4. No Sources in Release
```gradle
// build.gradle
// sources-jar не собирается
```
В `build/release/` лежит только боевой jar (59 MB).

---

### 5. Discord RPC — роль
Обновлён `DiscordRPCFeature.java`:
```
details: "Attack DLC | 26.2"
state:   "👑 Owner · Kitikat"   ← роль + ник
```

| Роль | Эмодзи |
|---|---|
| owner | 👑 Owner |
| user | 🔑 User |
| free | ⚡ Free |

---

### 6. ProGuard (заготовка)
В `build.gradle` добавлена задача `obfuscate`. Для включения:
```
.\gradlew.bat obfuscate
```
Keep-rules защищены: entrypoint, mixin-классы, license-пакет, feature-impl.

---

## Структура файлов протекта

```
AttackDLC/
├── src/main/
│   ├── java/org/attackdlc/
│   │   ├── license/
│   │   │   ├── LicenseManager.java    ← split-check logic
│   │   │   ├── NativeGuard.java       ← JNA обёртка + хеши
│   │   │   └── Hwid.java              ← HWID (не менялся)
│   │   └── feature/impl/player/
│   │       └── DiscordRPCFeature.java ← роль в RPC
│   └── resources/
│       └── cpp/
│           └── attackdlc-guard.cpp   ← исходник native-guard
├── build.gradle                       ← proguard + dumpClassHashes
└── PROTECT.md                         ← инструкция по сборке
```

## Build-команды

```bash
# Собрать DLL (один раз при изменении cpp)
cmd /c '"C:\Program Files\Microsoft Visual Studio\2022\Community\VC\Auxiliary\Build\vcvars64.bat" >nul 2>&1 && cl /LD /Fe:src/main/resources/win32-x86-64/attackdlc-guard.dll /I"%JAVA_HOME%\include" /I"%JAVA_HOME%\include\win32" src/main/resources/cpp/attackdlc-guard.cpp'

# Собрать jar
.\gradlew.bat clean jar

# Получить свежие хеши (после каждого изменения license-классов)
.\gradlew.bat dumpClassHashes

# Обфускация (опционально)
.\gradlew.bat obfuscate

# Релизная копия
copy build\devlibs\attackdlc-1.0-SNAPSHOT-dev.jar build\release\attackdlc.jar
```

## Что ещё можно добавить (следующие шаги)

- [ ] Шифрование payload (AES-GCM) — вынести боевой код в `.dat` файл
- [ ] Custom ClassLoader — расшифровка «just-in-time» при defineClass
- [ ] Control-flow flattening — включить в proguard.cfg
- [ ] Стриппинг строк — убрать читаемые строки из байткода
- [ ] Lease-токены (бэкенд) — сервер подписывает токен Ed25519
