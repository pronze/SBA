package pronze.hypixelify.api.data;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.scheduler.BukkitTask;
import pronze.hypixelify.api.wrapper.PlayerWrapper;

@RequiredArgsConstructor
@Getter
public class PartyInviteData {
    private final PlayerWrapper player;
    private final PlayerWrapper invited;
    private final BukkitTask inviteTask;
}
