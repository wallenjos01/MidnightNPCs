package me.m1dnightninja.midnightnpcs.fabric.npc;

import me.m1dnightninja.midnightcore.api.config.ConfigSection;
import me.m1dnightninja.midnightcore.api.inventory.MInventoryGUI;
import me.m1dnightninja.midnightcore.api.module.skin.Skin;
import me.m1dnightninja.midnightcore.api.player.Location;
import me.m1dnightninja.midnightcore.api.registry.MIdentifier;
import me.m1dnightninja.midnightcore.api.text.MComponent;
import me.m1dnightninja.midnightcore.fabric.MidnightCore;
import me.m1dnightninja.midnightcore.fabric.util.ConversionUtil;
import me.m1dnightninja.midnightcore.fabric.util.LocationUtil;
import me.m1dnightninja.midnightnpcs.api.MidnightNPCsAPI;
import me.m1dnightninja.midnightnpcs.api.npc.NPCAction;
import me.m1dnightninja.midnightnpcs.api.trait.Trait;
import me.m1dnightninja.midnightnpcs.api.trait.TraitType;
import me.m1dnightninja.midnightnpcs.common.npc.AbstractNPC;
import me.m1dnightninja.midnightnpcs.fabric.entity.NPCEntity;
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

    public FabricNPC(Location location, MIdentifier entityType, UUID uuid, MComponent name) {
        super(location, entityType, uuid, name);
    }

    public FabricNPC(Location location, MIdentifier entityType, UUID uuid, MComponent name, NPCEntity ent) {
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

        return ConversionUtil.fromCompoundTag(traits.getCompound(id.toString()));
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

        CommandSourceStack stack = entity.createCommandSourceStack();
        MidnightCore.getServer().getCommands().performCommand(stack, value);
    }

    public CompoundTag saveActions() {

        if(actions.isEmpty()) return null;

        CompoundTag out = new CompoundTag();
        for(Map.Entry<MInventoryGUI.ClickType, Collection<NPCAction>> ent : actions.entrySet()) {

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

            MInventoryGUI.ClickType type;
            try {
                type = MInventoryGUI.ClickType.valueOf(s.toUpperCase(Locale.ROOT));
            } catch (IllegalStateException ex) {
                MidnightNPCsAPI.getLogger().warn("No click type exists with name " + s + "!");
                continue;
            }

            for(Tag t : tag.getList(s, 10)) {
                ConfigSection sec = ConversionUtil.fromCompoundTag((CompoundTag) t);
                addCommand(type, NPCAction.parse(sec, this));
            }
        }
    }

    public NPCEntity getEntity() {
        return entity;
    }

    public ServerLevel getLevel() {
        return MidnightCore.getServer().getLevel(ResourceKey.create(Registry.DIMENSION_REGISTRY, ConversionUtil.toResourceLocation(location.getWorld())));
    }
}
