package org.attackdlc.license;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Проверка лицензии Attack DLC в клиенте.
 *
 * <p>Данные входа лаунчер оставляет в файле
 * {@code %LOCALAPPDATA%\AttackDLC\client-session.json}: uid, ник и токены
 * аккаунта. Клиент по токену читает свой профиль и свой ключ и проверяет:</p>
 *
 * <ul>
 *   <li>ключ закреплён за этим аккаунтом ({@code owner == uid});</li>
 *   <li>ключ активирован на этом ПК (hwid ключа совпадает с нашим);</li>
 *   <li>срок подписки не истёк ({@code activatedAt + days}).</li>
 * </ul>
 *
 * <p>Если хоть одно условие не выполнено — доступ закрывается: все фичи
 * выключаются, в HUD появляется причина. Проверка повторяется каждые
 * несколько минут, поэтому истёкший ключ отключит клиент сам.</p>
 */
public final class LicenseManager {

   public static final LicenseManager INSTANCE = new LicenseManager();

   private static final String API_KEY = "AIzaSyA6Lz1LK3i9cjtfOxB1AJgrM-fWliFZCtY";
   private static final String RTDB = "https://attack-dlc-default-rtdb.firebaseio.com/";
   private static final long CHECK_INTERVAL_MS = 60_000L;
   /** Сколько живёт лицензия без успешной проверки (крайний срок при обрыве сети). */
   private static final long OFFLINE_GRACE_MS = 20 * 60_000L;

   private final AtomicBoolean licensed = new AtomicBoolean(false);
   private volatile String status = "проверка лицензии…";
   private volatile String nick = "";
   private volatile boolean checked;
   private volatile String role = "";
   private volatile long expiresAt;
   private volatile Runnable statusListener;
   private volatile String lastStatus = "";
   /** Код последнего ответа сервера: 5xx — сервер недоступен, 401/403 — отказ. */
   private volatile int lastStatusCode;
   /** Момент последней успешной проверки. */
   private volatile long lastSuccessAt;

   private String uid = "";
   private String idToken = "";
   private String refreshToken = "";
   private String activatedKey = "";

   private LicenseManager() {
   }

   /** Путь файла, который оставляет лаунчер. */
   public static Path sessionFile() {
      String local = System.getenv("LOCALAPPDATA");
      if (local == null || local.isBlank()) {
         local = System.getProperty("user.home", ".");
      }
      return Path.of(local, "AttackDLC", "client-session.json");
   }

   /** Есть ли действующая лицензия. */
   public boolean isLicensed() {
      return licensed.get();
   }

   /** Проверка уже выполнялась хотя бы раз (до неё окно не показываем). */
   public boolean isChecked() {
      return checked;
   }

   /** Статус для интерфейса: «навсегда», «осталось N дней», причина отказа. */
   public String status() {
      return status;
   }

   /** Ник аккаунта из лаунчера. */
   public String nick() {
      return nick;
   }

   /** Роль аккаунта. */
   public String role() {
      return role;
   }

   /** Дата конца подписки (или «навсегда»). */
   public long expiresAt() {
      return expiresAt;
   }

   /** Подписка бессрочная (владелец или ключ «навсегда»). */
   public boolean forever() {
      return expiresAt <= 0L;
   }

   /** Осталось дней подписки; -1 — навсегда; 0 — подписки нет. */
   public int daysLeft() {
      if (expiresAt <= 0L) return -1;
      long left = expiresAt - System.currentTimeMillis();
      if (left <= 0L) return 0;
      return (int) Math.max(0, (left + 86_399_999L) / 86_400_000L);
   }

   /** Слушатель смены статуса — чтобы показать уведомление в игре. */
   public void setStatusListener(Runnable listener) {
      this.statusListener = listener;
   }

   /** Запускает проверку и периодическое обновление. */
   public void start() {
      // Инициализируем native-guard
      NativeGuard.init();
      Thread thread = new Thread(this::loop, "attack-license-check");
      thread.setDaemon(true);
      thread.start();
   }

   private void loop() {
      while (!Thread.currentThread().isInterrupted()) {
         try { check(); } catch (Exception ignored) {}
         try { Thread.sleep(CHECK_INTERVAL_MS); } catch (InterruptedException ex) { return; }
      }
   }

   /** Одна проверка: файл входа → профиль → ключ → native-verdict. */
   public void check() {
      try {
         checked = true;

         // 1. Anti-debug (native)
         if (!NativeGuard.checkAntiDebug()) {
            fail("\u043e\u0431\u043d\u0430\u0440\u0443\u0436\u0435\u043d \u043e\u0442\u043b\u0430\u0434\u0447\u0438\u043a");
            return;
         }

         // 2. Integrity (native + java)
         if (!NativeGuard.checkIntegrity()) {
            fail("\u0438\u043d\u0442\u0435\u0433\u0440\u0438\u0442\u043d\u043e\u0441\u0442\u044c \u043d\u0430\u0440\u0443\u0448\u0435\u043d\u0430");
            return;
         }

         // 3. Читаем файл сессии
         if (!readSessionFile()) {
            fail("\u043d\u0435\u0442 \u0434\u0430\u043d\u043d\u044b\u0445 \u0432\u0445\u043e\u0434\u0430");
            return;
         }
         if (idToken.isBlank()) {
            fail("\u043d\u0435\u0442 \u0442\u043e\u043a\u0435\u043d\u0430");
            return;
         }

         // 4. Проверяем профиль через Firebase
         String profile = get("users/" + uid + ".json", idToken);
         if (profile == null) {
            profile = get("users/" + uid + ".json", refreshToken());
            if (profile == null) {
               if (isServerDown()) { keepOnNetworkProblem(); return; }
               fail("\u0441\u0435\u0441\u0441\u0438\u044f \u0438\u0441\u0442\u0435\u043a\u043b\u0430");
               return;
            }
         }
         String key = pick(profile, "activatedKey");
         String roleVal = pick(profile, "role");
         this.role = roleVal == null ? "" : roleVal;

         if ("owner".equalsIgnoreCase(this.role)) {
            activatedKey = key == null ? "" : key;
            expiresAt = 0L;
            grant("\u0432\u043b\u0430\u0434\u0435\u043b\u0435\u0446");
            return;
         }

         if (key == null || key.isBlank()) {
            fail("\u043a\u043b\u044e\u0447 \u043d\u0435 \u0430\u043a\u0442\u0438\u0432\u0438\u0440\u043e\u0432\u0430\u043d");
            return;
         }
         activatedKey = key;

         // 5. Проверяем ключ
         String keyJson = get("keys/" + key + ".json", idToken);
         if (keyJson == null) {
            keyJson = get("keys/" + key + ".json", refreshToken());
            if (keyJson == null) {
               if (isServerDown()) { keepOnNetworkProblem(); return; }
               fail("\u043d\u0435 \u0443\u0434\u0430\u043b\u043e\u0441\u044c \u043f\u0440\u043e\u0432\u0435\u0440\u0438\u0442\u044c \u043a\u043b\u044e\u0447");
               return;
            }
         }

         String owner = pick(keyJson, "owner");
         if (owner == null || !owner.equals(uid)) {
            fail("\u043a\u043b\u044e\u0447 \u0437\u0430\u043a\u0440\u0435\u043f\u043b\u0451\u043d \u0437\u0430 \u0434\u0440\u0443\u0433\u0438\u043c \u0430\u043a\u043a\u0430\u0443\u043d\u0442\u043e\u043c");
            return;
         }

         String keyHwid = pick(keyJson, "hwid");
         if (keyHwid != null && !keyHwid.isBlank()
               && !keyHwid.equalsIgnoreCase(Hwid.get())) {
            fail("\u0430\u043a\u043a\u0430\u0443\u043d\u0442 \u043f\u0440\u0438\u0432\u044f\u0437\u0430\u043d \u043a \u0434\u0440\u0443\u0433\u043e\u043c\u0443 \u043a\u043e\u043c\u043f\u044c\u044e\u0442\u0435\u0440\u0443");
            return;
         }

         long days = number(keyJson, "days");
         long activatedAt = number(keyJson, "activatedAt");
         if (days <= 0) {
            expiresAt = 0L;
            grant("\u043d\u0430\u0432\u0441\u0435\u0433\u0434\u0430");
            return;
         }
         expiresAt = activatedAt + days * 86_400_000L;
         if (expiresAt <= System.currentTimeMillis()) {
            fail("\u0441\u0440\u043e\u043a \u043f\u043e\u0434\u043f\u0438\u0441\u043a\u0438 \u0438\u0441\u0442\u0435\u043a");
            return;
         }
         long left = expiresAt - System.currentTimeMillis();
         int daysLeft = (int) Math.max(0, (left + 86_399_999L) / 86_400_000L);

         // 6. Native final verdict
         if (!NativeGuard.licenseVerdict(uid, Hwid.get(), key)) {
            fail("\u043d\u0435\u0439\u0442\u0438\u0432-\u0433\u0430\u0440\u0434 \u0437\u0430\u043f\u0440\u0435\u0442\u0438\u043b \u0434\u043e\u0441\u0442\u0443\u043f");
            return;
         }

         grant("\u043e\u0441\u0442\u0430\u043b\u043e\u0441\u044c \u0434\u043d\u0435\u0439: " + daysLeft);
      } catch (Exception ex) {
         if (isServerDown()) { keepOnNetworkProblem(); return; }
         fail("\u043f\u0440\u043e\u0432\u0435\u0440\u043a\u0430 \u043d\u0435 \u0443\u0434\u0430\u043b\u0430\u0441\u044c: " + ex.getClass().getSimpleName());
      }
   }

   private void keepOnNetworkProblem() {
      if (licensed.get() && System.currentTimeMillis() - lastSuccessAt < OFFLINE_GRACE_MS) {
         status = "\u043d\u0435\u0442 \u0441\u0432\u044f\u0437\u0438 \u0441 \u0441\u0435\u0440\u0432\u0435\u0440\u043e\u043c — \u0434\u043e\u0441\u0442\u0443\u043f \u043e\u0441\u0442\u0430\u0432\u043b\u0435\u043d";
         notifyStatus();
         return;
      }
      fail("\u043d\u0435\u0442 \u0441\u0432\u044f\u0437\u0438 \u0441 \u0441\u0435\u0440\u0432\u0435\u0440\u043e\u043c \u0430\u043a\u043a\u0430\u0443\u043d\u0442\u043e\u0432");
   }

   private boolean isServerDown() {
      return lastStatusCode >= 500;
   }

   private boolean readSessionFile() {
      Path file = sessionFile();
      if (!Files.isRegularFile(file)) return false;
      try {
         String text = Files.readString(file, StandardCharsets.UTF_8);
         uid = pick(text, "uid");
         nick = pick(text, "nick") == null ? "" : pick(text, "nick");
         idToken = pick(text, "idToken") == null ? "" : pick(text, "idToken");
         refreshToken = pick(text, "refreshToken") == null ? "" : pick(text, "refreshToken");
         return uid != null && !uid.isBlank();
      } catch (Exception ex) {
         return false;
      }
   }

   private String get(String path, String token) {
      if (token == null || token.isBlank()) return null;
      try {
         var request = java.net.http.HttpRequest
               .newBuilder(java.net.URI.create(RTDB + path + "?auth=" + token))
               .timeout(java.time.Duration.ofSeconds(20))
               .GET().build();
         var response = java.net.http.HttpClient.newHttpClient()
               .send(request, java.net.http.HttpResponse.BodyHandlers.ofString());
         lastStatusCode = response.statusCode();
         if (response.statusCode() != 200) return null;
         return response.body();
      } catch (Exception ex) {
         lastStatusCode = 503;
         return null;
      }
   }

   private String refreshToken() {
      if (refreshToken == null || refreshToken.isBlank()) return "";
      try {
         String body = "grant_type=refresh_token&refresh_token="
               + java.net.URLEncoder.encode(refreshToken, StandardCharsets.UTF_8);
         var request = java.net.http.HttpRequest
               .newBuilder(java.net.URI.create(
                     "https://securetoken.googleapis.com/v1/token?key=" + API_KEY))
               .timeout(java.time.Duration.ofSeconds(20))
               .header("Content-Type", "application/x-www-form-urlencoded")
               .POST(java.net.http.HttpRequest.BodyPublishers.ofString(body))
               .build();
         var response = java.net.http.HttpClient.newHttpClient()
               .send(request, java.net.http.HttpResponse.BodyHandlers.ofString());
         if (response.statusCode() != 200) return "";
         String token = pick(response.body(), "id_token");
         if (token == null || token.isBlank()) token = pick(response.body(), "idToken");
         if (token != null && !token.isBlank()) {
            idToken = token;
            return token;
         }
      } catch (Exception ignored) {}
      return "";
   }

   private void grant(String message) {
      status = message;
      lastSuccessAt = System.currentTimeMillis();
      notifyStatus();
      if (licensed.compareAndSet(false, true)) {
         System.out.println("[AttackDLC] license active (" + message + ")");
      }
   }

   private void fail(String reason) {
      status = reason;
      notifyStatus();
      if (licensed.compareAndSet(true, false)) {
         System.out.println("[AttackDLC] license denied: " + reason);
         disableFeatures();
      }
   }

   private void disableFeatures() {
      try {
         var manager = org.attackdlc.feature.FeatureManager.INSTANCE;
         for (var feature : manager.getFeatures()) {
            if (feature.isEnabled()) feature.setEnabled(false);
         }
      } catch (Throwable ignored) {}
   }

   private void notifyStatus() {
      String current = status;
      if (current.equals(lastStatus)) return;
      lastStatus = current;
      Runnable listener = statusListener;
      if (listener != null) {
         try { listener.run(); } catch (Throwable ignored) {}
      }
   }

   private static String pick(String json, String field) {
      if (json == null) return null;
      try {
         var node = com.google.gson.JsonParser.parseString(json).getAsJsonObject();
         if (!node.has(field) || node.get(field).isJsonNull()) return null;
         return node.get(field).getAsString();
      } catch (Exception ex) { return null; }
   }

   private static long number(String json, String field) {
      if (json == null) return 0L;
      try {
         var node = com.google.gson.JsonParser.parseString(json).getAsJsonObject();
         if (!node.has(field) || node.get(field).isJsonNull()) return 0L;
         return node.get(field).getAsLong();
      } catch (Exception ex) { return 0L; }
   }
}
