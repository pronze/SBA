package io.github.pronze.sba.listener;

import io.github.pronze.sba.MessageKeys;
import io.github.pronze.sba.SBA;
import io.github.pronze.sba.lib.lang.LanguageService;
import io.github.pronze.sba.specials.SpawnerProtection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.screamingsandals.bedwars.Main;
import org.screamingsandals.lib.player.Players;
import org.screamingsandals.lib.utils.annotations.Service;
import org.screamingsandals.lib.utils.annotations.ServiceDependencies;
import org.screamingsandals.lib.utils.annotations.methods.OnPostEnable;

@Service
@ServiceDependencies(dependsOn = SpawnerProtection.class)
public class SpawnerProtectionListener implements Listener {

    @OnPostEnable
    public void registerListener() {
        if(SBA.isBroken())return;
        SBA.getInstance().registerListener(this);
    }


    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlace(BlockPlaceEvent event)
    {
        final var player = event.getPlayer();

        if (!Main.isPlayerInGame(player))
            return;
        
        final var playerGame = Main.getInstance().getGameOfPlayer(player);

        if (SpawnerProtection.getInstance().isProtected(playerGame, event.getBlock().getLocation()))
        {
            event.setCancelled(true);
            final var component = LanguageService
            .getInstance()
            .get(MessageKeys.SPAWNER_PROTECTION)
                    .toComponent();
            Players.wrapSender(player).sendMessage(component);
        }
    }
}
