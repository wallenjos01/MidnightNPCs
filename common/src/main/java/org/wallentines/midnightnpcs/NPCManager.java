package org.wallentines.midnightnpcs;

import org.wallentines.midnightnpcs.api.npc.NPC;

import java.util.*;

@SuppressWarnings("unused")
public class NPCManager {

    public static final NPCManager INSTANCE = new NPCManager();

    private final List<NPC> loaded = new ArrayList<>();
    private final HashMap<UUID, Integer> indicesById = new HashMap<>();


    public int getNPCCount() {
        return loaded.size();
    }

    public NPC getByUUID(UUID u) {
        Integer index = indicesById.get(u);
        if(index == null) return null;

        return loaded.get(index);
    }

    public Collection<UUID> getIds() {

        return indicesById.keySet();
    }

    public Collection<NPC> getNPCs() {
        return loaded;
    }

    public void loadNPC(NPC n) {

        if(indicesById.containsKey(n.getUUID())) return;

        int index = loaded.size();
        loaded.add(n);
        indicesById.put(n.getUUID(), index);
    }

    public void unloadNPC(NPC n) {

        Integer index = indicesById.get(n.getUUID());
        if(index == null) return;

        if(n != loaded.get(index)) return;

        loaded.remove(index.intValue());
        indicesById.remove(n.getUUID());

        for(int i = index ; i < loaded.size() ; i++) {

            indicesById.put(n.getUUID(), i);
        }

    }

}
