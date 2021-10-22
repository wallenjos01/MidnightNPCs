package me.m1dnightninja.midnightnpcs.api.trait;

import me.m1dnightninja.midnightcore.api.registry.MRegistry;
import me.m1dnightninja.midnightnpcs.api.npc.NPC;

public interface TraitType {

    Trait create(NPC n);

    MRegistry<TraitType> TRAIT_REGISTRY = new MRegistry<>();

}
