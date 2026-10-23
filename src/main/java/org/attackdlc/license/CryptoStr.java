package org.attackdlc.license;

/**
 * Контейнер для зашифрованных (base64) строк.
 *
 * <p>Хранит значение в закодированном виде. При первом обращении к {@link #get()}
 * строка декодируется один раз и результат кешируется. Это мешает просто
 * посмотреть на декомпилированный код и увидеть API-ключ, хеши или URL
 * Firebase — вместо этого там будет набор случайных символов base64.</p>
 *
 * <p>Для генерации закодированного значения используйте:</p>
 * <pre>
 *   java.util.Base64.getEncoder().encodeToString("ваша строка".getBytes())
 * </pre>
 */
public final class CryptoStr {

    private final byte[] encoded;
    private volatile String cached;

    /** Создать из base64-представления исходной строки. */
    public CryptoStr(String base64) {
        this.encoded = base64.getBytes(java.nio.charset.StandardCharsets.US_ASCII);
    }

    /** Создать из raw-bytes (для внутренних нужд). */
    public CryptoStr(byte[] raw) {
        this.encoded = raw;
    }

    /** Декодировать и вернуть. Результат кешируется после первого вызова. */
    public String get() {
        String v = cached;
        if (v != null) return v;
        synchronized (this) {
            v = cached;
            if (v != null) return v;
            byte[] b;
            try {
                b = java.util.Base64.getDecoder().decode(encoded);
            } catch (IllegalArgumentException ignored) {
                // не base64 — воспринимаем как plain ASCII
                b = encoded;
            }
            cached = new String(b, java.nio.charset.StandardCharsets.UTF_8);
            return cached;
        }
    }

    /** Быстрый конструктор из уже готовой закодированной строки. */
    public static CryptoStr of(String s) {
        return new CryptoStr(
            java.util.Base64.getEncoder().encodeToString(s.getBytes(java.nio.charset.StandardCharsets.UTF_8))
        );
    }

    /** Пустой CryptoStr, если строка null/blank — get() вернёт "". */
    public static CryptoStr blankIfNull(String s) {
        if (s == null || s.isBlank()) return new CryptoStr(new byte[0]);
        return of(s);
    }
}
