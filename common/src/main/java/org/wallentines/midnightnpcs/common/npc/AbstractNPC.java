package org.wallentines.midnightnpcs.common.npc;

import org.wallentines.midnightcore.api.MidnightCoreAPI;
import org.wallentines.midnightlib.config.ConfigSection;
import org.wallentines.midnightcore.api.item.InventoryGUI;
import org.wallentines.midnightlib.math.Vec3d;
import org.wallentines.midnightcore.api.module.skin.Skin;
import org.wallentines.midnightcore.api.player.Location;
import org.wallentines.midnightcore.api.player.MPlayer;
import org.wallentines.midnightlib.requirement.Requirement;
import org.wallentines.midnightlib.registry.Identifier;
import org.wallentines.midnightcore.api.text.MComponent;
import org.wallentines.midnightnpcs.api.MidnightNPCsAPI;
import org.wallentines.midnightnpcs.api.npc.NPC;
import org.wallentines.midnightnpcs.api.npc.NPCAction;
import org.wallentines.midnightnpcs.api.npc.NPCActionType;
import org.wallentines.midnightnpcs.api.trait.Trait;
import org.wallentines.midnightnpcs.api.trait.TraitType;

import java.util.*;

public abstract class AbstractNPC implements NPC {

    protected final UUID uuid;

    protected Identifier entityType;

    protected static Random random = new Random();
    protected Location location;
    protected MComponent name;
    protected Skin skin;

    protected HashMap<TraitType, Trait> traits = new HashMap<>();
    protected HashMap<InventoryGUI.ClickType, Collection<NPCAction>> actions = new HashMap<>();

    public AbstractNPC(Location location, Identifier entityType, UUID uuid, MComponent name) {
        this.location = location;
        this.entityType = entityType;
        this.uuid = uuid == null ? createRandomUUID() : uuid;
        this.name = name;

        NPCManager.INSTANCE.loadNPC(this);
    }


    @Override
    public UUID getUUID() {
        return uuid;
    }

    @Override
    public Identifier getEntityType() {
        return entityType;
    }

    @Override
    public Identifier getWorldId() {
        return location.getWorldId();
    }

    @Override
    public Vec3d getLocation() {
        return location.getCoordinates();
    }

    @Override
    public float getPitch() {
        return location.getPitch();
    }

    @Override
    public float getYaw() {
        return location.getYaw();
    }

    @Override
    public MComponent getDisplayName() {
        return name;
    }

    @Override
    public Skin getSkin() {
        return skin;
    }


    @Override
    public void setEntityType(Identifier type) {
        this.entityType = type;
        respawn();
    }

    @Override
    public void teleport(Location location) {
        this.location = location;
        doTeleport();
    }

    @Override
    public void teleport(Vec3d location, float yaw, float pitch) {
        this.location = new Location(getWorldId(), location, yaw, pitch);
        doTeleport();
    }

    @Override
    public void setDisplayName(MComponent component) {
        this.name = component;
        doNameUpdate();
    }

    @Override
    public void setSkin(Skin skin) {
        this.skin = skin;
        doSkinUpdate();
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
    public void addCommand(InventoryGUI.ClickType click, NPCActionType type, String value, Requirement<MPlayer> requirement) {

        addCommand(click, new NPCAction(type, this, value, requirement));
    }

    @Override
    public void clearCommands() {
        actions.clear();
    }

    @Override
    public Iterable<NPCAction> getCommands() {
        List<NPCAction> out = new ArrayList<>();
        for(Collection<NPCAction> acts : actions.values()) {
            out.addAll(acts);
        }
        return out;
    }

    @Override
    public void addTrait(TraitType trait) {

        if(traits.containsKey(trait)) return;
        addTrait(trait, getTraitConfig(trait));

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
    public boolean hasTrait(TraitType identifier) {

        return traits.containsKey(identifier);
    }

    @Override
    public void removeTrait(TraitType trait) {

        traits.remove(trait);
    }

    @Override
    public Iterable<Trait> getTraits() {

        return traits.values();
    }

    @Override
    public Trait getTrait(TraitType trait) {

        return traits.get(trait);
    }

    @Override
    public void setTraitConfig(TraitType id, ConfigSection sec, boolean fill) {

        Trait t = traits.get(id);

        if(fill) {
            ConfigSection config = t.saveConfig();
            config.fill(sec);

            t.loadConfig(config);

        } else {

            t.loadConfig(sec);
        }
    }

    @Override
    public void tick() {

        for(Trait t : traits.values()) {
            t.onTick();
        }
    }

    @Override
    public NPC getSelectedNPC() {
        return this;
    }

    @Override
    public void setSelectedNPC(NPC n) {
        // Ignore
    }

    protected void addCommand(InventoryGUI.ClickType click, NPCAction act) {

        if(act == null) return;

        actions.compute(click, (k,v) -> {
            if(v == null) v = new ArrayList<>();
            v.add(act);
            return v;
        });
    }

    public abstract void doTeleport();
    public abstract void doSkinUpdate();
    public abstract void doNameUpdate();

    protected abstract ConfigSection getTraitConfig(TraitType id);

    public static UUID createRandomUUID() {

        byte[] bytes = new byte[24];
        random.nextBytes(bytes);

        return UUID.nameUUIDFromBytes(bytes);
    }
}
