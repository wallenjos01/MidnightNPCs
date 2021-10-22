package me.m1dnightninja.midnightnpcs.api.npc;

import me.m1dnightninja.midnightcore.api.MidnightCoreAPI;
import me.m1dnightninja.midnightcore.api.player.MPlayer;
import me.m1dnightninja.midnightcore.api.registry.MIdentifier;
import me.m1dnightninja.midnightcore.api.registry.MRegistry;
import me.m1dnightninja.midnightnpcs.api.MidnightNPCsAPI;

public interface NPCActionType {

    void execute(MPlayer clicker, NPC npc, String value);


    MRegistry<NPCActionType> ACTION_TYPE_REGISTRY = new MRegistry<>();

    static NPCActionType register(String id, NPCActionType type) {

        return ACTION_TYPE_REGISTRY.register(MIdentifier.parseOrDefault(id, "midnightnpcs"), type);
    }

    NPCActionType MESSAGE = register("message", (clicker, npc, value) -> clicker.sendMessage(MidnightNPCsAPI.getInstance().getLangProvider().getModule().parseText(value, clicker, npc)));
    NPCActionType COMMAND = register("command", (clicker, npc, value) -> npc.runCommand(MidnightNPCsAPI.getInstance().getLangProvider().getModule().applyPlaceholdersFlattened(value, clicker, npc)));
    NPCActionType CONSOLE_COMMAND = register("console_command", (clicker, npc, value) -> MidnightCoreAPI.getInstance().executeConsoleCommand(MidnightNPCsAPI.getInstance().getLangProvider().getModule().applyPlaceholdersFlattened(value, clicker, npc)));
    NPCActionType PLAYER_COMMAND = register("player_command", (clicker, npc, value) -> clicker.executeCommand(MidnightNPCsAPI.getInstance().getLangProvider().getModule().applyPlaceholdersFlattened(value, clicker, npc)));

}
