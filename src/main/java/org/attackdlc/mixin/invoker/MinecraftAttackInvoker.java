package org.attackdlc.mixin.invoker;

import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Minecraft.class)
public interface MinecraftAttackInvoker {
   @Invoker("startAttack")
   boolean attackdlc$startAttack();
}
