package org.wallentines.midnightnpcs.mixin;

import org.spongepowered.asm.mixin.Unique;
import org.wallentines.midnightnpcs.api.npc.NPC;
import org.wallentines.midnightnpcs.api.npc.NPCSelector;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ServerPlayer.class)
public class MixinServerPlayer implements NPCSelector {

    @Unique
    private NPC selectedNPC;

    @Override
    public NPC getSelectedNPC() {
        return selectedNPC;
    }

    @Override
    public void setSelectedNPC(NPC n) {
        selectedNPC = n;
    }
}
