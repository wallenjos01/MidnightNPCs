package org.wallentines.midnightnpcs.api.trait;

import org.wallentines.midnightlib.config.ConfigSection;

@SuppressWarnings("unused")
public interface Trait {

    TraitType getType();

    void loadConfig(ConfigSection sec);

    default void onTick() { }
    default void onSpawn() { }
    default void onRemove() { }

    default ConfigSection saveConfig() { return getType().getDefaultConfig(); }


}
