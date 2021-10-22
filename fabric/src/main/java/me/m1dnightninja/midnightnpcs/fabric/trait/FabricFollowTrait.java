package me.m1dnightninja.midnightnpcs.fabric.trait;

import me.m1dnightninja.midnightcore.api.config.ConfigSection;
import me.m1dnightninja.midnightcore.fabric.MidnightCore;
import me.m1dnightninja.midnightcore.fabric.util.LocationUtil;
import me.m1dnightninja.midnightnpcs.api.npc.NPC;
import me.m1dnightninja.midnightnpcs.common.trait.FollowTrait;
import me.m1dnightninja.midnightnpcs.fabric.entity.NPCEntity;
import me.m1dnightninja.midnightnpcs.fabric.npc.FabricNPC;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.FollowMobGoal;

import java.util.UUID;

public class FabricFollowTrait extends FollowTrait {

    private final FabricNPC npc;
    private Mob following;

    public FabricFollowTrait(NPC n) {

        if(!(n instanceof FabricNPC)) throw new IllegalArgumentException(n.getUUID() + " is not a FabricNPC!");
        npc = (FabricNPC) n;

    }

    @Override
    public void loadConfig(ConfigSection sec) {

        if(!sec.has("following", UUID.class)) return;
        UUID u = sec.get("following", UUID.class);

        for(ServerLevel level : MidnightCore.getServer().getAllLevels()) {
            Entity m = level.getEntity(u);
            if(m instanceof Mob) {
                following = (Mob) m;
                break;
            }
        }

    }

    @Override
    public void onSpawn() {

        if(following == null || following.isRemoved()) return;
        npc.teleport(LocationUtil.getEntityLocation(following));

        NPCEntity ent = npc.getEntity();
        ent.addGoal(new FollowMobGoal(following, 1.0f, 1.5f, 1000.0f));
    }

    @Override
    public ConfigSection saveConfig() {
        ConfigSection out = new ConfigSection();
        if(following != null) out.set("following", following.getUUID());

        return out;
    }
}
