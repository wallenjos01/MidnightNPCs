package org.wallentines.midnightnpcs;

import org.wallentines.midnightcore.api.text.LangProvider;
import org.wallentines.midnightcore.api.text.PlaceholderManager;
import org.wallentines.midnightcore.api.text.PlaceholderSupplier;
import org.wallentines.midnightcore.common.util.FileUtil;
import org.wallentines.midnightlib.config.ConfigSection;
import org.wallentines.midnightlib.config.FileConfig;
import org.wallentines.midnightlib.registry.Identifier;
import org.wallentines.midnightnpcs.api.MidnightNPCsAPI;
import org.wallentines.midnightnpcs.api.npc.NPC;

import java.io.File;
import java.nio.file.Path;
import java.util.UUID;

public class MidnightNPCsImpl extends MidnightNPCsAPI {

    private final LangProvider provider;

    private final FileConfig mainConfig;

    public MidnightNPCsImpl(Path dataFolder, ConfigSection langDefaults) {
        INSTANCE = this;

        File fld = FileUtil.tryCreateDirectory(dataFolder);
        if(fld == null) {
            throw new IllegalStateException("Unable to create data directory " + dataFolder);
        }

        this.mainConfig = FileConfig.findOrCreate("config", fld);

        FileUtil.tryCreateDirectory(dataFolder.resolve("lang"));
        provider = new LangProvider(dataFolder.resolve("lang"), langDefaults);

        PlaceholderManager.INSTANCE.getPlaceholders().register("midnightnpcs_npc_name", PlaceholderSupplier.create(NPC.class, NPC::getVisibleName));
        PlaceholderManager.INSTANCE.getInlinePlaceholders().register("midnightnpcs_npc_id", PlaceholderSupplier.create(NPC.class, npc -> npc.getUUID().toString()));
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
