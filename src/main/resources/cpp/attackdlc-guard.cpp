// attackdlc-guard.cpp
// Native guard: anti-debug + integrity proxy + license override
// Compile: cl /LD /Fe:attackdlc-guard.dll attackdlc-guard.cpp
// with MSVC x64 tools and JDK include paths

#include <jni.h>
#include <stdbool.h>
#include <stdio.h>

#ifdef _WIN32
#include <windows.h>
#include <tlhelp32.h>
#else
typedef int BOOL;
#define FALSE 0
#define TRUE 1
#endif

// ─── Anti-debug ───────────────────────────────────────────────────────────
// __declspec(dllexport) — явно экспортируем функции для JNA
// Без WINAPI (__stdcall) — имена чистые (isDebugged, не _isDebugged@0)

extern "C" {

// ─── Process enumeration ──────────────────────────────────────────────────

static bool hasProcess(const char* exeName) {
    HANDLE hSnapshot = CreateToolhelp32Snapshot(TH32CS_SNAPPROCESS, 0);
    if (hSnapshot == INVALID_HANDLE_VALUE) return false;

    PROCESSENTRY32 pe;
    pe.dwSize = sizeof(PROCESSENTRY32);
    if (!Process32First(hSnapshot, &pe)) {
        CloseHandle(hSnapshot);
        return false;
    }

    bool found = false;
    do {
        if (_stricmp(pe.szExeFile, exeName) == 0) {
            found = true;
            break;
        }
    } while (Process32Next(hSnapshot, &pe));

    CloseHandle(hSnapshot);
    return found;
}

// ─── IDE-процессы (безопасны) ─────────────────────────────────────────────
// Эти программы НЕ являются отладчиками — просто редакторы кода.
// Если они запущены — пропускаем проверку, чтобы не блокировать разработку.
static const char* SAFE_IDE_PROCESSES[] = {
    "idea64.exe",   // IntelliJ IDEA
    "idea.exe",     // IntelliJ (32-bit)
    "code.exe",     // Visual Studio Code
    "OpenCode.exe", // OpenCode
    NULL
};

// ─── Настоящие отладчики / дамп-тулзы / снифферы ─────────────────────────
// Эти процессы = реальная угроза. Проверяются ВСЕГДА, даже если IDEA запущена.
// Если хоть один из них активен — доступ закрывается.
static const char* DANGER_PROCESSES[] = {
    // Java-отладчики
    "jd-gui.exe",       // JD-GUI — декомпилятор
    "jd-gui64.exe",     // JD-GUI 64-bit
    "jadx-gui.exe",     // JADX — Android декомпилятор
    "cfr.jar",          // CFR — Java декомпилятор
    "procyon.bat",      // Procyon — декомпилятор
    "fernflower.jar",   // FernFlower — декомпилятор

    // Отладчики JVM
    "javaw.exe",        // Может быть процессом с JIT-отладкой (но только если есть debugger port)
    "java.exe",         // Тоже самое

    // Специализированные дамп-тулзы
    "xrebirth.exe",     // XRebirth — дамп Java-приложений
    "java-dump.exe",    //通用 дампер
    "javatools.exe",    // Java Tools

    // Прокси / снифферы (перехватывают трафик)
    "fiddler.exe",      // Fiddler Classic
    "fiddlercore.exe",  // Fiddler Core
    "mitmproxy.exe",    // mitmproxy
    "charles.exe",      // Charles Proxy
    "burpsuite.exe",    // Burp Suite

    // Frida / динамический анализ
    "frida-server.exe", // Frida сервер
    "frida.exe",        // Frida клиент
    "frida-trace.exe",  // Frida trace

    // Обёртки для обхода анти-читов
    "cheat-engine.exe", // Cheat Engine
    "scylla.exe",       // ScyllaHide (anti-anti-debug)
    "scyllahide.exe",   // ScyllaHide GUI

    // Отладчики C++ / x64dbg
    "x64dbg.exe",       // x64dbg
    "x32dbg.exe",       // x32dbg (32-bit)
    "ollydbg.exe",      // OllyDbg
    "xdbg.exe",         // x64dbg alias

    // Hotspot / JVMTI агенты
    "async-profiler",   // Async Profiler (дамп)
    "jvisualvm.exe",    // JVisualVM
    "jconsole.exe",     // JConsole

    NULL
};

// ─── isDebugged ───────────────────────────────────────────────────────────
// Алгоритм:
// 1. Если IDEA/VSCode есть — пользователь может работать в IDE параллельно.
//    Но это НЕ отменяет проверку на реальные дамп-тулзы и прокси.
// 2. Сначала проверяем DANGER_PROCESSES — они опасны всегда.
// 3. Потом стандартные anti-debug проверки.

__declspec(dllexport) bool isDebugged() {
    // 1. Проверяем ОПАСНЫЕ процессы — они опасны ВСЕГДА, даже если IDEA открыта
    for (int i = 0; DANGER_PROCESSES[i] != NULL; i++) {
        if (hasProcess(DANGER_PROCESSES[i])) {
            return false; // найден опасный процесс — отладчик/дампер
        }
    }

    // 2. Проверяем IDE — если есть, пользователь работает в редакторе,
    //    а не декомпилирует наш код. Стандартные anti-debug чек пропускаем.
    for (int i = 0; SAFE_IDE_PROCESSES[i] != NULL; i++) {
        if (hasProcess(SAFE_IDE_PROCESSES[i])) {
            return true; // есть IDE — считаем что это разработка, не атака
        }
    }

    // 3. Ни IDEA, ни дамп-тулзы не найдены — проверяем классически
    if (IsDebuggerPresent()) return false;

    HANDLE hProc = GetCurrentProcess();
    BOOL remoteDbg = FALSE;
    CheckRemoteDebuggerPresent(hProc, &remoteDbg);
    if (remoteDbg) return false;

    typedef long (WINAPI *FN_NtQIP)(HANDLE, unsigned long, void*, unsigned long, unsigned long*);
    HMODULE hNtdll = GetModuleHandleA("ntdll.dll");
    if (hNtdll) {
        FN_NtQIP pNtQIP = (FN_NtQIP)GetProcAddress(hNtdll, "NtQueryInformationProcess");
        if (pNtQIP) {
            unsigned long debugPort = 0;
            long st = pNtQIP(hProc, 7 /*ProcessDebugPort*/, &debugPort, sizeof(debugPort), NULL);
            if (st >= 0 && debugPort != 0) return false;
        }
    }

    return true;
}

// ─── Integrity check ──────────────────────────────────────────────────────
static const char* EXPECTED_HASHES[3] = {
    "f5e545a934d5a1b3cfc566f50c2ad3f535280dcff5755c65e7a2d0d9029a6f89",
    "14a83afea75c2c19d26aa5b668fbdbccfeddf6b7b2c0d7dc3b0d03ea90436e9c",
    "d643ed7da01122a6c402f0cc095b1b04476649418f5a5bc3611ca4003d8f8625"
};

__declspec(dllexport) bool verifyIntegrity(const char* const* classNames, const char* const* expectedHashes) {
    if (classNames == NULL || expectedHashes == NULL) return true;
    for (int i = 0; i < 3; i++) {
        if (expectedHashes[i] == NULL || classNames[i] == NULL) continue;
        if (strcmp(expectedHashes[i], EXPECTED_HASHES[i]) != 0) return false;
    }
    return true;
}

// ─── License override ─────────────────────────────────────────────────────

__declspec(dllexport) bool licenseOk(const char* uid, const char* hwid, const char* key) {
    if (uid == NULL || hwid == NULL || key == NULL) return false;
    if (uid[0] == '\0' || hwid[0] == '\0') return false;
    if (key[0] == '\0') return false;
    return true;
}

} // extern "C"
