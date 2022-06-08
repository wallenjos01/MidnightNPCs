package org.wallentines.midnightnpcs.fabric.client;

public class ClientInit {

    public static void onInitializeClient() {
        /*EntityRendererRegistry.INSTANCE.register(NPCEntity.TYPE, context -> new MobRenderer<>(context, new PlayerModel<>(context.bakeLayer(ModelLayers.PLAYER), false), 1.0F) {
            @Override
            public ResourceLocation getTextureLocation(NPCEntity entity) {
                return DefaultPlayerSkin.getDefaultSkin();
            }
        });*/
    }

}
