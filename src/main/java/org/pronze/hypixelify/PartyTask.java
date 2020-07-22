package org.pronze.hypixelify;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;
import org.pronze.hypixelify.Party.Party;
import org.pronze.hypixelify.database.PlayerDatabase;

public class PartyTask extends BukkitRunnable {

    public PartyTask(){
        runTaskTimer(Hypixelify.getInstance(), 20L, 20L);
    }

    @Override
    public void run() {
        for(PlayerDatabase playerDatabase : Hypixelify.getInstance().playerData.values()){
            if(playerDatabase != null){
                playerDatabase.updateDatabase();
                Bukkit.getLogger().info("Player: " + playerDatabase.getName());
                Bukkit.getLogger().info("Leader: " + playerDatabase.getPartyLeader());
                Bukkit.getLogger().info("IsInParty: " + playerDatabase.isInParty());
            }

        }

        //more debug info
        for(Party party : Hypixelify.getInstance().partyManager.parties.values()){
            if(party != null){
                Bukkit.getLogger().info("Party: " + party.getLeader().getName());
            }
        }
    }

}
