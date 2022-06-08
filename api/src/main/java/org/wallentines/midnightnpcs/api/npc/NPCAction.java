package org.wallentines.midnightnpcs.api.npc;

import org.wallentines.midnightlib.config.ConfigSection;
import org.wallentines.midnightcore.api.player.MPlayer;
import org.wallentines.midnightlib.requirement.Requirement;
import org.wallentines.midnightlib.registry.Identifier;
import org.wallentines.midnightnpcs.api.MidnightNPCsAPI;

public class NPCAction {

    private final NPCActionType type;
    private final NPC npc;
    private final String value;
    private final Requirement<MPlayer> requirement;

    public NPCAction(NPCActionType type, NPC npc, String value, Requirement<MPlayer> requirement) {
        this.type = type;
        this.npc = npc;
        this.value = value;
        this.requirement = requirement;
    }

    public NPCActionType getType() {
        return type;
    }

    public NPC getNpc() {
        return npc;
    }

    public String getValue() {
        return value;
    }

    public boolean tryExecute(MPlayer player) {

        if(requirement != null && !requirement.check(player)) return false;
        return execute(player);
    }

    public boolean execute(MPlayer player) {

        try {
            type.execute(player, npc, value);
            return true;

        } catch (Throwable th) {

            MidnightNPCsAPI.getLogger().warn("An error occurred while an NPC was executing a command!");
            th.printStackTrace();
        }
        return false;
    }

    public ConfigSection save() {

        ConfigSection out = new ConfigSection();
        out.set("type", NPCActionType.ACTION_TYPE_REGISTRY.getId(type).toString());
        out.set("value", value);
        if(requirement != null) out.set("requirement", requirement);

        return out;
    }

    public static NPCAction parse(ConfigSection sec, NPC npc) {

        if(!sec.has("type") || !sec.has("value")) return null;

        NPCActionType type = NPCActionType.ACTION_TYPE_REGISTRY.get(Identifier.parseOrDefault(sec.getString("type"), "midnightnpcs"));
        String value = sec.getString("value");

        Requirement<MPlayer> req = null;

        if(sec.has("requirement")) {
            req = sec.get("requirement", Requirement.class);
        }

        return new NPCAction(type, npc, value, req);
    }

}
