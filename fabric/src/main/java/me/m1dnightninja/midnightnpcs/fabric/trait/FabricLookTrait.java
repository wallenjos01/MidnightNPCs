package me.m1dnightninja.midnightnpcs.fabric.trait;

import me.m1dnightninja.midnightnpcs.api.npc.NPC;
import me.m1dnightninja.midnightnpcs.common.trait.LookTrait;
import me.m1dnightninja.midnightnpcs.fabric.entity.NPCEntity;
import me.m1dnightninja.midnightnpcs.fabric.npc.FabricNPC;
import net.minecraft.world.entity.player.Player;

public class FabricLookTrait extends LookTrait {

    public FabricLookTrait(NPC npc) {
        super(npc);
    }

    @Override
    protected void lookAtNearestPlayer(float lookDistance) {

        NPCEntity ent = ((FabricNPC) npc).getEntity();
        if(ent == null) return;

        Player pl = ent.level.getNearestPlayer(ent, lookDistance);
        if(pl != null) {

            ent.lookAt(pl, 60.0f, 60.0f);
            ent.setYHeadRot(ent.getYRot());
        }
    }
}
