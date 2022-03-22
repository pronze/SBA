package io.github.pronze.sba.listener;

import io.github.pronze.sba.MessageKeys;
import io.github.pronze.sba.Permissions;
import io.github.pronze.sba.SBA;
import io.github.pronze.sba.config.SBAConfig;
import io.github.pronze.sba.data.DegradableItem;
import io.github.pronze.sba.game.ArenaManager;
import io.github.pronze.sba.lib.lang.LanguageService;
import io.github.pronze.sba.specials.SpawnerProtection;
import io.github.pronze.sba.utils.SBAUtil;
import io.github.pronze.sba.utils.ShopUtil;
import io.github.pronze.sba.wrapper.SBAPlayerWrapper;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
import org.bukkit.scheduler.BukkitRunnable;
import org.screamingsandals.bedwars.Main;
import org.screamingsandals.bedwars.api.game.GameStatus;
import org.screamingsandals.bedwars.game.GamePlayer;
import org.screamingsandals.lib.player.PlayerMapper;
import org.screamingsandals.lib.utils.annotations.Service;
import org.screamingsandals.lib.utils.annotations.methods.OnPostEnable;
import io.github.pronze.lib.pronzelib.scoreboards.Scoreboard;
import io.github.pronze.lib.pronzelib.scoreboards.ScoreboardManager;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Service(dependsOn = SpawnerProtection.class)
public class SpawnerProtectionListener implements Listener {

    @OnPostEnable
    public void registerListener() {
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
            PlayerMapper.wrapSender(player).sendMessage(component);
        }
    }
}
