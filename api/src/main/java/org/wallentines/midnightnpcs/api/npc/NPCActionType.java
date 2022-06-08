package org.wallentines.midnightnpcs.api.npc;

import org.wallentines.midnightcore.api.MidnightCoreAPI;
import org.wallentines.midnightcore.api.player.MPlayer;
import org.wallentines.midnightlib.registry.Identifier;
import org.wallentines.midnightlib.registry.Registry;
import org.wallentines.midnightnpcs.api.MidnightNPCsAPI;

public interface NPCActionType {

    void execute(MPlayer clicker, NPC npc, String value);


    Registry<NPCActionType> ACTION_TYPE_REGISTRY = new Registry<>();

    static NPCActionType register(String id, NPCActionType type) {

        return ACTION_TYPE_REGISTRY.register(Identifier.parseOrDefault(id, "midnightnpcs"), type);
    }

    NPCActionType MESSAGE = register("message", (clicker, npc, value) -> clicker.sendMessage(MidnightNPCsAPI.getInstance().getLangProvider().getModule().parseText(value, clicker, npc)));
    NPCActionType COMMAND = register("command", (clicker, npc, value) -> npc.runCommand(MidnightNPCsAPI.getInstance().getLangProvider().getModule().parseText(value, clicker, npc).getAllContent()));
    NPCActionType CONSOLE_COMMAND = register("console_command", (clicker, npc, value) -> MidnightCoreAPI.getInstance().executeConsoleCommand(MidnightNPCsAPI.getInstance().getLangProvider().getModule().parseText(value, clicker, npc).getAllContent(), false));
    NPCActionType PLAYER_COMMAND = register("player_command", (clicker, npc, value) -> clicker.executeCommand(MidnightNPCsAPI.getInstance().getLangProvider().getModule().parseText(value, clicker, npc).getAllContent()));

}
