package org.wallentines.midnightnpcs.api.trait;

import org.wallentines.midnightlib.registry.Registry;
import org.wallentines.midnightnpcs.api.npc.NPC;

public interface TraitType {

    Trait create(NPC n);

    Registry<TraitType> TRAIT_REGISTRY = new Registry<>();

}
