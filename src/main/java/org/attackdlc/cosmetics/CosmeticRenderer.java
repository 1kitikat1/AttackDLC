package org.attackdlc.cosmetics;

import java.util.LinkedHashMap;
import java.util.Map;
import sdk.api.optimize.optimize;

/**
 * Менеджер экипированной косметики.
 * Хранит выбранные предметы и предоставляет данные для рендера.
 */
@optimize
public final class CosmeticRenderer {

    public static CosmeticRenderer Instance;

    /** category -> id */
    private final Map<String, String> equipped = new LinkedHashMap<>();
    private float globalScale = 1.0f;
    private double prevX, prevY, prevZ;
    private float prevBodyYaw;
    private boolean hadTick;

    public CosmeticRenderer() {
        Instance = this;
        CosmeticManager.ensureLoaded();
    }

    public void equip(String id) {
        CosmeticManager.Entry entry = CosmeticManager.byId(id);
        if (entry == null) return;
        equipped.remove(entry.category());
        equipped.put(entry.category(), id);
    }

    public void unequip(String id) {
        equipped.values().remove(id);
    }

    public void unequipCategory(String category) {
        equipped.remove(category);
    }

    public boolean isEquipped(String id) {
        return equipped.containsValue(id);
    }

    public String equippedIn(String category) {
        return equipped.get(category);
    }

    public void tick() {
        net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
        if (mc.player == null) {
            hadTick = false;
            return;
        }
        prevX = mc.player.getX();
        prevY = mc.player.getY();
        prevZ = mc.player.getZ();
        prevBodyYaw = mc.player.getYRot();
        hadTick = true;
    }

    public float globalScale() {
        return globalScale;
    }

    public boolean hadTick() {
        return hadTick;
    }

    public double prevX() { return prevX; }
    public double prevY() { return prevY; }
    public double prevZ() { return prevZ; }
    public float prevBodyYaw() { return prevBodyYaw; }
    public Map<String, String> equipped() { return equipped; }
}
