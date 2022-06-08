package org.wallentines.midnightnpcs.fabric.npc;

import net.minecraft.server.MinecraftServer;
import org.wallentines.midnightlib.config.ConfigSection;
import org.wallentines.midnightcore.api.item.InventoryGUI;
import org.wallentines.midnightcore.api.module.skin.Skin;
import org.wallentines.midnightcore.api.player.Location;
import org.wallentines.midnightlib.registry.Identifier;
import org.wallentines.midnightcore.api.text.MComponent;
import org.wallentines.midnightcore.fabric.MidnightCore;
import org.wallentines.midnightcore.fabric.util.ConversionUtil;
import org.wallentines.midnightcore.fabric.util.LocationUtil;
import org.wallentines.midnightnpcs.api.MidnightNPCsAPI;
import org.wallentines.midnightnpcs.api.npc.NPCAction;
import org.wallentines.midnightnpcs.api.trait.Trait;
import org.wallentines.midnightnpcs.api.trait.TraitType;
import org.wallentines.midnightnpcs.common.npc.AbstractNPC;
import org.wallentines.midnightnpcs.fabric.entity.NPCEntity;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;

import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class FabricNPC extends AbstractNPC {

    private NPCEntity entity;

    public FabricNPC(Location location, Identifier entityType, UUID uuid, MComponent name) {
        super(location, entityType, uuid, name);
    }

    public FabricNPC(Location location, Identifier entityType, UUID uuid, MComponent name, NPCEntity ent) {
        super(location, entityType, uuid, name);
        this.entity = ent;
    }

    @Override
    public Skin getSkin() {
        return skin;
    }

    @Override
    public void doTeleport() {
        LocationUtil.teleport(entity, location);
    }

    @Override
    public void doSkinUpdate() {
        entity.setSkin(skin);
    }

    @Override
    public void doNameUpdate() {
        entity.displayNameUpdated();
    }

    @Override
    protected ConfigSection getTraitConfig(TraitType id) {

        if (entity == null) return new ConfigSection();

        CompoundTag tag = entity.saveWithoutId(new CompoundTag());
        if (!tag.contains("MidnightNPC", 10)) return new ConfigSection();

        CompoundTag mnpc = tag.getCompound("MidnightNPC");
        if (!mnpc.contains("traits", 10)) return new ConfigSection();

        CompoundTag traits = mnpc.getCompound("traits");
        if (!traits.contains(TraitType.TRAIT_REGISTRY.getId(id).toString())) return new ConfigSection();

        return ConversionUtil.toConfigSection(traits.getCompound(id.toString()));
    }

    @Override
    public void spawn() {

        entity = NPCEntity.fromNPC(this);
        entity.level.addFreshEntity(entity);

        for(Trait t : traits.values()) {
            t.onSpawn();
        }

    }

    @Override
    public void respawn() {

        if(entity == null) {
            spawn();
        } else {
            entity.respawn();
        }
    }

    @Override
    public void kill() {

        if(entity != null) {

            entity.kill();
        }
        entity = null;
    }

    @Override
    public void runCommand(String value) {

        MinecraftServer server = entity.getServer();
        if(server == null) return;

        CommandSourceStack stack = entity.createCommandSourceStack().withSuppressedOutput();
        server.getCommands().performCommand(stack, value);
    }

    public CompoundTag saveActions() {

        if(actions.isEmpty()) return null;

        CompoundTag out = new CompoundTag();
        for(Map.Entry<InventoryGUI.ClickType, Collection<NPCAction>> ent : actions.entrySet()) {

            ListTag tag = new ListTag();
            for(NPCAction act : ent.getValue()) {

                tag.add(ConversionUtil.toCompoundTag(act.save()));
            }

            out.put(ent.getKey().name(), tag);
        }

        return out;
    }

    public void loadActions(CompoundTag tag) {

        actions.clear();

        for(String s : tag.getAllKeys()) {

            InventoryGUI.ClickType type;
            try {
                type = InventoryGUI.ClickType.valueOf(s.toUpperCase(Locale.ROOT));
            } catch (IllegalStateException ex) {
                MidnightNPCsAPI.getLogger().warn("No click type exists with name " + s + "!");
                continue;
            }

            for(Tag t : tag.getList(s, 10)) {
                ConfigSection sec = ConversionUtil.toConfigSection((CompoundTag) t);
                addCommand(type, NPCAction.parse(sec, this));
            }
        }
    }

    public NPCEntity getEntity() {
        return entity;
    }

    public ServerLevel getLevel() {

        return MidnightCore.getInstance().getServer().getLevel(ResourceKey.create(Registry.DIMENSION_REGISTRY, ConversionUtil.toResourceLocation(location.getWorldId())));
    }
}
