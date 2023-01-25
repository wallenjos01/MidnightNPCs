package org.wallentines.midnightnpcs;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import org.wallentines.midnightcore.fabric.event.server.CommandLoadEvent;
import org.wallentines.midnightlib.config.serialization.json.JsonConfigProvider;
import org.wallentines.midnightlib.event.Event;
import org.wallentines.midnightnpcs.api.trait.TraitType;
import org.wallentines.midnightnpcs.trait.*;

import java.nio.file.Paths;

public class MidnightNPCs implements ModInitializer {

    public static final EntityType<NPCEntity> NPC_TYPE = Registry.register(
            BuiltInRegistries.ENTITY_TYPE,
            new ResourceLocation("midnightnpcs", "npc"),
            FabricEntityTypeBuilder.create(MobCategory.MISC, NPCEntity::new).dimensions(EntityDimensions.fixed(0.6f, 1.8f)).build()
    );

    @Override
    public void onInitialize() {

        TraitType.TRAIT_REGISTRY.register(LookTrait.ID, FabricLookTrait.TYPE);
        TraitType.TRAIT_REGISTRY.register(FollowTrait.ID, FabricFollowTrait.TYPE);
        TraitType.TRAIT_REGISTRY.register(DefendTrait.ID, FabricDefendTrait.TYPE);

        FabricDefaultAttributeRegistry.register(NPC_TYPE, NPCEntity.createNPCAttributes());

        Event.register(CommandLoadEvent.class, this, event -> NPCCommand.register(event.getDispatcher()));

        new MidnightNPCsImpl(Paths.get("config/MidnightNPCs"), JsonConfigProvider.INSTANCE.loadFromStream(getClass().getResourceAsStream("/midnightnpcs/lang/en_us.json")));
    }
}
