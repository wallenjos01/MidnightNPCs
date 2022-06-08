package org.wallentines.midnightnpcs.api.trait;

import org.wallentines.midnightlib.config.ConfigSection;
import org.wallentines.midnightlib.registry.Identifier;

public interface Trait {

    Identifier getId();

    void loadConfig(ConfigSection sec);

    default void onTick() { }
    default void onSpawn() { }

    default ConfigSection saveConfig() { return null; }
}
