package org.attackdlc.event.events.lifecycle;

import lombok.Generated;
import org.attackdlc.event.Event;
import org.attackdlc.feature.Feature;

public final class FeatureToggleEvent extends Event {
   private final Feature feature;
   private final boolean enabled;

   public FeatureToggleEvent(Feature feature, boolean enabled) {
      this.feature = feature;
      this.enabled = enabled;
   }

   public boolean isDisabled() {
      return !this.enabled;
   }

   @Generated
   public Feature getFeature() {
      return this.feature;
   }

   @Generated
   public boolean isEnabled() {
      return this.enabled;
   }
}

