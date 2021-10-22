package me.m1dnightninja.midnightnpcs.common;

import me.m1dnightninja.midnightcore.api.MidnightCoreAPI;
import me.m1dnightninja.midnightcore.api.config.ConfigSection;
import me.m1dnightninja.midnightcore.api.config.FileConfig;
import me.m1dnightninja.midnightcore.api.module.lang.ILangModule;
import me.m1dnightninja.midnightcore.api.module.lang.ILangProvider;
import me.m1dnightninja.midnightcore.api.player.MPlayer;
import me.m1dnightninja.midnightcore.api.registry.MIdentifier;
import me.m1dnightninja.midnightnpcs.api.MidnightNPCsAPI;
import me.m1dnightninja.midnightnpcs.api.npc.NPC;
import me.m1dnightninja.midnightnpcs.common.util.LangUtil;

import java.io.File;
import java.util.UUID;

public class MidnightNPCsImpl extends MidnightNPCsAPI {

    private ILangProvider provider;

    private final File dataFolder;
    private final FileConfig mainConfig;

    public MidnightNPCsImpl(File dataFolder) {
        INSTANCE = this;

        this.dataFolder = dataFolder;
        this.mainConfig = FileConfig.findOrCreate("config", dataFolder);

    }

    public void initialize(ConfigSection langDefaults) {

        ILangModule module = MidnightCoreAPI.getInstance().getModule(ILangModule.class);
        provider = module.createLangProvider(new File(dataFolder, "lang"), langDefaults);

        LangUtil.registerLangEntries(module);

    }

    @Override
    public ConfigSection getConfig() {

        return mainConfig.getRoot();
    }

    @Override
    public ILangProvider getLangProvider() {
        return provider;
    }

    @Override
    public NPC getNPC(MIdentifier world, UUID u) {
        return null;
    }

}
