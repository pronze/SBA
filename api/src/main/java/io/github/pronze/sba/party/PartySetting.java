package io.github.pronze.sba.party;
import lombok.Data;
import lombok.RequiredArgsConstructor;

/**
 * TODO:
 * Represents the settings of the party.
 * Configurable via /p settings <enum> <setting>
 */
@RequiredArgsConstructor
@Data
public class PartySetting {
    private Chat chat = Chat.UNMUTE;
    private Invite invite = Invite.ALL;
    private AutoJoin autoJoin = AutoJoin.ENABLED;

    public enum Chat {
        MUTED,
        UNMUTE;
    }

    public enum Invite {
        NONE,
        ALL,
    }

    public enum AutoJoin {
        ENABLED,
        DISABLED
    }
}
