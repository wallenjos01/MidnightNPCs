package me.m1dnightninja.midnightnpcs.api.npc;

import me.m1dnightninja.midnightcore.api.config.ConfigSection;
import me.m1dnightninja.midnightcore.api.player.MPlayer;
import me.m1dnightninja.midnightcore.api.player.Requirement;
import me.m1dnightninja.midnightcore.api.registry.MIdentifier;
import me.m1dnightninja.midnightnpcs.api.MidnightNPCsAPI;

public class NPCAction {

    private final NPCActionType type;
    private final NPC npc;
    private final String value;
    private final Requirement requirement;

    public NPCAction(NPCActionType type, NPC npc, String value, Requirement requirement) {
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

        NPCActionType type = NPCActionType.ACTION_TYPE_REGISTRY.get(MIdentifier.parseOrDefault(sec.getString("type"), "midnightnpcs"));
        String value = sec.getString("value");

        Requirement req = null;

        if(sec.has("requirement")) {
            req = sec.get("requirement", Requirement.class);
        }

        return new NPCAction(type, npc, value, req);
    }

}
