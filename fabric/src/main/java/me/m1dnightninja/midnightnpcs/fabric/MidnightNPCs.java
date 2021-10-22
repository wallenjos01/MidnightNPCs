package me.m1dnightninja.midnightnpcs.fabric;

import me.m1dnightninja.midnightcore.api.MidnightCoreAPI;
import me.m1dnightninja.midnightcore.api.math.Color;
import me.m1dnightninja.midnightcore.api.player.MPlayer;
import me.m1dnightninja.midnightcore.api.registry.MIdentifier;
import me.m1dnightninja.midnightcore.api.text.MComponent;
import me.m1dnightninja.midnightcore.api.text.MStyle;
import me.m1dnightninja.midnightcore.common.config.JsonConfigProvider;
import me.m1dnightninja.midnightcore.fabric.MidnightCore;
import me.m1dnightninja.midnightcore.fabric.MidnightCoreModInitializer;
import me.m1dnightninja.midnightcore.fabric.event.Event;
import me.m1dnightninja.midnightcore.fabric.event.EventHandler;
import me.m1dnightninja.midnightcore.fabric.event.PlayerChatEvent;
import me.m1dnightninja.midnightcore.fabric.player.FabricPlayer;
import me.m1dnightninja.midnightcore.fabric.util.ConversionUtil;
import me.m1dnightninja.midnightcore.fabric.util.LocationUtil;
import me.m1dnightninja.midnightnpcs.api.npc.NPC;
import me.m1dnightninja.midnightnpcs.api.trait.Trait;
import me.m1dnightninja.midnightnpcs.api.trait.TraitType;
import me.m1dnightninja.midnightnpcs.common.MidnightNPCsImpl;
import me.m1dnightninja.midnightnpcs.common.npc.AbstractNPC;
import me.m1dnightninja.midnightnpcs.common.trait.FollowTrait;
import me.m1dnightninja.midnightnpcs.common.trait.LookTrait;
import me.m1dnightninja.midnightnpcs.fabric.client.ClientInit;
import me.m1dnightninja.midnightnpcs.fabric.command.NPCCommand;
import me.m1dnightninja.midnightnpcs.fabric.entity.NPCEntity;
import me.m1dnightninja.midnightnpcs.fabric.npc.FabricNPC;
import me.m1dnightninja.midnightnpcs.fabric.trait.FabricFollowTrait;
import me.m1dnightninja.midnightnpcs.fabric.trait.FabricLookTrait;
import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;

import java.io.File;

public class MidnightNPCs implements MidnightCoreModInitializer {

    private MidnightNPCsImpl api;

    @Override
    public void onInitialize() {

        api = new MidnightNPCsImpl(new File("config/MidnightNPCs"));

        TraitType.TRAIT_REGISTRY.register(LookTrait.ID, FabricLookTrait::new);
        TraitType.TRAIT_REGISTRY.register(FollowTrait.ID, FabricFollowTrait::new);

        Registry.register(Registry.ENTITY_TYPE, new ResourceLocation("midnightnpcs", "npc"), NPCEntity.TYPE);
        FabricDefaultAttributeRegistry.register(NPCEntity.TYPE, NPCEntity.createMobAttributes());

        if(FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
            ClientInit.onInitializeClient();
        }

        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
            NPCCommand.register(dispatcher);
        });
    }

    @Override
    public void onAPICreated(MidnightCore core, MidnightCoreAPI api) {

        this.api.initialize(JsonConfigProvider.INSTANCE.loadFromStream(getClass().getResourceAsStream("/assets/midnightnpcs/lang/en_us.json")));
    }
}
