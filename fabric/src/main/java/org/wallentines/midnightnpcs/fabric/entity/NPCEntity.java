package org.wallentines.midnightnpcs.fabric.entity;

import com.mojang.authlib.GameProfile;
import com.mojang.datafixers.util.Pair;
import io.netty.buffer.Unpooled;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Crypt;
import net.minecraft.util.CryptException;
import net.minecraft.world.entity.player.ProfilePublicKey;
import org.jetbrains.annotations.NotNull;
import org.wallentines.midnightcore.api.item.InventoryGUI;
import org.wallentines.midnightlib.config.serialization.json.JsonConfigProvider;
import org.wallentines.midnightlib.math.Vec3d;
import org.wallentines.midnightcore.api.module.skin.Skin;
import org.wallentines.midnightcore.api.module.skin.Skinnable;
import org.wallentines.midnightlib.registry.Identifier;
import org.wallentines.midnightcore.api.text.MComponent;
import org.wallentines.midnightcore.common.util.MojangUtil;
import org.wallentines.midnightcore.fabric.player.FabricPlayer;
import org.wallentines.midnightcore.fabric.util.ConversionUtil;
import org.wallentines.midnightcore.fabric.util.LocationUtil;
import org.wallentines.midnightnpcs.api.npc.NPC;
import org.wallentines.midnightnpcs.api.npc.NPCSelector;
import org.wallentines.midnightnpcs.api.trait.Trait;
import org.wallentines.midnightnpcs.api.trait.TraitType;
import org.wallentines.midnightnpcs.common.npc.NPCManager;
import org.wallentines.midnightnpcs.fabric.MidnightNPCs;
import org.wallentines.midnightnpcs.fabric.mixin.AccessorPlayer;
import org.wallentines.midnightnpcs.fabric.npc.FabricNPC;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.*;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.control.LookControl;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.RandomStringUtils;

import java.security.KeyPair;
import java.time.Instant;
import java.util.*;
import java.util.function.Consumer;

public class NPCEntity extends PathfinderMob implements NPCSelector, Skinnable {

    private EntityType<?> visibleType;
    private final Player internalPlayer;
    private final String profileName;

    private HumanoidArm mainHand = HumanoidArm.RIGHT;
    private GameProfile gameProfile;

    private FabricNPC npc;

    public NPCEntity(EntityType<? extends NPCEntity> entityType, Level level) {
        super(entityType, level);

        setUUID(UUID.nameUUIDFromBytes(RandomStringUtils.random(24, true, true).getBytes()));

        profileName = generateInvisibleName(NPCManager.INSTANCE.getNPCCount());

        try {
            KeyPair pair = Crypt.generateKeyPair();

            gameProfile = new GameProfile(getUUID(), profileName);
            internalPlayer = new Player(level, getOnPos(), getYHeadRot(), gameProfile, ProfilePublicKey.createTrusted(
                    new ProfilePublicKey.Data(Instant.MAX, pair.getPublic(), pair.getPublic().getEncoded())
            )) {
                @Override
                public boolean isSpectator() {
                    return false;
                }

                @Override
                public boolean isCreative() {
                    return false;
                }
            };
            } catch (CryptException ex) {
            ex.printStackTrace();
            throw new IllegalStateException("Unable to create Public Key for Player!");
        }

        lookControl = new LookControl(this) {
            @Override
            protected boolean resetXRotOnTick() {
                return false;
            }
        };

        this.maxUpStep = 0.6f;
        this.setCanPickUpLoot(false);
        this.setCustomNameVisible(true);
        this.setInvulnerable(true);
        this.setPersistenceRequired();
        this.xpReward = 0;
        this.setSpeed(0.4f);
        ((GroundPathNavigation) this.getNavigation()).setCanPassDoors(true);

        internalPlayer.getEntityData().set(AccessorPlayer.getModelPartsDataId(), (byte) 0x7F);
    }

    public static NPCEntity fromNPC(FabricNPC npc) {
        NPCEntity out = new NPCEntity(MidnightNPCs.NPC_TYPE, npc.getLevel());

        Vec3d loc = npc.getLocation();

        out.teleportTo(loc.getX(), loc.getY(), loc.getZ());
        out.setRot(npc.getYaw(), npc.getPitch());
        out.setYHeadRot(out.getYRot());
        out.setCustomName(ConversionUtil.toComponent(npc.getDisplayName()));

        out.gameProfile = new GameProfile(npc.getUUID(), out.gameProfile.getName());
        out.npc = npc;

        return out;
    }

    public Player getInternalPlayer() {
        return internalPlayer;
    }

    public void setMainArm(HumanoidArm mainHand) {
        this.mainHand = mainHand;
    }

    public void addGoal(Goal goal) {
        goalSelector.addGoal(2, goal);
    }


    @Override
    public InteractionResult interactAt(@NotNull Player player, @NotNull Vec3 vec3, @NotNull InteractionHand interactionHand) {

        if(!(player instanceof ServerPlayer)) return InteractionResult.CONSUME;

        InventoryGUI.ClickType type = interactionHand == InteractionHand.MAIN_HAND ?
                player.isShiftKeyDown() ? InventoryGUI.ClickType.SHIFT_LEFT : InventoryGUI.ClickType.LEFT :
                player.isShiftKeyDown() ? InventoryGUI.ClickType.SHIFT_RIGHT : InventoryGUI.ClickType.RIGHT;


        boolean out = npc.executeCommands(FabricPlayer.wrap((ServerPlayer) player), type);
        return out ? InteractionResult.SUCCESS : InteractionResult.CONSUME;

    }

    @Override
    public void kill() {
        this.remove(RemovalReason.KILLED);
    }

    @Override
    public boolean removeWhenFarAway(double d) {
        return false;
    }

    @Override
    protected void customServerAiStep() {
        npc.tick();
    }

    @Override
    public HumanoidArm getMainArm() {

        return mainHand;
    }

    @Override
    public boolean isPathFinding() {
        return false;
    }

    @Override
    protected void registerGoals() { }

    @Override
    protected void pickUpItem(@NotNull ItemEntity itemEntity) { }

    @Override
    public boolean wantsToPickUp(@NotNull ItemStack itemStack) {
        return false;
    }

    @Override
    public boolean canBeLeashed(@NotNull Player player) {
        return false;
    }

    @Override
    protected boolean shouldDespawnInPeaceful() {
        return false;
    }

    @Override
    public void checkDespawn() { }

    @Override
    public boolean hurt(@NotNull DamageSource damageSource, float f) {
        return false;
    }

    @Override
    public MobType getMobType() {
        return MobType.UNDEFINED;
    }

    @Override
    protected void doPush(@NotNull Entity entity) { }

    @Override
    public void push(@NotNull Entity entity) { }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public PushReaction getPistonPushReaction() {
        return PushReaction.IGNORE;
    }

    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag compoundTag) {
        super.addAdditionalSaveData(compoundTag);

        CompoundTag tag = new CompoundTag();

        Skin skin = MojangUtil.getSkinFromProfile(gameProfile);
        if(skin != null) {

            CompoundTag skinTag = new CompoundTag();
            skinTag.putString("b64", skin.getValue());
            skinTag.putString("sig", skin.getSignature());

            tag.put("skin", skinTag);
        }

        CompoundTag traits = new CompoundTag();
        for(Trait t : npc.getTraits()) {

            traits.put(t.getId().toString(), ConversionUtil.toCompoundTag(t.saveConfig()));
        }

        tag.put("traits", traits);

        tag.putString("name", MComponent.SERIALIZER.serialize(npc.getDisplayName()).toString());
        tag.putString("type", npc.getEntityType().toString());

        CompoundTag actions = npc.saveActions();
        if(actions != null) tag.put("actions", actions);

        compoundTag.put("MidnightNPC", tag);

    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag compoundTag) {
        super.readAdditionalSaveData(compoundTag);

        if(!compoundTag.contains("MidnightNPC")) return;

        CompoundTag tag = compoundTag.getCompound("MidnightNPC");

        if(tag.contains("skin")) {

            CompoundTag skinTag = tag.getCompound("skin");

            Skin s = new Skin(getUUID(), skinTag.getString("b64"), skinTag.getString("sig"));
            MojangUtil.setSkinOfProfile(gameProfile, s);
        }

        Identifier type = Identifier.parse(tag.getString("type"));

        if(npc == null) {

            String name = tag.getString("name");

            GameProfile prof = new GameProfile(getUUID(), null);
            prof.getProperties().putAll(gameProfile.getProperties());

            gameProfile = prof;
            npc = new FabricNPC(LocationUtil.getEntityLocation(this), type, getUUID(), MComponent.SERIALIZER.deserialize(JsonConfigProvider.INSTANCE.loadFromString(name)), this);
        }

        if(tag.contains("traits")) {

            CompoundTag traits = tag.getCompound("traits");
            for(String key : traits.getAllKeys()) {

                if(!traits.contains(key, 10)) continue;

                TraitType t = TraitType.TRAIT_REGISTRY.get(Identifier.parseOrDefault(key, "midnightnpcs"));
                if(t == null) continue;

                npc.addTrait(t, ConversionUtil.toConfigSection(traits.getCompound(key)));
            }
        }

        if(tag.contains("actions")) {
            npc.loadActions(tag.getCompound("actions"));
        }
    }

    @Override
    public void remove(@NotNull RemovalReason removalReason) {

        NPCManager.INSTANCE.unloadNPC(npc);
        super.remove(removalReason);
    }

    @Override
    protected int getPermissionLevel() {
        return 2;
    }

    @Override
    public Packet<?> getAddEntityPacket() {

        if(visibleType == EntityType.PLAYER) {

            FriendlyByteBuf byteBuf = new FriendlyByteBuf(Unpooled.buffer());

            byteBuf.writeVarInt(getId());
            byteBuf.writeUUID(getUUID());
            byteBuf.writeDouble(getX());
            byteBuf.writeDouble(getY());
            byteBuf.writeDouble(getZ());
            byteBuf.writeByte((byte) ((int) (getYRot() * 256.0F / 360.0F)));
            byteBuf.writeByte((byte) ((int) (getXRot() * 256.0F / 360.0F)));

            return new ClientboundAddPlayerPacket(byteBuf);

        } else {

            return new ClientboundAddEntityPacket(getId(), getUUID(), getX(), getY(), getZ(), getXRot(), getYRot(), visibleType, 0, getDeltaMovement(), getYHeadRot());
        }
    }

    public SynchedEntityData getOutwardEntityData() {
        if(visibleType == EntityType.PLAYER) {
            return internalPlayer.getEntityData();
        }
        return getEntityData();
    }

    public void onPreSpawn(Consumer<Packet<?>> consumer) {

        visibleType = Registry.ENTITY_TYPE.get(ConversionUtil.toResourceLocation(npc.getEntityType()));

        Packet<?> preSpawn = getPreSpawnPacket();

        if(preSpawn != null) {
            consumer.accept(preSpawn);
        }
    }

    private void updateTeams(Consumer<Packet<?>> consumer) {

        String teamId = getUUID().toString().replace("-", "").substring(0,15);

        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        buf.writeUtf(teamId);
        buf.writeByte(1);

        consumer.accept(new ClientboundSetPlayerTeamPacket(buf));

        Component comp = getCustomName();
        if(comp == null) comp = Component.literal("");

        buf = new FriendlyByteBuf(Unpooled.buffer());
        buf.writeUtf(teamId);
        buf.writeByte(0);
        buf.writeComponent(Component.literal(""));
        buf.writeByte(2);
        buf.writeUtf(isCustomNameVisible() ? "always" : "never");
        buf.writeUtf("always");
        buf.writeEnum(ChatFormatting.WHITE);
        buf.writeComponent(comp);
        buf.writeComponent(Component.literal(""));
        buf.writeVarInt(1);
        buf.writeUtf(profileName);

        consumer.accept(new ClientboundSetPlayerTeamPacket(buf));
    }

    public void onPostSpawn(Consumer<Packet<?>> consumer) {

        List<Pair<EquipmentSlot, ItemStack>> items = new ArrayList<>();

        items.add(new Pair<>(EquipmentSlot.MAINHAND, getMainHandItem()));
        items.add(new Pair<>(EquipmentSlot.OFFHAND, getOffhandItem()));
        items.add(new Pair<>(EquipmentSlot.HEAD, getItemBySlot(EquipmentSlot.HEAD)));
        items.add(new Pair<>(EquipmentSlot.CHEST, getItemBySlot(EquipmentSlot.CHEST)));
        items.add(new Pair<>(EquipmentSlot.LEGS, getItemBySlot(EquipmentSlot.LEGS)));
        items.add(new Pair<>(EquipmentSlot.FEET, getItemBySlot(EquipmentSlot.FEET)));

        ClientboundSetEquipmentPacket equip = new ClientboundSetEquipmentPacket(getId(), items);

        ClientboundSetEntityDataPacket data = new ClientboundSetEntityDataPacket(getId(), getOutwardEntityData(), true);

        ClientboundRotateHeadPacket head = new ClientboundRotateHeadPacket(this, (byte) ((int) (getYHeadRot() * 256.0F / 360.0F)));

        consumer.accept(equip);
        consumer.accept(head);
        consumer.accept(data);

        if(visibleType == EntityType.PLAYER) {

            updateTeams(consumer);
        }

        Packet<?> post = getPostSpawnPacket();
        if(post != null) {
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    consumer.accept(post);
                }
            }, 3000L);
        }
    }

    public Packet<?> getPreSpawnPacket() {

        if(visibleType != EntityType.PLAYER) return null;

        FriendlyByteBuf byteBuf = new FriendlyByteBuf(Unpooled.buffer());

        byteBuf.writeEnum(ClientboundPlayerInfoPacket.Action.ADD_PLAYER);
        byteBuf.writeVarInt(1);
        byteBuf.writeUUID(getUUID());
        byteBuf.writeUtf(profileName);
        byteBuf.writeCollection(gameProfile.getProperties().values(), (buf, property) -> {
            buf.writeUtf(property.getName());
            buf.writeUtf(property.getValue());
            if(property.hasSignature()) {
                buf.writeBoolean(true);
                buf.writeUtf(property.getSignature());
            } else {
                buf.writeBoolean(false);
            }
        });
        byteBuf.writeVarInt(GameType.CREATIVE.getId());
        byteBuf.writeVarInt(0);
        byteBuf.writeBoolean(false);

        return new ClientboundPlayerInfoPacket(byteBuf);
    }

    public Packet<?> getPostSpawnPacket() {

        if(visibleType != EntityType.PLAYER) return null;

        FriendlyByteBuf byteBuf = new FriendlyByteBuf(Unpooled.buffer());

        byteBuf.writeEnum(ClientboundPlayerInfoPacket.Action.REMOVE_PLAYER);
        byteBuf.writeVarInt(1);
        byteBuf.writeUUID(getUUID());

        return new ClientboundPlayerInfoPacket(byteBuf);
    }

    public void setSkin(Skin skin) {

        MojangUtil.setSkinOfProfile(gameProfile, skin);
        if(visibleType == EntityType.PLAYER) {
            respawn();
        }
    }

    public void displayNameUpdated() {

        setCustomName(ConversionUtil.toComponent(npc.getDisplayName()));
        if(visibleType == EntityType.PLAYER) {
            respawn();
        }
    }

    public void respawn() {

        ClientboundRemoveEntitiesPacket destroy = new ClientboundRemoveEntitiesPacket(getId());

        for(Player player : level.players()) {
            ((ServerPlayer) player).connection.send(destroy);
        }

        Packet<?> spawn = getAddEntityPacket();

        for(Player player : level.players()) {

            ServerGamePacketListenerImpl conn = ((ServerPlayer) player).connection;

            onPreSpawn(conn::send);
            conn.send(spawn);
            onPostSpawn(conn::send);
        }

    }

    public NPC getNPC() {
        return npc;
    }

    public void setSkinPartVisible(PlayerModelPart part, boolean visible) {

        EntityDataAccessor<Byte> acc = AccessorPlayer.getModelPartsDataId();

        Byte oldValue = internalPlayer.getEntityData().get(acc);
        Byte newValue = (byte) (visible ? (oldValue | part.getMask()) : (oldValue ^ part.getMask()));

        internalPlayer.getEntityData().set(acc, newValue);
    }

    private static String generateInvisibleName(int index) {

        String str = "00000000";
        String code = Integer.toHexString(index);

        StringBuilder out = new StringBuilder();

        str = str.substring(0, 8 - code.length()) + code;
        for(int i = 0 ; i < 8 ; i++) {
            out.append("§").append(str.charAt(i));
        }

        return out.toString();
    }

    @Override
    public NPC getSelectedNPC() {
        return npc.getSelectedNPC();
    }

    @Override
    public void setSelectedNPC(NPC n) {
        npc.setSelectedNPC(n);
    }

    @Override
    public Skin getSkin() {
        return npc.getSkin();
    }

    @Override
    public void resetSkin() {
        // Ignore
    }

}
