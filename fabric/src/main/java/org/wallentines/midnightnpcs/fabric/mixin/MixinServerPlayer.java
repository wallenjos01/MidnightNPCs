package org.wallentines.midnightnpcs.fabric.mixin;

import org.wallentines.midnightnpcs.api.npc.NPC;
import org.wallentines.midnightnpcs.api.npc.NPCSelector;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ServerPlayer.class)
public class MixinServerPlayer implements NPCSelector {

    NPC selectedNPC;

    @Override
    public NPC getSelectedNPC() {
        return selectedNPC;
    }

    @Override
    public void setSelectedNPC(NPC n) {
        selectedNPC = n;
    }
}
