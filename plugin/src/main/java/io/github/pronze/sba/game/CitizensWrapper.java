package io.github.pronze.sba.game;

import java.util.HashMap;
import java.util.Map;

import org.screamingsandals.bedwars.api.game.GameStore;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.MemoryNPCDataStore;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.NPCRegistry;

@Getter
@AllArgsConstructor class CitizensWrapper
{
    public CitizensWrapper()
    {
        citizenRegistry=CitizensAPI.createAnonymousNPCRegistry(new MemoryNPCDataStore());
        citizensStores=new HashMap<>();
    }
    private NPCRegistry citizenRegistry;
    private Map<org.screamingsandals.bedwars.api.game.GameStore, net.citizensnpcs.api.npc.NPC> citizensStores;
}