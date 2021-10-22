package me.m1dnightninja.midnightnpcs.common.trait;

import me.m1dnightninja.midnightcore.api.config.ConfigSection;
import me.m1dnightninja.midnightcore.api.registry.MIdentifier;
import me.m1dnightninja.midnightnpcs.api.npc.NPC;
import me.m1dnightninja.midnightnpcs.api.trait.Trait;

public abstract class LookTrait implements Trait {

    public static final MIdentifier ID = MIdentifier.create("midnightnpcs", "look");

    protected final NPC npc;
    private float lookDistance = 8.0f;

    public LookTrait(NPC npc) {
        this.npc = npc;
    }

    @Override
    public MIdentifier getId() {
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
