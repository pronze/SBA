package org.pronze.hypixelify.utils;

import com.alessiodp.parties.api.Parties;
import com.alessiodp.parties.api.interfaces.PartiesAPI;
import com.alessiodp.parties.api.interfaces.Party;
import com.alessiodp.parties.api.interfaces.PartyPlayer;

public class PartyUtil {

    public static boolean checkPartyLeader(PartyPlayer player) {
        PartiesAPI api = Parties.getApi();

        if (player != null || player.getPartyName() != null || !player.getPartyName().isEmpty()) {
            Party party = api.getParty(player.getPartyName());
            if (party != null && party.getLeader() != null && player.getPlayerUUID().equals(party.getLeader())) {
                    return true;
            }
        }
        return false;
    }

}
