package org.wallentines.midnightnpcs.trait;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import org.wallentines.midnightcore.fabric.event.entity.EntityLoadedEvent;
import org.wallentines.midnightcore.fabric.event.entity.EntitySpawnEvent;
import org.wallentines.midnightlib.config.ConfigSection;
import org.wallentines.midnightlib.event.Event;
import org.wallentines.midnightnpcs.NPCEntity;
import org.wallentines.midnightnpcs.api.npc.NPC;
import org.wallentines.midnightnpcs.api.trait.TraitType;
import org.wallentines.midnightnpcs.goal.FollowEntityGoal;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class FabricFollowTrait extends FollowTrait {


    private final List<Goal> registeredGoals = new ArrayList<>();
    private final NPCEntity npc;
    private LivingEntity cache;

    public FabricFollowTrait(NPC n) {

        if (!(n instanceof NPCEntity)) throw new IllegalArgumentException(n.getUUID() + " is not a FabricNPC!");
        npc = (NPCEntity) n;

        Event.register(EntityLoadedEvent.class, this, ev -> {
            if(cache == null && (ev.getEntity() instanceof LivingEntity le) && le.getUUID().equals(followingId)) {
                cache = le;
            }
        });

        Event.register(EntitySpawnEvent.class, this, ev -> {
            if(cache == null && (ev.getEntity() instanceof LivingEntity le) && le.getUUID().equals(followingId)) {
                cache = le;
            }
        });

        registeredGoals.add(npc.addGoal(6, new FollowEntityGoal(npc, () -> cache, 1.0, 4.0f, 2.0f)));
    }

    @Override
    public TraitType getType() {
        return TYPE;
    }

    @Override
    public void onRemove() {
        super.onRemove();

        Event.unregisterAll(this);
        for(Goal g : registeredGoals) {
            npc.removeGoal(g);
        }
    }

    @Override
    protected void cacheTarget(UUID targetId) {

        Entity ent = ((ServerLevel) npc.level).getEntity(targetId);
        if(ent instanceof LivingEntity le) cache = le;

    }

    @Override
    protected void clearCache() {

        cache = null;
    }

    public static final TraitType TYPE = TraitType.create(FabricFollowTrait::new, new ConfigSection());

}
