package org.wallentines.midnightnpcs.mixin;

import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ClientboundPlayerInfoUpdatePacket.Action.class)
public interface AccessorAction {
    @Accessor
    ClientboundPlayerInfoUpdatePacket.Action.Writer getWriter();
}
