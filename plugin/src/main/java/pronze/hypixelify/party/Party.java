package pronze.hypixelify.party;

import pronze.hypixelify.SBAHypixelify;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import pronze.hypixelify.utils.MessageUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Party implements pronze.hypixelify.api.party.Party {

    private int member_size = 0;
    private List<Player> players = new ArrayList<>();
    private List<Player> invitedMembers = new ArrayList<>();
    private Player leader;
    private final boolean anyoneCanInvite = true;

    public Party(Player leader) {
        setLeader(leader);
        if(players.contains(leader)) return;
        players.add(leader);
    }

    public void sendMessage(String msg) {
        getAllPlayers().forEach(p->p.sendMessage(msg));
    }

    @Override
    public void sendChat(Player sender, String msg) {
        msg = "§aParty>> " + sender.getName() + "§f : §o"  + msg;
        sendMessage(msg);
    }

    public List<Player> getOfflinePlayers() {
        final var offlinePlayers = new ArrayList<Player>();
        players.stream()
                .filter(Objects::nonNull)
                .forEach(player->{
            if(!player.isOnline() || Bukkit.getPlayer(player.getUniqueId()) == null){
                offlinePlayers.add(player);
            }
        });
        if (offlinePlayers.isEmpty()) return null;
        return offlinePlayers;
    }

    @Override
    public boolean shouldDisband(){
        return getPlayers() == null && getInvitedMembers() == null && getOfflinePlayers() == null;
    }

    @Override
    public void disband() {
        players.clear();
        invitedMembers.clear();
        invitedMembers = null;
        players = null;
        setLeader(null);
    }

    public void setLeader(Player player) {
        leader = player;
    }

   public void addInvitedMember(Player pl) {
       if (!invitedMembers.contains(pl)) {
           invitedMembers.add(pl);
           final var wrapper = SBAHypixelify.getWrapperService().getWrapper(pl);
           wrapper.setInvitedParty(this);
           wrapper.setInvited(true);
       }
   }

    @Override
    public List<Player> getInvitedMembers() {
        if(invitedMembers == null || invitedMembers.isEmpty()) return null;
        return invitedMembers;
    }

    @Override
    public void removeInvitedMember(Player pl) {
        invitedMembers.remove(pl);
    }

    @Override
   public void addMember(Player player) {
       if (!players.contains(player)) {
           players.add(player);
           member_size++;
       }
   }

    @Override
    public int getSize() {
        return member_size;
    }

    @Override
    public List<Player> getPlayers() {
        if (leader == null)
            return null;
        final var list = getAllPlayers();
        if (list == null)
            return null;
        list.remove(leader);
        if(list.isEmpty())
            return null;
        return list;
    }

    @Override
    public void removeMember(Player player) {
        if (player.equals(leader)) {
            getPlayers().forEach(pl-> MessageUtils.sendMessage("party.message.disband-inactivity", pl));
            Bukkit.getScheduler().runTask(SBAHypixelify.getInstance(), ()->
                    SBAHypixelify.getPartyManager().disband(player));
            return;
        }
        players.remove(player);
    }

    @Override
    public int getCompleteSize(){
        if( players == null || invitedMembers == null) return 0;
        return players.size() + invitedMembers.size();
    }

    @Override
    public boolean canAnyoneInvite() {
        return anyoneCanInvite;
    }

    @Override
    public Player getLeader(){
        return leader;
    }

    @Override
    public List<Player> getAllPlayers(){
        final var newPlayerList = new ArrayList<Player>();

        players.stream()
                .filter(Objects::nonNull)
                .forEach(player->{
            if(!player.isOnline()){
                return;
            }
            newPlayerList.add(player);
        });

        if(newPlayerList.isEmpty())
            return null;

        return newPlayerList;
    }
}
