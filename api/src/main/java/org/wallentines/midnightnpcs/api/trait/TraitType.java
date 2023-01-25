package org.wallentines.midnightnpcs.api.trait;

import org.wallentines.midnightlib.config.ConfigSection;
import org.wallentines.midnightlib.registry.Registry;
import org.wallentines.midnightnpcs.api.npc.NPC;

import java.util.function.Function;

public interface TraitType {

    Trait create(NPC n);

    Registry<TraitType> TRAIT_REGISTRY = new Registry<>();

    default ConfigSection getDefaultConfig() { return new ConfigSection(); }

    static TraitType create(Function<NPC, Trait> creator, ConfigSection defaultConfig) {
        return new TraitType() {
            @Override
            public Trait create(NPC n) {
                return creator.apply(n);
            }
            @Override
            public ConfigSection getDefaultConfig() {
                return defaultConfig;
            }
        };
    }

}
