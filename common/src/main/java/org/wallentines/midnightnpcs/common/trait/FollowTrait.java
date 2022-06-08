package org.wallentines.midnightnpcs.common.trait;

import org.wallentines.midnightlib.registry.Identifier;
import org.wallentines.midnightnpcs.api.trait.Trait;

public abstract class FollowTrait implements Trait {

    public static final Identifier ID = new Identifier("midnightnpcs", "follow");

    @Override
    public Identifier getId() {
        return ID;
    }

}
