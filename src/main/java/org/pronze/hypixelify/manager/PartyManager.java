package org.pronze.hypixelify.manager;
import org.bukkit.entity.Player;
import org.pronze.hypixelify.Party.Party;
import java.util.HashMap;

public class PartyManager {

    public HashMap<Player, Party> parties = new HashMap<>();


    public void disband(Player leader){
        if(parties.get(leader) == null)
            return;

        parties.get(leader).disband();
        parties.remove(leader);
    }

}
