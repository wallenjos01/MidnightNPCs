package me.m1dnightninja.midnightnpcs.api.npc;

import me.m1dnightninja.midnightcore.api.config.ConfigSection;
import me.m1dnightninja.midnightcore.api.inventory.MInventoryGUI;
import me.m1dnightninja.midnightcore.api.math.Vec3d;
import me.m1dnightninja.midnightcore.api.module.skin.Skin;
import me.m1dnightninja.midnightcore.api.player.Location;
import me.m1dnightninja.midnightcore.api.player.MPlayer;
import me.m1dnightninja.midnightcore.api.player.Requirement;
import me.m1dnightninja.midnightcore.api.registry.MIdentifier;
import me.m1dnightninja.midnightcore.api.text.MComponent;
import me.m1dnightninja.midnightnpcs.api.trait.Trait;
import me.m1dnightninja.midnightnpcs.api.trait.TraitType;

import java.util.UUID;

public interface NPC extends NPCSelector {

    UUID getUUID();

    MIdentifier getEntityType();

    MIdentifier getWorldId();

    Vec3d getLocation();

    float getPitch();

    float getYaw();

    MComponent getDisplayName();

    Skin getSkin();


    void setEntityType(MIdentifier type);

    void teleport(Location location);

    void teleport(Vec3d location, float yaw, float pitch);

    default void teleport(Vec3d location) { teleport(location, getYaw(), getPitch()); }

    void setDisplayName(MComponent component);

    void setSkin(Skin skin);


    boolean executeCommands(MPlayer player, MInventoryGUI.ClickType type);

    void addCommand(MInventoryGUI.ClickType click, NPCActionType type, String value, Requirement requirement);

    void clearCommands();

    void runCommand(String value);

    Iterable<NPCAction> getCommands();


    void addTrait(TraitType trait);

    void addTrait(TraitType trait, ConfigSection section);

    boolean hasTrait(TraitType identifier);

    void removeTrait(TraitType trait);

    Trait getTrait(TraitType trait);

    Iterable<Trait> getTraits();

    void setTraitConfig(TraitType type, ConfigSection sec, boolean fill);


    void tick();

    void spawn();

    void respawn();

    void kill();

}
