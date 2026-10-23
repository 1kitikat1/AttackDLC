package org.attackdlc.gift;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Управление розыгрышами подарков.
 *
 * <p>Каждый HWID может крутить рулетку раз в неделю (7 дней).
 * Шансы: 0 дн — 60%, 1 дн — 15%, 3 дн — 14%, 6 дн — 1%.</p>
 */
public final class GiftSpinManager {

    private static final long COOLDOWN_MS = 7L * 24 * 3600_000L;
    private static final String RTDB = "https://attack-dlc-default-rtdb.firebaseio.com/";

    /** Последний результат рулетки; null — ещё не крутили. */
    private static volatile Integer lastDays;
    /** Момент последнего результата. */
    private static volatile long lastResultAt;
    /** Статус для интерфейса. */
    private static volatile String statusText = "";

    private GiftSpinManager() {}

    public static String statusText() { return statusText; }
    public static Integer lastDays() { return lastDays; }
    public static long lastResultAt() { return lastResultAt; }

    // ─── проверка кулдауна ──────────────────────────────────────────

    /** Можно ли крутить сейчас? */
    public static synchronized boolean canSpin(String hwid) {
        try {
            if (hwid == null || hwid.isBlank()) return false;
            // Проверяем Firebase
            String data = fetch("gift_spins/" + sha256(hwid) + ".json");
            if (data != null && !data.isBlank() && !"null".equals(data)) {
                long lastSpin = parseLong(data, "lastSpin");
                if (lastSpin > 0 && System.currentTimeMillis() - lastSpin < COOLDOWN_MS) {
                    long remaining = COOLDOWN_MS - (System.currentTimeMillis() - lastSpin);
                    long hours = remaining / 3_600_000L;
                    statusText = "кулдаун: " + hours + "ч";
                    return false;
                }
            }
            statusText = "";
            return true;
        } catch (Exception e) {
            statusText = "ошибка проверки";
            return false;
        }
    }

    // ─── запуск рулетки ─────────────────────────────────────────────

    /**
     * Запускает спин. Результат хранится локально и пишется в Firebase.
     * Вызывать из фонового потока.
     */
    public static synchronized SpinResult spin(String hwid, String uid) {
        try {
            // Генерируем результат на клиенте (weighted random)
            int days = weightedRandom();

            // Сохраняем результат
            String now = String.valueOf(System.currentTimeMillis());
            String body = "{\"days\":" + days + ",\"lastSpin\":" + now
                    + ",\"uid\":\"" + escape(uid) + "\"}";

            String path = "gift_spins/" + sha256(hwid) + ".json";
            post(path, body);

            lastDays = days;
            lastResultAt = System.currentTimeMillis();
            statusText = days == 0 ? "не повезло :("
                    : "выпало " + days + " " + dayWord(days);

            return new SpinResult(true, days);
        } catch (Exception e) {
            statusText = "ошибка: " + e.getMessage();
            return new SpinResult(false, 0);
        }
    }

    // ─── сброс HWID (для владельцев) ───────────────────────────────

    /**
     * Сбрасывает привязку HWID ключа (сброс подписки к ПК).
     * Вызывается владельцем после оплаты.
     */
    public static boolean resetHwid(String key, String uid) {
        try {
            String body = "{\"hwid\":\"\",\"activatedAt\":0}";
            post("keys/" + key + ".json", body);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // ─── утилиты ────────────────────────────────────────────────────

    private static int weightedRandom() {
        int r = (int)(Math.random() * 10000);
        if (r < 6000) return 0;      // 60%
        if (r < 7500) return 1;      // 15%
        if (r < 8900) return 3;      // 14%
        return 6;                     // 1%
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
            var req = HttpRequest.newBuilder(
                    URI.create(RTDB + path))
                    .timeout(Duration.ofSeconds(10))
                    .GET().build();
            var resp = HttpClient.newHttpClient().send(req,
                    HttpResponse.BodyHandlers.ofString());
            return resp.statusCode() == 200 ? resp.body() : null;
        } catch (Exception e) {
            return null;
        }
    }

    private static void post(String path, String body) throws Exception {
        var req = HttpRequest.newBuilder(
                URI.create(RTDB + path))
                .timeout(Duration.ofSeconds(10))
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(body))
                .build();
        HttpClient.newHttpClient().send(req,
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

    private static String dayWord(int days) {
        int abs = Math.abs(days) % 10;
        int ten = Math.abs(days) % 100;
        if (ten >= 11 && ten <= 19) return "дней";
        if (abs == 1) return "день";
        if (abs >= 2 && abs <= 4) return "дня";
        return "дней";
    }

    public record SpinResult(boolean ok, int days) {}
}
