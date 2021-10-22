package me.m1dnightninja.midnightnpcs.api;

import me.m1dnightninja.midnightcore.api.config.ConfigSection;
import me.m1dnightninja.midnightcore.api.module.lang.ILangProvider;
import me.m1dnightninja.midnightcore.api.player.MPlayer;
import me.m1dnightninja.midnightcore.api.registry.MIdentifier;
import me.m1dnightninja.midnightnpcs.api.npc.NPC;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.UUID;

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

    public abstract ILangProvider getLangProvider();

    public abstract NPC getNPC(MIdentifier world, UUID u);

}
