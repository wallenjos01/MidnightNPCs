package org.wallentines.midnightnpcs.trait;

import org.wallentines.midnightlib.config.ConfigSection;
import org.wallentines.midnightlib.registry.Identifier;
import org.wallentines.midnightnpcs.api.trait.Trait;

import java.util.UUID;

public abstract class FollowTrait implements Trait {

    public static final Identifier ID = new Identifier("midnightnpcs", "follow");

    protected UUID followingId;

    @Override
    public void loadConfig(ConfigSection sec) {

        clearCache();
        followingId = sec.getOrDefault("following", null, UUID.class);
        cacheTarget(followingId);
    }

    @Override
    public void onRemove() {
        clearCache();
    }

    @Override
    public ConfigSection saveConfig() {

        return new ConfigSection().with("following", followingId);
    }

    protected abstract void cacheTarget(UUID targetId);
    protected abstract void clearCache();
}
