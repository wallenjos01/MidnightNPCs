package org.wallentines.midnightnpcs.fabric;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import org.wallentines.midnightcore.fabric.event.MidnightCoreAPICreatedEvent;
import org.wallentines.midnightcore.fabric.event.server.CommandLoadEvent;
import org.wallentines.midnightlib.config.serialization.json.JsonConfigProvider;
import org.wallentines.midnightlib.event.Event;
import org.wallentines.midnightnpcs.api.trait.TraitType;
import org.wallentines.midnightnpcs.common.MidnightNPCsImpl;
import org.wallentines.midnightnpcs.common.trait.FollowTrait;
import org.wallentines.midnightnpcs.common.trait.LookTrait;
import org.wallentines.midnightnpcs.fabric.client.ClientInit;
import org.wallentines.midnightnpcs.fabric.command.NPCCommand;
import org.wallentines.midnightnpcs.fabric.entity.NPCEntity;
import org.wallentines.midnightnpcs.fabric.trait.FabricFollowTrait;
import org.wallentines.midnightnpcs.fabric.trait.FabricLookTrait;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Paths;

public class MidnightNPCs implements ModInitializer {

    private MidnightNPCsImpl api;

    public static final EntityType<NPCEntity> NPC_TYPE = Registry.register(
            Registry.ENTITY_TYPE,
            new ResourceLocation("midnightnpcs", "npc"),
            FabricEntityTypeBuilder.create(MobCategory.MISC, NPCEntity::new).dimensions(EntityDimensions.fixed(0.6f, 1.8f)).build()
    );

    @Override
    public void onInitialize() {

        api = new MidnightNPCsImpl(Paths.get("config/MidnightNPCs"));

        TraitType.TRAIT_REGISTRY.register(LookTrait.ID, FabricLookTrait::new);
        TraitType.TRAIT_REGISTRY.register(FollowTrait.ID, FabricFollowTrait::new);

        FabricDefaultAttributeRegistry.register(NPC_TYPE, NPCEntity.createMobAttributes());

        if(FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
            ClientInit.onInitializeClient();
        }

        Event.register(CommandLoadEvent.class, this, event -> NPCCommand.register(event.getDispatcher()));

        Event.register(MidnightCoreAPICreatedEvent.class, this, event -> {

            this.api.initialize(JsonConfigProvider.INSTANCE.loadFromStream(getClass().getResourceAsStream("/midnightnpcs/lang/en_us.json")));
        });

    }
}
