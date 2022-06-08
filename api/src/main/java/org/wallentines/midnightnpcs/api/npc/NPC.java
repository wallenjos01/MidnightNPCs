package org.wallentines.midnightnpcs.api.npc;

import org.wallentines.midnightlib.config.ConfigSection;
import org.wallentines.midnightcore.api.item.InventoryGUI;
import org.wallentines.midnightlib.math.Vec3d;
import org.wallentines.midnightcore.api.module.skin.Skin;
import org.wallentines.midnightcore.api.player.Location;
import org.wallentines.midnightcore.api.player.MPlayer;
import org.wallentines.midnightlib.requirement.Requirement;
import org.wallentines.midnightlib.registry.Identifier;
import org.wallentines.midnightcore.api.text.MComponent;
import org.wallentines.midnightnpcs.api.trait.Trait;
import org.wallentines.midnightnpcs.api.trait.TraitType;

import java.util.UUID;

public interface NPC extends NPCSelector {

    UUID getUUID();

    Identifier getEntityType();

    Identifier getWorldId();

    Vec3d getLocation();

    float getPitch();

    float getYaw();

    MComponent getDisplayName();

    Skin getSkin();


    void setEntityType(Identifier type);

    void teleport(Location location);

    void teleport(Vec3d location, float yaw, float pitch);

    default void teleport(Vec3d location) { teleport(location, getYaw(), getPitch()); }

    void setDisplayName(MComponent component);

    void setSkin(Skin skin);


    boolean executeCommands(MPlayer player, InventoryGUI.ClickType type);

    void addCommand(InventoryGUI.ClickType click, NPCActionType type, String value, Requirement<MPlayer> requirement);

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
