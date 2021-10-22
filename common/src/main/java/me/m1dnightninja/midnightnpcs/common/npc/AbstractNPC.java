package me.m1dnightninja.midnightnpcs.common.npc;

import me.m1dnightninja.midnightcore.api.MidnightCoreAPI;
import me.m1dnightninja.midnightcore.api.config.ConfigSection;
import me.m1dnightninja.midnightcore.api.inventory.MInventoryGUI;
import me.m1dnightninja.midnightcore.api.math.Vec3d;
import me.m1dnightninja.midnightcore.api.module.skin.Skin;
import me.m1dnightninja.midnightcore.api.player.Location;
import me.m1dnightninja.midnightcore.api.player.MPlayer;
import me.m1dnightninja.midnightcore.api.player.Requirement;
import me.m1dnightninja.midnightcore.api.registry.MIdentifier;
import me.m1dnightninja.midnightcore.api.text.MComponent;
import me.m1dnightninja.midnightnpcs.api.MidnightNPCsAPI;
import me.m1dnightninja.midnightnpcs.api.npc.NPC;
import me.m1dnightninja.midnightnpcs.api.npc.NPCAction;
import me.m1dnightninja.midnightnpcs.api.npc.NPCActionType;
import me.m1dnightninja.midnightnpcs.api.trait.Trait;
import me.m1dnightninja.midnightnpcs.api.trait.TraitType;

import java.util.*;

public abstract class AbstractNPC implements NPC {

    protected final UUID uuid;

    protected MIdentifier entityType;

    protected Location location;
    protected MComponent name;
    protected Skin skin;

    protected HashMap<TraitType, Trait> traits = new HashMap<>();
    protected HashMap<MInventoryGUI.ClickType, Collection<NPCAction>> actions = new HashMap<>();

    public AbstractNPC(Location location, MIdentifier entityType, UUID uuid, MComponent name) {
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
    public MIdentifier getEntityType() {
        return entityType;
    }

    @Override
    public MIdentifier getWorldId() {
        return location.getWorld();
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
    public void setEntityType(MIdentifier type) {
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
    public boolean executeCommands(MPlayer player, MInventoryGUI.ClickType type) {

        if(!actions.containsKey(type)) return false;

        int executed = 0;

        for(NPCAction act : actions.get(type)) {
            if(act.tryExecute(player)) executed++;
        }

        return executed > 0;
    }

    @Override
    public void addCommand(MInventoryGUI.ClickType click, NPCActionType type, String value, Requirement requirement) {

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

    protected void addCommand(MInventoryGUI.ClickType click, NPCAction act) {

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
        MidnightCoreAPI.getInstance().getRandom().nextBytes(bytes);

        return UUID.nameUUIDFromBytes(bytes);
    }
}
