package org.attackdlc.feature.impl.visual;

import org.attackdlc.feature.Feature;
import org.attackdlc.feature.FeatureCategory;
import sdk.api.optimize.optimize;

@optimize
public final class ItemPhysicsFeature extends Feature {
   public ItemPhysicsFeature() {
      super("ItemPhysics", "Dropped items lie on the ground", FeatureCategory.VISUAL, -1);
   }
}

