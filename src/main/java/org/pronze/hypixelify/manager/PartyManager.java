package org.pronze.hypixelify.manager;
import org.bukkit.entity.Player;
import org.pronze.hypixelify.Hypixelify;
import org.pronze.hypixelify.database.PlayerDatabase;
import org.pronze.hypixelify.party.Party;
import java.util.HashMap;

public class PartyManager {


    public HashMap<Player, Party> parties = new HashMap<>();


    public void disband(Player leader){
        if(parties.get(leader) == null)
            return;

        parties.get(leader).disband();
        parties.remove(leader);
    }

    public boolean isInParty(Player player){
        if(Hypixelify.getInstance().playerData.get(player.getUniqueId()) != null)
            return Hypixelify.getInstance().playerData.get(player.getUniqueId()).isInParty();

        return false;
    }

    public Party getParty(Player player){
        if(!isInParty(player)) return null;

        PlayerDatabase database = Hypixelify.getInstance().playerData.get(player.getUniqueId());
        if(database == null) return null;
        if(database.getPartyLeader() != null && isInParty(database.getPartyLeader())){
            return parties.get(database.getPartyLeader());
        }

        return null;
    }

}
