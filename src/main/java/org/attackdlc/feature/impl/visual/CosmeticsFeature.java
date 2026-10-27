package org.attackdlc.feature.impl.visual;

import org.attackdlc.cosmetics.CosmeticRenderer;
import org.attackdlc.event.EventTarget;
import org.attackdlc.event.events.game.GameTickEvent;
import org.attackdlc.feature.Feature;
import org.attackdlc.feature.FeatureCategory;
import sdk.api.optimize.optimize;

/**
 * Косметика — 3D-модели на теле игрока (крылья, шляпы, питомцы и т.д.).
 * Меню выбора: вкладка Cosmetics в клик GUI.
 */
@optimize
public final class CosmeticsFeature extends Feature {

    public CosmeticsFeature() {
        super("Cosmetics", "Косметические модели: крылья, шляпы, питомцы, хвосты и др.", FeatureCategory.VISUAL, -1);
    }

    @Override
    public void onEnable() {
        if (CosmeticRenderer.Instance == null) {
            CosmeticRenderer.Instance = new CosmeticRenderer();
        }
    }

    @Override
    public void onDisable() {
        CosmeticRenderer.Instance = null;
    }

    @EventTarget
    public void onTick(GameTickEvent event) {
        if (CosmeticRenderer.Instance != null) {
            CosmeticRenderer.Instance.tick();
        }
    }
}
