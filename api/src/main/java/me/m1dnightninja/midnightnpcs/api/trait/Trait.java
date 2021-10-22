package me.m1dnightninja.midnightnpcs.api.trait;

import me.m1dnightninja.midnightcore.api.config.ConfigSection;
import me.m1dnightninja.midnightcore.api.registry.MIdentifier;

public interface Trait {

    MIdentifier getId();

    void loadConfig(ConfigSection sec);

    default void onTick() { }
    default void onSpawn() { }

    default ConfigSection saveConfig() { return null; }
}
