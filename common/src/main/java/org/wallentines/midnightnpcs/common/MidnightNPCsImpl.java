package org.wallentines.midnightnpcs.common;

import org.wallentines.midnightcore.api.MidnightCoreAPI;
import org.wallentines.midnightcore.common.util.FileUtil;
import org.wallentines.midnightlib.config.ConfigSection;
import org.wallentines.midnightlib.config.FileConfig;
import org.wallentines.midnightcore.api.module.lang.LangModule;
import org.wallentines.midnightcore.api.module.lang.LangProvider;
import org.wallentines.midnightcore.api.player.MPlayer;
import org.wallentines.midnightlib.registry.Identifier;
import org.wallentines.midnightnpcs.api.MidnightNPCsAPI;
import org.wallentines.midnightnpcs.api.npc.NPC;
import org.wallentines.midnightnpcs.common.util.LangUtil;

import java.io.File;
import java.nio.file.Path;
import java.util.UUID;

public class MidnightNPCsImpl extends MidnightNPCsAPI {

    private LangProvider provider;

    private final File dataFolder;
    private final FileConfig mainConfig;

    public MidnightNPCsImpl(Path dataFolder) {
        INSTANCE = this;

        File fld = FileUtil.tryCreateDirectory(dataFolder);
        if(fld == null) {
            throw new IllegalStateException("Unable to create data directory " + dataFolder);
        }

        this.dataFolder = fld;
        this.mainConfig = FileConfig.findOrCreate("config", this.dataFolder);

    }

    public void initialize(ConfigSection langDefaults) {

        LangModule module = MidnightCoreAPI.getInstance().getModuleManager().getModule(LangModule.class);
        provider = module.createProvider(dataFolder.toPath().resolve("lang"), langDefaults);

        LangUtil.registerLangEntries(module);

    }

    @Override
    public ConfigSection getConfig() {

        return mainConfig.getRoot();
    }

    @Override
    public LangProvider getLangProvider() {
        return provider;
    }

    @Override
    public NPC getNPC(Identifier world, UUID u) {
        return null;
    }

}
