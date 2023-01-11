package io.github.pronze.sba.commands.party;

import cloud.commandframework.annotations.CommandMethod;
import cloud.commandframework.annotations.CommandPermission;
import io.github.pronze.sba.MessageKeys;
import io.github.pronze.sba.SBA;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.screamingsandals.lib.player.PlayerMapper;
import org.screamingsandals.lib.utils.annotations.Service;
import org.screamingsandals.lib.utils.annotations.methods.OnPostEnable;
import io.github.pronze.sba.commands.CommandManager;
import io.github.pronze.sba.config.SBAConfig;
import io.github.pronze.sba.lib.lang.LanguageService;
import io.github.pronze.sba.party.PartyManager;
import io.github.pronze.sba.wrapper.PlayerSetting;
import org.screamingsandals.lib.spectator.Component;

@Service
public class PartyListCommand {

    static boolean init = false;

    @OnPostEnable
    public void onPostEnabled() {
        if(SBA.isBroken())return;
        if (init)
            return;
        if (SBAConfig.getInstance().party().enabled())
            CommandManager.getInstance().getAnnotationParser().parse(this);
        init = true;
    }

    @CommandMethod("party|p list")
    @CommandPermission("sba.party")
    private void commandList(
            final @NotNull Player sender) {
        final var player = SBA.getInstance().getPlayerWrapper((sender));

        if (!player.getSettings().isToggled(PlayerSetting.IN_PARTY)) {
            LanguageService
                    .getInstance()
                    .get(MessageKeys.PARTY_MESSAGE_NOT_IN_PARTY)
                    .send(player);
            return;
        }
        var optionnalParty = PartyManager.getInstance().getPartyOf(player);
        optionnalParty.ifPresent(party -> {

            Component leader = (party.getPartyLeader().getDisplayName());
            List<Component> members = new ArrayList<>();
            party.getMembers().forEach(member -> {
                if (member != party.getPartyLeader())
                    members.add(member.getDisplayName());
            });

            Component membersComponent = Component.join((Component.text(",")), members);

            LanguageService
                    .getInstance()
                    .get(MessageKeys.PARTY_LIST)
                    .replace("%count%", String.valueOf(party.getMembers().size()))
                    .replace("%leader%", (leader))
                    .replace("%members%", (membersComponent))
                    .send(PlayerMapper.wrapPlayer(sender));
        });

    }

}
