package org.attackdlc.mixin.player;

import net.minecraft.client.multiplayer.ClientPacketListener;
import org.attackdlc.feature.impl.misc.PanicFeature;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Блокирует отправку чата при панике.
 * <p>Перехватывает {@code ClientPacketListener#sendChat}, который используется
 * Baritone для отправки своих команд (через {@code #}). Обычный чат без
 * baritone-префикса также блокируется — во время паники чат не нужен.
 */
@Mixin(ClientPacketListener.class)
public abstract class ClientPacketListenerMixin {

    @Inject(method = "sendChat", at = @At("HEAD"), cancellable = true)
    private void onSendChat(String message, CallbackInfo ci) {
        if (PanicFeature.isPanicked()) {
            ci.cancel();
        }
    }
}
