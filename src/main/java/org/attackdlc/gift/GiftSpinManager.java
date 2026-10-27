package org.attackdlc.gift;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Управление розыгрышами подарков.
 *
 * <p>Шансы: 0д — 60%, 1д — 15%, 3д — 14%, 6д — 1%.
 * Кулдаун: 7 дней на HWID (хранится в Firebase).</p>
 */
public final class GiftSpinManager {

    private static final long COOLDOWN_MS = 7L * 24 * 3600_000L;
    private static final String RTDB = "https://attack-dlc-default-rtdb.firebaseio.com/";

    private static volatile Integer lastDays;
    private static volatile long    lastResultAt;
    private static volatile String  statusText = "";

    private GiftSpinManager() {}

    public static String    statusText()   { return statusText; }
    public static Integer   lastDays()     { return lastDays; }
    public static long      lastResultAt() { return lastResultAt; }

    // ─── доступность подарка (кнопка видна только раз в неделю) ─────

    private static final Path LOCAL_STATE =
            org.attackdlc.utils.ConfigIO.configDir().resolve("gift-cooldown.txt");
    private static final AtomicBoolean REFRESHING = new AtomicBoolean(false);
    /** Момент (ms), когда снова можно крутить. 0 = можно сейчас. */
    private static volatile long    nextSpinAt;
    private static volatile boolean checked;

    /** Проверить доступность (вызывать каждый кадр, дёшево). Сеть — асинхронно. */
    public static void ensureChecked() {
        if (checked) return;
        synchronized (GiftSpinManager.class) {
            if (checked) return;
            long local = readLocalLastSpin();
            nextSpinAt = local > 0 ? local + COOLDOWN_MS : 0L;
            checked = true;
            refreshAsync();
        }
    }

    /** true, если подарок ещё не открыт и его можно показывать. */
    public static boolean isAvailable() {
        return checked && System.currentTimeMillis() >= nextSpinAt;
    }

    /** Сколько ожидать до следующего спина (мс). */
    public static long msUntilNextSpin() {
        return Math.max(0L, nextSpinAt - System.currentTimeMillis());
    }

    /** Асинхронно сверяет кулдаун с Firebase (сервер — источник истины). */
    private static void refreshAsync() {
        if (!REFRESHING.compareAndSet(false, true)) return;
        String hwid;
        try {
            hwid = org.attackdlc.license.Hwid.get();
        } catch (Throwable t) {
            REFRESHING.set(false);
            return;
        }
        Thread th = new Thread(() -> {
            try {
                String data = fetch("gift_spins/" + sha256(hwid) + ".json");
                if (data != null && !data.isBlank() && !"null".equals(data)) {
                    long lastSpin = parseLong(data, "lastSpin");
                    if (lastSpin > 0) {
                        nextSpinAt = lastSpin + COOLDOWN_MS;
                        writeLocalLastSpin(lastSpin); // зеркало для оффлайна
                    } else {
                        nextSpinAt = 0L;
                    }
                }
            } finally {
                REFRESHING.set(false);
            }
        }, "attackdlc-gift-refresh");
        th.setDaemon(true);
        th.start();
    }

    /** Фиксирует спин локально — кнопка сразу исчезает на неделю. */
    private static synchronized void markSpun(long when) {
        writeLocalLastSpin(when);
        nextSpinAt = when + COOLDOWN_MS;
    }

    private static long readLocalLastSpin() {
        try {
            if (Files.exists(LOCAL_STATE)) {
                String s = Files.readString(LOCAL_STATE).trim();
                if (!s.isEmpty()) return Long.parseLong(s);
            }
        } catch (Throwable ignored) {}
        return 0L;
    }

    private static void writeLocalLastSpin(long value) {
        try {
            Files.createDirectories(LOCAL_STATE.getParent());
            Files.writeString(LOCAL_STATE, Long.toString(value),
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (Throwable ignored) {}
    }

    /**
     * Проверяет, можно ли крутить сейчас.
     * Возвращает false если кулдаун ещё не истёк.
     * statusText содержит текст для отображения (например "кулдаун: 3ч").
     */
    public static synchronized boolean canSpin(String hwid) {
        try {
            if (hwid == null || hwid.isBlank()) return false;
            String data = fetch("gift_spins/" + sha256(hwid) + ".json");
            if (data != null && !data.isBlank() && !"null".equals(data)) {
                long lastSpin = parseLong(data, "lastSpin");
                if (lastSpin > 0) {
                    long remaining = COOLDOWN_MS - (System.currentTimeMillis() - lastSpin);
                    if (remaining > 0) {
                        long hours = remaining / 3_600_000L;
                        statusText = "до следующего спина: " + hours + "ч";
                        return false;
                    }
                }
            }
            statusText = "";
            return true;
        } catch (Exception e) {
            statusText = "ошибка сети";
            return false;
        }
    }

    /**
     * Запускает спин.
     * Результат определяется на клиенте (weighted random) и сразу пишется в Firebase.
     */
    public static synchronized SpinResult spin(String hwid, String uid) {
        try {
            int days = weightedRandom();
            String body = "{\"days\":" + days
                    + ",\"lastSpin\":" + System.currentTimeMillis()
                    + ",\"uid\":\"" + escape(uid) + "\"}";
            post("gift_spins/" + sha256(hwid) + ".json", body);
            markSpun(System.currentTimeMillis()); // кнопка скрывается на неделю
            lastDays = days;
            lastResultAt = System.currentTimeMillis();
            statusText = days == 0 ? "не повезло" : "выпало " + days + " дн";
            return new SpinResult(true, days);
        } catch (Exception e) {
            statusText = "ошибка: " + e.getMessage();
            return new SpinResult(false, 0);
        }
    }

    // ─── утилиты ───────────────────────────────────────────────────

    private static int weightedRandom() {
        int r = (int)(Math.random() * 10000);
        if (r < 6000) return 0;   // 60%
        if (r < 7500) return 1;   // 15%
        if (r < 8900) return 3;   // 14%
        return 6;                  // 1%
    }

    private static String sha256(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] b = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(b.length * 2);
            for (byte bb : b) {
                sb.append(Character.forDigit((bb >> 4) & 0xF, 16));
                sb.append(Character.forDigit(bb & 0xF, 16));
            }
            return sb.toString();
        } catch (Exception e) {
            return "0".repeat(64);
        }
    }

    private static String fetch(String path) {
        try {
            var resp = HttpClient.newHttpClient().send(
                    HttpRequest.newBuilder(URI.create(RTDB + path))
                            .timeout(Duration.ofSeconds(10)).GET().build(),
                    HttpResponse.BodyHandlers.ofString());
            return resp.statusCode() == 200 ? resp.body() : null;
        } catch (Exception e) {
            return null;
        }
    }

    private static void post(String path, String body) throws Exception {
        HttpClient.newHttpClient().send(
                HttpRequest.newBuilder(URI.create(RTDB + path))
                        .timeout(Duration.ofSeconds(10))
                        .header("Content-Type", "application/json")
                        .PUT(HttpRequest.BodyPublishers.ofString(body)).build(),
                HttpResponse.BodyHandlers.ofString());
    }

    private static long parseLong(String json, String field) {
        try {
            int idx = json.indexOf("\"" + field + "\"");
            if (idx < 0) return 0;
            int colon = json.indexOf(':', idx);
            int end = json.indexOf(',', colon);
            if (end < 0) end = json.indexOf('}', colon);
            return Long.parseLong(json.substring(colon + 1, end).trim());
        } catch (Exception e) {
            return 0;
        }
    }

    private static String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    public record SpinResult(boolean ok, int days) {}
}
