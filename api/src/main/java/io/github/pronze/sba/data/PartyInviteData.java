package io.github.pronze.sba.data;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.scheduler.BukkitTask;
import io.github.pronze.sba.wrapper.SBAPlayerWrapper;

/**
 * Represents the data related to the Party Invites system.
 */
@RequiredArgsConstructor(access = AccessLevel.PUBLIC, staticName = "of")
@Getter
public class PartyInviteData {
    /**
     * Player that invited an invitee to his current party.
     */
    private final SBAPlayerWrapper player;

    /**
     * Player that has been invited to the party.
     */
    private final SBAPlayerWrapper invited;

    /**
     * Task that cancels the invite timer of the player.
     */
    private final BukkitTask inviteTask;
}
