package org.wallentines.midnightnpcs.common.util;

import org.wallentines.midnightcore.api.module.lang.LangModule;
import org.wallentines.midnightcore.api.module.lang.PlaceholderSupplier;
import org.wallentines.midnightnpcs.api.npc.NPC;

public final class LangUtil {

    public static void registerLangEntries(LangModule module) {

        module.registerPlaceholder("midnightnpcs_npc_name", PlaceholderSupplier.create(NPC.class, NPC::getDisplayName));
        module.registerInlinePlaceholder("midnightnpcs_npc_id", PlaceholderSupplier.create(NPC.class, npc -> npc.getUUID().toString()));

    }

}
