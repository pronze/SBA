package pronze.hypixelify.api.party;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Data
public class PartySetting {
    private Chat chat = Chat.MUTED;
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
