package org.attackdlc.license;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Locale;

/**
 * Идентификатор компьютера (HWID).
 *
 * <p>Считается из системных номеров: MachineGuid Windows, серийник
 * материнской платы и идентификатор системного диска. Все составляющие
 * складываются в строку и хэшируются SHA-256 — наружу уходит только
 * короткий отпечаток.</p>
 *
 * <p>Алгоритм обязан совпадать с лаунчером: клиент сверяет свой отпечаток
 * с тем, что записан в ключе, и любое расхождение считается чужим ПК.</p>
 */
public final class Hwid {

   private static final Object LOCK = new Object();
   private static volatile String cached;

   private Hwid() {
   }

   /** Шестнадцатеричный отпечаток ПК (32 символа). */
   public static String get() {
      String value = cached;
      if (value != null) {
         return value;
      }
      synchronized (LOCK) {
         if (cached == null) {
            cached = hash(fingerprint());
         }
         return cached;
      }
   }

   /** Короткий вид для показа в интерфейсе: ABCD…WXYZ. */
   public static String shortForm() {
      String value = get();
      if (value.length() <= 12) {
         return value;
      }
      return value.substring(0, 4) + "…" + value.substring(value.length() - 4);
   }

   private static String fingerprint() {
      StringBuilder raw = new StringBuilder();
      append(raw, commandOutput("reg", "query",
            "HKLM\\SOFTWARE\\Microsoft\\Cryptography",
            "/v", "MachineGuid"));
      append(raw, commandOutput("powershell", "-NoProfile", "-NonInteractive", "-Command",
            "(Get-CimInstance Win32_BaseBoard).SerialNumber"));
      append(raw, commandOutput("powershell", "-NoProfile", "-NonInteractive", "-Command",
            "(Get-Volume -DriveLetter C).UniqueId"));
      raw.append('|').append(System.getProperty("user.name", ""));
      return raw.toString();
   }

   private static void append(StringBuilder raw, String part) {
      raw.append('|').append(part == null ? "" : part.trim().toLowerCase(Locale.ROOT));
   }

   private static String commandOutput(String... command) {
      try {
         Process process = new ProcessBuilder(command)
               .redirectErrorStream(true)
               .start();
         String out = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
         process.waitFor();
         return out;
      } catch (Exception ignored) {
         return "";
      }
   }

   private static String hash(String source) {
      try {
         MessageDigest digest = MessageDigest.getInstance("SHA-256");
         byte[] bytes = digest.digest(source.getBytes(StandardCharsets.UTF_8));
         StringBuilder hex = new StringBuilder(bytes.length * 2);
         for (byte b : bytes) {
            hex.append(Character.forDigit((b >> 4) & 0xF, 16))
                  .append(Character.forDigit(b & 0xF, 16));
         }
         return hex.toString();
      } catch (Exception ignored) {
         return "0".repeat(32);
      }
   }
}
