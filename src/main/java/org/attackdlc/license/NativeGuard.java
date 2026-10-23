package org.attackdlc.license;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Platform;

import java.io.InputStream;
import java.security.MessageDigest;
import java.util.Base64;

/**
 * Обёртка над native-guard DLL (attackdlc-guard.dll).
 *
 * <p>Native-guard делает три вещи:
 * 1. Anti-debug / anti-dump — если обнаружен отладчик, возвращает false.
 * 2. Integrity check — проверяет хеш ключевых классов (передаётся из Java).
 * 3. License override — если Java-проверка прошла, но native говорит "false",
 *    доступ закрывается.
 *
 * <p>Если DLL не загружена — работает только Java-часть (fallback).
 * DLL должна лежать в папке с игрой или в java.io.tmpdir/attackdlc-natives/.
 */
public final class NativeGuard {

    public interface Guard extends Library {
        /** Проверка: есть ли отладчик / подозрительный агент. */
        boolean isDebugged();
        /** Проверка целостности: хеши переданных классов. */
        boolean verifyIntegrity(String[] classNames, String[] expectedHashes);
        /** Финальный вердикт лицензии: true = разрешить, false = запретить. */
        boolean licenseOk(String uid, String hwid, String key);
    }

    private static Guard guard;
    private static volatile boolean dllLoaded;

    // Эталонные хеши ключевых классов — генерируются одной командой:
    //   gradlew dumpClassHashes
    private static final String HASH_LICENSE_MANAGER = "f5e545a934d5a1b3cfc566f50c2ad3f535280dcff5755c65e7a2d0d9029a6f89";
    private static final String HASH_HWID            = "14a83afea75c2c19d26aa5b668fbdbccfeddf6b7b2c0d7dc3b0d03ea90436e9c";
    private static final String HASH_CLIENT          = "d643ed7da01122a6c402f0cc095b1b04476649418f5a5bc3611ca4003d8f8625";

    private NativeGuard() {}

    /** Загружает DLL один раз. */
    public static synchronized void init() {
        if (dllLoaded) return;
        dllLoaded = true;
        try {
            // Всегда извлекаем свежую DLL из jar — старые кешированные версии могут
            // содержать устаревшие хеши integrity check, из-за чего проверка ломается.
            extractGuard();
            String tmp = System.getProperty("java.io.tmpdir");
            String path = tmp + java.io.File.separator + "attackdlc-guard.dll";
            if (new java.io.File(path).exists()) {
                guard = (Guard) Native.load(path, Guard.class);
                System.out.println("[AttackDLC] native-guard loaded OK");
            } else {
                System.err.println("[AttackDLC] native-guard DLL not found — Java-only mode");
            }
        } catch (Throwable t) {
            System.err.println("[AttackDLC] native-guard init failed: " + t.getMessage());
            guard = null;
        }
    }

    private static void extractGuard() {
        try {
            String arch = Platform.is64Bit() ? "win32-x86-64" : "win32-x86";
            String resPath = "/" + arch + "/attackdlc-guard.dll";
            InputStream is = NativeGuard.class.getResourceAsStream(resPath);
            if (is == null) return;
            java.io.File dst = new java.io.File(
                    System.getProperty("java.io.tmpdir"), "attackdlc-guard.dll");
            java.io.OutputStream os = new java.io.FileOutputStream(dst);
            byte[] buf = new byte[8192];
            int n;
            while ((n = is.read(buf)) != -1) os.write(buf, 0, n);
            os.close();
            is.close();
            System.out.println("[AttackDLC] native-guard extracted → " + dst.getAbsolutePath());
        } catch (Throwable ignored) {}
    }

    /** True если DLL загружена и антидаг не обнаружен. */
    public static boolean checkAntiDebug() {
        if (!dllLoaded) init();
        if (guard == null) return true; // fallback — пропускаем
        return !guard.isDebugged();
    }

    /**
     * Проверка хешей ключевых классов.
     * <p>Сравнивает текущий SHA-256 классов с эталонными, зашитыми в бинарь.
     * Если хеш не совпадает — класс был пересобран/запатчен.</p>
     */
    public static boolean checkIntegrity() {
        if (!dllLoaded) init();
        if (guard != null) {
            String[] names = {
                "org.attackdlc.license.LicenseManager",
                "org.attackdlc.license.Hwid",
                "org.attackdlc.AttackDlcClient"
            };
            String[] hashes = {
                HASH_LICENSE_MANAGER,
                HASH_HWID,
                HASH_CLIENT
            };
            return guard.verifyIntegrity(names, hashes);
        }
        // Fallback: чистая Java-проверка
        try {
            String h1 = classHash("org/attackdlc/license/LicenseManager.class");
            String h2 = classHash("org/attackdlc/license/Hwid.class");
            String h3 = classHash("org/attackdlc/AttackDlcClient.class");
            if (h1 == null || h2 == null || h3 == null) return true; // не нашли — пропускаем
            return h1.equals(HASH_LICENSE_MANAGER) &&
                   h2.equals(HASH_HWID) &&
                   h3.equals(HASH_CLIENT);
        } catch (Throwable ignored) { return true; }
    }

    /** Финальный вердикт: native + Java. */
    public static boolean licenseVerdict(String uid, String hwid, String key) {
        if (!dllLoaded) init();
        if (guard == null) return true; // без DLL доверяем Java
        return guard.licenseOk(uid, hwid, key);
    }

    /** SHA-256 хеш класса из ресурсов jar (для integrity check). */
    private static String classHash(String resourcePath) {
        try (InputStream is = NativeGuard.class.getResourceAsStream(resourcePath)) {
            if (is == null) return null;
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] buf = new byte[8192];
            int n;
            while ((n = is.read(buf)) != -1) md.update(buf, 0, n);
            byte[] digest = md.digest();
            return Base64.getEncoder().encodeToString(digest);
        } catch (Throwable ignored) { return null; }
    }
}
