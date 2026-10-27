package org.attackdlc.feature.impl.combat;

import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import org.attackdlc.context.MinecraftContext;
import org.attackdlc.event.EventTarget;
import org.attackdlc.event.events.packet.PacketReceiveEvent;
import org.attackdlc.feature.Feature;
import org.attackdlc.feature.FeatureCategory;
import sdk.api.invoke.api.invoke;
import sdk.api.invoke.impl.PackerMode;
import sdk.api.invoke.impl.PackerType;

@invoke(ivirtualiz = PackerType.MUTATION, imode = PackerMode.BLOCK)
public final class VelocityFeature extends Feature implements MinecraftContext {
   public VelocityFeature() {
      super("Velocity", "Removes knockback", FeatureCategory.COMBAT, -1);
   }

   @EventTarget
   public void onPacketReceive(PacketReceiveEvent event) {
      if (event.getPhase() == PacketReceiveEvent.Phase.PRE && this.player() != null) {
         if (event.getPacket() instanceof ClientboundSetEntityMotionPacket packet && packet.id() == this.player().getId()) {
            event.cancel();
         }
      }
   }
}

