package org.wallentines.midnightnpcs;

import com.mojang.authlib.GameProfile;
import com.mojang.datafixers.util.Pair;
import io.netty.buffer.Unpooled;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.NotNull;
import org.wallentines.midnightcore.api.item.InventoryGUI;
import org.wallentines.midnightcore.api.player.Location;
import org.wallentines.midnightcore.api.player.MPlayer;
import org.wallentines.midnightlib.config.ConfigSection;
import org.wallentines.midnightlib.math.Vec3d;
import org.wallentines.midnightcore.api.module.skin.Skin;
import org.wallentines.midnightlib.registry.Identifier;
import org.wallentines.midnightcore.api.text.MComponent;
import org.wallentines.midnightcore.common.util.MojangUtil;
import org.wallentines.midnightcore.fabric.player.FabricPlayer;
import org.wallentines.midnightcore.fabric.util.ConversionUtil;
import org.wallentines.midnightcore.fabric.util.LocationUtil;
import org.wallentines.midnightnpcs.api.MidnightNPCsAPI;
import org.wallentines.midnightnpcs.api.npc.NPC;
import org.wallentines.midnightnpcs.api.npc.NPCAction;
import org.wallentines.midnightnpcs.api.trait.Trait;
import org.wallentines.midnightnpcs.api.trait.TraitType;
import org.wallentines.midnightnpcs.attribute.EntityAttribute;
import org.wallentines.midnightnpcs.attribute.FabricAttributes;
import org.wallentines.midnightnpcs.mixin.AccessorAction;
import org.wallentines.midnightnpcs.mixin.AccessorPlayer;
import net.minecraft.ChatFormatting;
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
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.RandomStringUtils;

import java.util.*;
import java.util.function.Consumer;

public class NPCEntity extends PathfinderMob implements NPC {

    private final Player internalPlayer;
    private final String profileName;
    private final GameProfile gameProfile;
    private EntityType<?> visibleType;

    private final HashMap<TraitType, Trait> traits = new HashMap<>();
    private final HashMap<InventoryGUI.ClickType, Collection<NPCAction>> actions = new HashMap<>();


    public NPCEntity(EntityType<? extends NPCEntity> entityType, Level level) {
        super(entityType, level);

        setUUID(UUID.nameUUIDFromBytes(RandomStringUtils.random(24, true, true).getBytes()));

        this.profileName = generateInvisibleName(NPCManager.INSTANCE.getNPCCount());
        this.gameProfile = new GameProfile(getUUID(), profileName);
        this.visibleType = EntityType.PLAYER;

        this.internalPlayer = new Player(level, getOnPos(), getYHeadRot(), gameProfile) {
            @Override
            public boolean isSpectator() {
                return false;
            }
            @Override
            public boolean isCreative() {
                return false;
            }
        };

        this.maxUpStep = 0.6f;
        this.setCanPickUpLoot(false);
        this.setCustomNameVisible(FabricAttributes.NAME_VISIBLE.getDefaultValue());
        this.setInvulnerable(FabricAttributes.INVULNERABLE.getDefaultValue());
        this.setPersistenceRequired();
        this.xpReward = 0;
        this.noActionTime = 0;

        internalPlayer.getEntityData().set(AccessorPlayer.getModelPartsDataId(), (byte) 0b1111111);

        NPCManager.INSTANCE.loadNPC(this);
    }

    public Player getInternalPlayer() {
        return internalPlayer;
    }

    public Goal addGoal(Goal goal) {
        return addGoal(2, goal);
    }

    public Goal addGoal(int priority, Goal goal) {
        goalSelector.addGoal(priority, goal);
        return goal;
    }

    public void removeGoal(Goal goal) {
        goalSelector.removeGoal(goal);
    }

    public static AttributeSupplier.Builder createNPCAttributes() {

        AttributeSupplier.Builder out = Mob.createMobAttributes();
        for(EntityAttribute att : FabricAttributes.getEntityAttributes()) {
            out.add(att.getBase(), att.getDefaultValue());
        }

        return out;
    }

    @Override
    public @NotNull UUID getUUID() {
        return uuid;
    }

    @Override
    public @NotNull InteractionResult interactAt(@NotNull Player player, @NotNull Vec3 vec3, @NotNull InteractionHand interactionHand) {

        if(!(player instanceof ServerPlayer)) return InteractionResult.CONSUME;

        InventoryGUI.ClickType type = interactionHand == InteractionHand.MAIN_HAND ?
                player.isShiftKeyDown() ? InventoryGUI.ClickType.SHIFT_LEFT : InventoryGUI.ClickType.LEFT :
                player.isShiftKeyDown() ? InventoryGUI.ClickType.SHIFT_RIGHT : InventoryGUI.ClickType.RIGHT;

        boolean out = executeCommands(FabricPlayer.wrap((ServerPlayer) player), type);
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

        for(Trait t : traits.values()) {
            t.onTick();
        }
    }

    @Override
    public void aiStep() {

        this.updateSwingTime();
        super.aiStep();
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
    public void checkDespawn() {

        this.noActionTime = 0;
    }

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
    public @NotNull PushReaction getPistonPushReaction() {
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
        for(Trait t : getTraits()) {

            traits.put(TraitType.TRAIT_REGISTRY.getId(t.getType()).toString(), ConversionUtil.toCompoundTag(t.saveConfig()));
        }

        tag.putByte("modelLayers", internalPlayer.getEntityData().get(AccessorPlayer.getModelPartsDataId()));

        tag.put("traits", traits);
        tag.putString("type", getEntityType().toString());

        if(!actions.isEmpty()) {
            CompoundTag allActions = new CompoundTag();
            for (Map.Entry<InventoryGUI.ClickType, Collection<NPCAction>> ent : actions.entrySet()) {

                ListTag acts = new ListTag();
                for (NPCAction act : ent.getValue()) {

                    acts.add(ConversionUtil.toCompoundTag(act.save()));
                }

                allActions.put(ent.getKey().name(), acts);
            }
            tag.put("actions", allActions);
        }

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

        visibleType = BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.tryParse(tag.getString("type")));

        if(tag.contains("modelLayers")) {
            internalPlayer.getEntityData().set(AccessorPlayer.getModelPartsDataId(), tag.getByte("modelLayers"));
        }

        if(tag.contains("traits")) {

            CompoundTag traits = tag.getCompound("traits");
            for(String key : traits.getAllKeys()) {

                if(!traits.contains(key, 10)) continue;

                TraitType t = TraitType.TRAIT_REGISTRY.get(Identifier.parseOrDefault(key, "midnightnpcs"));
                if(t == null) continue;


                addTrait(t, ConversionUtil.toConfigSection(traits.getCompound(key)));
            }
        }

        if(tag.contains("actions")) {

            actions.clear();

            CompoundTag actionTag = tag.getCompound("actions");
            for(String s :actionTag .getAllKeys()) {

                InventoryGUI.ClickType type;
                try {
                    type = InventoryGUI.ClickType.valueOf(s.toUpperCase(Locale.ROOT));
                } catch (IllegalStateException ex) {
                    MidnightNPCsAPI.getLogger().warn("No click type exists with name " + s + "!");
                    continue;
                }

                for(Tag t : actionTag.getList(s, 10)) {
                    ConfigSection sec = ConversionUtil.toConfigSection((CompoundTag) t);
                    addCommand(type, NPCAction.parse(sec, this));
                }
            }
        }
    }

    @Override
    public void remove(@NotNull RemovalReason removalReason) {

        NPCManager.INSTANCE.unloadNPC(this);
        super.remove(removalReason);
    }

    @Override
    protected int getPermissionLevel() {
        return 2;
    }

    @Override
    public @NotNull Packet<ClientGamePacketListener> getAddEntityPacket() {

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

    @Override
    public @NotNull SynchedEntityData getEntityData() {

        if(visibleType == EntityType.PLAYER) {
            return internalPlayer.getEntityData();
        }

        return super.getEntityData();
    }

    public void onPreSpawn(Consumer<Packet<?>> consumer) {

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
        ClientboundRotateHeadPacket head = new ClientboundRotateHeadPacket(this, (byte) ((int) (getYHeadRot() * 256.0F / 360.0F)));

        consumer.accept(equip);
        consumer.accept(head);

        List<SynchedEntityData.DataValue<?>> entityData = getEntityData().getNonDefaultValues();
        if(entityData != null) {

            consumer.accept(new ClientboundSetEntityDataPacket(getId(), entityData));
        }

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

        EnumSet<ClientboundPlayerInfoUpdatePacket.Action> actions = EnumSet.of(
                ClientboundPlayerInfoUpdatePacket.Action.ADD_PLAYER,
                ClientboundPlayerInfoUpdatePacket.Action.UPDATE_LISTED,
                ClientboundPlayerInfoUpdatePacket.Action.UPDATE_LATENCY
        );

        List<ClientboundPlayerInfoUpdatePacket.Entry> entries = new ArrayList<>();
        entries.add(new ClientboundPlayerInfoUpdatePacket.Entry(
                getUUID(),
                gameProfile,
                false,
                0,
                GameType.CREATIVE,
                null,
                null
        ));


        // Write Custom Packet
        FriendlyByteBuf byteBuf = new FriendlyByteBuf(Unpooled.buffer());

        byteBuf.writeEnumSet(actions, ClientboundPlayerInfoUpdatePacket.Action.class);
        byteBuf.writeCollection(entries, (buf, entry) -> {
            buf.writeUUID(entry.profileId());
            for(ClientboundPlayerInfoUpdatePacket.Action act : actions) {
                AccessorAction.class.cast(act).getWriter().write(buf, entry);
            }
        });

        return new ClientboundPlayerInfoUpdatePacket(byteBuf);
    }

    public Packet<?> getPostSpawnPacket() {

        if(visibleType != EntityType.PLAYER) return null;

        return new ClientboundPlayerInfoRemovePacket(List.of(getUUID()));
    }

    public void displayNameUpdated() {

        if(visibleType == EntityType.PLAYER) {
            respawn();
        }
    }

    public boolean isLookedAt(Entity ent) {

        if(level != ent.level || ent.distanceTo(this) > 128.0f) return false;

        Vec3 entForward = ent.getViewVector(1.0F).normalize();
        Vec3 distance = new Vec3(getX() - ent.getX(), getEyeY() - ent.getEyeY(), getZ() - ent.getZ());
        double d = distance.length();
        distance = distance.normalize();
        double e = entForward.dot(distance);

        Vec3 eyeLoc = new Vec3(getX(), getEyeY(), getZ());
        Vec3 entEyeLoc = new Vec3(ent.getX(), ent.getEyeY(), ent.getZ());

        if(e > 1.0 - 0.025 / d) {

            BlockHitResult res = this.level.clip(new ClipContext(eyeLoc, entEyeLoc, ClipContext.Block.COLLIDER, net.minecraft.world.level.ClipContext.Fluid.NONE, this));
            return res.getType() == HitResult.Type.MISS;

        }

        return false;
    }

    @Override
    public Identifier getEntityType() {
        return ConversionUtil.toIdentifier(BuiltInRegistries.ENTITY_TYPE.getKey(visibleType));
    }

    @Override
    public Identifier getWorldId() {
        return ConversionUtil.toIdentifier(level.dimension().location());
    }

    @Override
    public Vec3d getLocation() {
        return new Vec3d(getX(), getY(), getZ());
    }

    @Override
    public float getPitch() {
        return getXRot();
    }

    @Override
    public float getYaw() {
        return getYRot();
    }

    @Override
    public MComponent getVisibleName() {
        return ConversionUtil.toMComponent(getCustomName());
    }

    @Override
    public void setEntityType(Identifier type) {
        this.visibleType = BuiltInRegistries.ENTITY_TYPE.get(ConversionUtil.toResourceLocation(type));
        respawn();
    }

    @Override
    public void teleport(Location location) {
        LocationUtil.teleport(this, location);
    }

    @Override
    public void teleport(Vec3d location, float yaw, float pitch) {
        teleport(new Location(ConversionUtil.toIdentifier(level.dimension().location()), location, yaw, pitch));
    }

    @Override
    public void setDisplayName(MComponent component) {

        setCustomName(ConversionUtil.toComponent(component));
    }

    @Override
    public boolean executeCommands(MPlayer player, InventoryGUI.ClickType type) {

        if(!actions.containsKey(type)) return false;

        int executed = 0;

        for(NPCAction act : actions.get(type)) {
            if(act.tryExecute(player)) executed++;
        }

        return executed > 0;
    }

    @Override
    public void addCommand(InventoryGUI.ClickType click, NPCAction act) {

        actions.computeIfAbsent(click, k -> new ArrayList<>()).add(act);
    }

    @Override
    public void clearCommands() {

        actions.clear();
    }

    @Override
    public void runCommand(String value) {

        MinecraftServer server = getServer();
        if(server == null) throw new IllegalStateException("Attempt to run command on null server!");

        server.getCommands().performPrefixedCommand(createCommandSourceStack(), value);
    }

    @Override
    public Collection<NPCAction> getCommands(InventoryGUI.ClickType type) {
        return actions.containsKey(type) ? actions.get(type) : new ArrayList<>();
    }

    @Override
    public void addTrait(TraitType trait) {

        if(traits.containsKey(trait)) return;
        addTrait(trait, trait.getDefaultConfig());
    }

    @Override
    public void addTrait(TraitType trait, ConfigSection section) {

        if(traits.containsKey(trait)) return;

        Trait instance = trait.create(this);

        try {
            instance.loadConfig(section);

        } catch (Exception ex) {

            MidnightNPCsAPI.getLogger().warn("An error occurred while initializing a Trait!");
            ex.printStackTrace();
        }
        traits.put(trait, instance);

    }

    @Override
    public boolean hasTrait(TraitType trait) {

        return traits.containsKey(trait);
    }

    @Override
    public void removeTrait(TraitType trait) {

        Trait t = traits.remove(trait);
        if(t != null) t.onRemove();
    }

    @Override
    public Trait getTrait(TraitType trait) {

        return traits.get(trait);
    }

    @Override
    public Collection<Trait> getTraits() {

        return traits.values();
    }

    @Override
    public void setTraitConfig(TraitType type, ConfigSection sec, boolean fill) {

        Trait t = traits.get(type);

        if(fill) {
            ConfigSection config = t.saveConfig();
            config.fill(sec);

            t.loadConfig(config);

        } else {

            t.loadConfig(sec);
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
        return this;
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
        return this;
    }

    @Override
    public void setSelectedNPC(NPC n) {
        // Ignore
    }

    public void setSkin(Skin skin) {

        MojangUtil.setSkinOfProfile(gameProfile, skin);
        if(visibleType == EntityType.PLAYER) {
            respawn();
        }
    }

    @Override
    public Skin getSkin() {

        return MojangUtil.getSkinFromProfile(gameProfile);
    }

    @Override
    public void resetSkin() {

        gameProfile.getProperties().get("textures").clear();
        if(visibleType == EntityType.PLAYER) {
            respawn();
        }
    }



}
