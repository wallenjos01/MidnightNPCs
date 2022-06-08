package org.wallentines.midnightnpcs.fabric.mixin;

import org.wallentines.midnightnpcs.fabric.entity.NPCEntity;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;

@Mixin(ServerEntity.class)
public class MixinServerEntity {

    @Shadow @Final private Entity entity;

    @Inject(method="sendPairingData", at=@At(value="INVOKE", target="Lnet/minecraft/world/entity/Entity;getAddEntityPacket()Lnet/minecraft/network/protocol/Packet;"))
    private void onSendData(Consumer<Packet<?>> consumer, CallbackInfo ci) {
        if(entity instanceof NPCEntity) {
            ((NPCEntity) entity).onPreSpawn(consumer);
        }
    }

    @Inject(method="sendPairingData", at=@At(value="RETURN"))
    private void afterSendData(Consumer<Packet<?>> consumer, CallbackInfo ci) {
        if(entity instanceof NPCEntity) {

            ((NPCEntity) entity).onPostSpawn(consumer);
        }
    }

}
