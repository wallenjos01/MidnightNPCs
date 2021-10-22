package me.m1dnightninja.midnightnpcs.fabric.mixin;

import me.m1dnightninja.midnightnpcs.api.npc.NPC;
import me.m1dnightninja.midnightnpcs.api.npc.NPCSelector;
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
