package org.wallentines.midnightnpcs.trait;

import org.wallentines.midnightlib.config.ConfigSection;
import org.wallentines.midnightlib.registry.Identifier;
import org.wallentines.midnightnpcs.api.trait.Trait;

import java.util.UUID;

public abstract class DefendTrait implements Trait {

    public static final Identifier ID = new Identifier("midnightnpcs", "defend");

    protected UUID defendingId;

    protected DefendTrait() { }

    @Override
    public void loadConfig(ConfigSection sec) {

        clearCache();
        defendingId = sec.getOrDefault("defending", null, UUID.class);
        cacheTarget(defendingId);
    }

    @Override
    public void onRemove() {
        clearCache();
    }

    @Override
    public ConfigSection saveConfig() {

        return new ConfigSection().with("defending", defendingId);
    }

    protected abstract void cacheTarget(UUID targetId);
    protected abstract void clearCache();
}
