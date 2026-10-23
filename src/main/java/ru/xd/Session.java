package ru.xd;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Сессия аккаунта Attack DLC: кто вошёл, какая подписка, какой ПК.
 *
 * <p>Данные приходят из лаунчера: тот оставляет файл
 * {@code %LOCALAPPDATA%\AttackDLC\client-session.json} (uid, ник, токены,
 * ключ). Класс читает его и отдаёт значения тем, кто их спрашивает:
 * ватермарка, меню и Discord RPC берут отсюда ник.</p>
 *
 * <p>Раньше здесь были native-заглушки от чужого клиента (с библиотекой,
 * которой в проекте нет) — вызов падал бы с ошибкой. Теперь всё считается
 * на Java, а проверка лицензии живёт в {@code LicenseManager}.</p>
 */
public class Session {

   private static Session instance;

   private String uid = "";
   private String name = "";
   private String role = "free";
   private String token = "";
   private String refreshToken = "";
   private String activatedKey = "";
   private String hwid = "";
   private long expiresAt;
   private String modification = "";
   private String funpayUrl = "";
   private String siteUrl = "";
   private long writtenAt;

   private Session() {
      reload();
      startWatcher();
   }

   /**
    * Следит за файлом входа: лаунчер обновляет его раз в минуту и при
    * смене аккаунта — клиент подхватывает ник и роль без перезапуска.
    */
   private static void startWatcher() {
      Thread thread = new Thread(() -> {
         while (!Thread.currentThread().isInterrupted()) {
            try {
               Thread.sleep(30_000L);
            } catch (InterruptedException ex) {
               return;
            }
            try {
               getInstance().reload();
            } catch (Throwable ignored) {
            }
         }
      }, "attack-session-watcher");
      thread.setDaemon(true);
      thread.start();
   }

   public static Session getInstance() {
      if (instance == null) {
         instance = new Session();
      }
      return instance;
   }

   /** Читает файл входа лаунчера заново. */
   public synchronized void reload() {
      Path file = file();
      if (file == null || !Files.isRegularFile(file)) {
         return;
      }
      try {
         String text = Files.readString(file, StandardCharsets.UTF_8);
         var node = com.google.gson.JsonParser.parseString(text).getAsJsonObject();
         uid = str(node, "uid");
         name = str(node, "nick");
         role = str(node, "role");
         token = str(node, "idToken");
         refreshToken = str(node, "refreshToken");
         activatedKey = str(node, "activatedKey");
         hwid = str(node, "hwid");
         expiresAt = num(node, "expiresAt");
         modification = str(node, "modification");
         funpayUrl = str(node, "funpayUrl");
         siteUrl = str(node, "siteUrl");
         writtenAt = num(node, "writtenAt");
      } catch (Exception ignored) {
      }
   }

   private static Path file() {
      String local = System.getenv("LOCALAPPDATA");
      if (local == null || local.isBlank()) {
         local = System.getProperty("user.home", ".");
      }
      return Path.of(local, "AttackDLC", "client-session.json");
   }

   private static String str(com.google.gson.JsonObject node, String field) {
      try {
         if (!node.has(field) || node.get(field).isJsonNull()) {
            return "";
         }
         return node.get(field).getAsString();
      } catch (Exception ex) {
         return "";
      }
   }

   private static long num(com.google.gson.JsonObject node, String field) {
      try {
         if (!node.has(field) || node.get(field).isJsonNull()) {
            return 0L;
         }
         return node.get(field).getAsLong();
      } catch (Exception ex) {
         return 0L;
      }
   }

   public String profile(String key) {
      switch (key.toLowerCase()) {
         case "uid":
            return getUid();
         case "role":
            return getRole();
         case "name":
            return getName();
         case "subtime":
            return getSubTime();
         case "sub":
            return getSub();
         case "hwid":
            return getHwid();
         case "token":
            return getToken();
         default:
            return null;
      }
   }

   /** Отпечаток компьютера. */
   public static String getHwid() {
      return org.attackdlc.license.Hwid.get();
   }

   /** Идентификатор аккаунта в базе. */
   public static String getUid() {
      return getInstance().uid;
   }

   /** Дата конца подписки (или «навсегда»). */
   public static String getSubTime() {
      long end = getInstance().expiresAt;
      if (end <= 0L) {
         return "навсегда";
      }
      return new java.text.SimpleDateFormat("dd.MM.yyyy")
            .format(new java.util.Date(end));
   }

   /** Осталось дней подписки; -1 — навсегда. */
   public static String getSub() {
      long end = getInstance().expiresAt;
      if (end <= 0L) {
         return "-1";
      }
      long left = end - System.currentTimeMillis();
      if (left <= 0L) {
         return "0";
      }
      return String.valueOf((left + 86_399_999L) / 86_400_000L);
   }

   /** Ник аккаунта из лаунчера. */
   public static String getName() {
      return getInstance().name;
   }

   /** Роль аккаунта. */
   public static String getRole() {
      return getInstance().role;
   }

   /** Название роли для интерфейса. */
   public static String getRoleName() {
      return switch (getRole()) {
         case "owner" -> "Владелец";
         case "user" -> "Пользователь";
         default -> "Бесплатно";
      };
   }

   /** Токен аккаунта (для запросов к серверу). */
   public static String getToken() {
      return getInstance().token;
   }

   /** Refresh-токен аккаунта. */
   public static String getRefreshToken() {
      return getInstance().refreshToken;
   }

   /** Активированный ключ. */
   public static String getActivatedKey() {
      return getInstance().activatedKey;
   }

   /** Название модификации, которой играют (из лаунчера). */
   public static String getModification() {
      return getInstance().modification;
   }

   /** Ссылка на покупку ключа (из лаунчера). */
   public static String getFunpayUrl() {
      return getInstance().funpayUrl;
   }

   /** Ссылка на сайт (из лаунчера). */
   public static String getSiteUrl() {
      return getInstance().siteUrl;
   }

   /** Когда лаунчер записал файл входа (мс); 0 — неизвестно. */
   public static long getWrittenAt() {
      return getInstance().writtenAt;
   }

   /** Сколько времени прошло с записи файла входа; -1 — неизвестно. */
   public static long getAgeMinutes() {
      long at = getWrittenAt();
      if (at <= 0L) {
         return -1L;
      }
      return Math.max(0L, (System.currentTimeMillis() - at) / 60_000L);
   }
}
