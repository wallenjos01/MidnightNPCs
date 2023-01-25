package org.wallentines.midnightnpcs.api;

import org.wallentines.midnightcore.api.text.LangProvider;
import org.wallentines.midnightlib.config.ConfigSection;
import org.wallentines.midnightlib.registry.Identifier;
import org.wallentines.midnightnpcs.api.npc.NPC;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.UUID;

@SuppressWarnings("unused")
public abstract class MidnightNPCsAPI {


    protected static Logger LOGGER = LogManager.getLogger("MidnightNPCs");
    protected static MidnightNPCsAPI INSTANCE;

    public static MidnightNPCsAPI getInstance() {
        return INSTANCE;
    }

    public static Logger getLogger() {
        return LOGGER;
    }


    public abstract ConfigSection getConfig();

    public abstract LangProvider getLangProvider();

    public abstract NPC getNPC(Identifier world, UUID u);

}
