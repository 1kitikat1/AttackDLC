package org.attackdlc.menu.i18n;

import org.attackdlc.menu.core.MenuConfigStore;

public enum UiLanguage {
   ENGLISH("english", "English"),
   RUSSIAN("russian", "Russian");

   private static final UiLanguage[] VALUES = values();
   private static UiLanguage current = RUSSIAN;
   private final String storageKey;
   private final String canonicalName;

   UiLanguage(String storageKey, String canonicalName) {
      this.storageKey = storageKey;
      this.canonicalName = canonicalName;
   }

   public String storageKey() {
      return this.storageKey;
   }

   public String canonicalName() {
      return this.canonicalName;
   }

   public static UiLanguage current() {
      return current;
   }

   public static void set(UiLanguage language) {
      current = language == null ? ENGLISH : language;
   }

   public static void loadSaved() {
      String stored = MenuConfigStore.getString("language", "");
      if (!stored.isBlank()) {
         set(byStorageKey(stored));
      } else {
         set(byIndex(MenuConfigStore.getInt("languageMode", 1)));
      }
   }

   public static UiLanguage byIndex(int index) {
      return index >= 0 && index < VALUES.length ? VALUES[index] : ENGLISH;
   }

   public static UiLanguage byStorageKey(String key) {
      for (UiLanguage language : VALUES) {
         if (language.storageKey.equalsIgnoreCase(key)) {
            return language;
         }
      }

      return ENGLISH;
   }

   public static String[] canonicalNames() {
      String[] names = new String[VALUES.length];

      for (int i = 0; i < VALUES.length; i++) {
         names[i] = VALUES[i].canonicalName;
      }

      return names;
   }

   // $VF: synthetic method
   private static UiLanguage[] $values() {
      return new UiLanguage[]{ENGLISH, RUSSIAN};
   }
}
