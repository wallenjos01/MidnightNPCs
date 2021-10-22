package me.m1dnightninja.midnightnpcs.common.trait;

import me.m1dnightninja.midnightcore.api.registry.MIdentifier;
import me.m1dnightninja.midnightnpcs.api.trait.Trait;

public abstract class FollowTrait implements Trait {

    public static final MIdentifier ID = MIdentifier.create("midnightnpcs", "follow");

    @Override
    public MIdentifier getId() {
        return ID;
    }

}
