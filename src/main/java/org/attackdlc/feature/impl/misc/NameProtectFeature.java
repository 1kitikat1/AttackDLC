package org.attackdlc.feature.impl.misc;

import org.attackdlc.feature.Feature;
import org.attackdlc.feature.FeatureCategory;
import org.attackdlc.feature.setting.TextSetting;
import sdk.api.invoke.api.invoke;
import sdk.api.invoke.impl.PackerMode;
import sdk.api.invoke.impl.PackerType;

@invoke(ivirtualiz = PackerType.MUTATION, imode = PackerMode.BLOCK)
public final class NameProtectFeature extends Feature {
   public final TextSetting name = this.register(new TextSetting("Name", "AttackDLCUser", 32));

   public NameProtectFeature() {
      super("NameProtect", "Protects your nickname in client UI", FeatureCategory.MISC, -1);
   }
}

