package me.m1dnightninja.midnightnpcs.fabric.client;

import me.m1dnightninja.midnightnpcs.fabric.entity.NPCEntity;
import net.fabricmc.fabric.api.client.rendereregistry.v1.EntityRendererRegistry;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.resources.ResourceLocation;

public class ClientInit {

    public static void onInitializeClient() {
        EntityRendererRegistry.INSTANCE.register(NPCEntity.TYPE, context -> new MobRenderer<>(context, new PlayerModel<>(context.bakeLayer(ModelLayers.PLAYER), false), 1.0F) {
            @Override
            public ResourceLocation getTextureLocation(NPCEntity entity) {
                return DefaultPlayerSkin.getDefaultSkin();
            }
        });
    }

}
