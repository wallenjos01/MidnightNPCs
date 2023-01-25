package org.wallentines.midnightnpcs.trait;

import org.wallentines.midnightlib.config.ConfigSection;
import org.wallentines.midnightnpcs.api.npc.NPC;
import org.wallentines.midnightnpcs.NPCEntity;
import net.minecraft.world.entity.player.Player;
import org.wallentines.midnightnpcs.api.trait.TraitType;

public class FabricLookTrait extends LookTrait {

    public FabricLookTrait(NPC npc) {
        super(npc);
    }

    @Override
    protected void lookAtNearestPlayer(float lookDistance) {

        NPCEntity ent = (NPCEntity) npc;

        Player pl = ent.level.getNearestPlayer(ent, lookDistance);
        if(pl != null) {

            ent.lookAt(pl, 60.0f, 60.0f);
            ent.setYHeadRot(ent.getYRot());
        }
    }

    @Override
    public TraitType getType() {
        return TYPE;
    }

    public static final TraitType TYPE = TraitType.create(FabricLookTrait::new, new ConfigSection());
}
