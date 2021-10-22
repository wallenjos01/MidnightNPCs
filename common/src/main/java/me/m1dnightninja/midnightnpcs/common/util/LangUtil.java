package me.m1dnightninja.midnightnpcs.common.util;

import me.m1dnightninja.midnightcore.api.module.lang.ILangModule;
import me.m1dnightninja.midnightcore.api.module.lang.PlaceholderSupplier;
import me.m1dnightninja.midnightnpcs.api.npc.NPC;

public final class LangUtil {

    public static void registerLangEntries(ILangModule module) {

        module.registerPlaceholderSupplier("midnightnpcs_npc_name", PlaceholderSupplier.create(NPC.class, NPC::getDisplayName));
        module.registerInlinePlaceholderSupplier("midnightnpcs_npc_id", PlaceholderSupplier.create(NPC.class, npc -> npc.getUUID().toString()));

    }

}
