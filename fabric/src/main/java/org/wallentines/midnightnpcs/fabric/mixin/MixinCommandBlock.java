package org.wallentines.midnightnpcs.fabric.mixin;

import org.wallentines.midnightnpcs.api.npc.NPC;
import org.wallentines.midnightnpcs.api.npc.NPCSelector;
import net.minecraft.world.level.block.entity.CommandBlockEntity;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(CommandBlockEntity.class)
public class MixinCommandBlock implements NPCSelector {

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
