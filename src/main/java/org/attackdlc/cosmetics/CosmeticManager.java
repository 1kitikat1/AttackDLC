package org.attackdlc.cosmetics;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import sdk.api.optimize.optimize;

/**
 * Загрузка каталога косметики из assets/attackdlc/cosmetics/catalog.json
 */
@optimize
public final class CosmeticManager {

    public record Entry(String name, String displayName, String category,
                        float scale, float x, float y, float z,
                        boolean animated, String attach) {
        public String id() {
            return category + "/" + name;
        }
    }

    private static final Map<String, Entry> ENTRIES = new LinkedHashMap<>();
    private static final Map<String, List<Entry>> BY_CATEGORY = new LinkedHashMap<>();
    private static boolean loaded;
    private static boolean loadErrorLogged;

    private CosmeticManager() {
    }

    public static synchronized void ensureLoaded() {
        if (loaded) return;
        Minecraft client = Minecraft.getInstance();
        if (client == null || client.getResourceManager() == null) return;

        try {
            InputStream stream = client.getResourceManager().open(
                    Identifier.parse("attackdlc:cosmetics/catalog.json"));

            JsonArray catalog = new Gson().fromJson(
                    new InputStreamReader(stream, StandardCharsets.UTF_8), JsonArray.class);

            ENTRIES.clear();
            BY_CATEGORY.clear();

            for (JsonElement element : catalog) {
                JsonObject obj = element.getAsJsonObject();
                String name = obj.get("name").getAsString();
                String category = obj.get("category").getAsString();
                String displayName = obj.has("displayName")
                        ? obj.get("displayName").getAsString() : name;
                float scale = obj.has("scale") ? obj.get("scale").getAsFloat() : 1f;
                float x = obj.has("x") ? obj.get("x").getAsFloat() : 0f;
                float y = obj.has("y") ? obj.get("y").getAsFloat() : 0f;
                float z = obj.has("z") ? obj.get("z").getAsFloat() : 0f;
                boolean animated = obj.has("animation") && obj.get("animation").getAsBoolean();
                String attach = obj.has("attach") ? obj.get("attach").getAsString() : defaultAttach(category);

                Entry entry = new Entry(name, displayName, category, scale, x, y, z, animated, attach);
                ENTRIES.put(entry.id(), entry);
                BY_CATEGORY.computeIfAbsent(category, k -> new ArrayList<>()).add(entry);
            }
            loaded = true;
        } catch (Exception e) {
            if (!loadErrorLogged) {
                loadErrorLogged = true;
                System.err.println("[AttackDLC] Failed to load cosmetics catalog: " + e);
            }
        }
    }

    private static String defaultAttach(String category) {
        return switch (category) {
            case "hats", "ears", "heads", "masks" -> "head";
            case "stockings" -> "stockings";
            case "pets" -> "world";
            default -> "body";
        };
    }

    public static List<String> categories() {
        ensureLoaded();
        return new ArrayList<>(BY_CATEGORY.keySet());
    }

    public static List<Entry> byCategory(String category) {
        ensureLoaded();
        return BY_CATEGORY.getOrDefault(category, List.of());
    }

    public static Entry byId(String id) {
        ensureLoaded();
        return ENTRIES.get(id);
    }
}
