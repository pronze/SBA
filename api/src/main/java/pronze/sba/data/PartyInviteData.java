package pronze.sba.data;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.scheduler.BukkitTask;
import pronze.sba.wrapper.PlayerWrapper;

@RequiredArgsConstructor
@Getter
public class PartyInviteData {
    private final PlayerWrapper player;
    private final PlayerWrapper invited;
    private final BukkitTask inviteTask;
}
