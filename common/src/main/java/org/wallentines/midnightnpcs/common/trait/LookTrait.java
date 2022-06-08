package org.wallentines.midnightnpcs.common.trait;

import org.wallentines.midnightlib.config.ConfigSection;
import org.wallentines.midnightlib.registry.Identifier;
import org.wallentines.midnightnpcs.api.npc.NPC;
import org.wallentines.midnightnpcs.api.trait.Trait;

public abstract class LookTrait implements Trait {

    public static final Identifier ID = new Identifier("midnightnpcs", "look");

    protected final NPC npc;
    private float lookDistance = 8.0f;

    public LookTrait(NPC npc) {
        this.npc = npc;
    }

    @Override
    public Identifier getId() {
        return ID;
    }

    @Override
    public void loadConfig(ConfigSection sec) {

        if(sec.has("distance")) lookDistance = sec.getFloat("distance");
    }

    @Override
    public void onTick() {
        lookAtNearestPlayer(lookDistance);
    }

    @Override
    public ConfigSection saveConfig() {

        ConfigSection out = new ConfigSection();
        out.set("distance", lookDistance);

        return out;
    }

    protected abstract void lookAtNearestPlayer(float lookDistance);

}
