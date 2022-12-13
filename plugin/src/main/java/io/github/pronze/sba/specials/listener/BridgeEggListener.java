package io.github.pronze.sba.specials.listener;

import io.github.pronze.sba.SBA;
import io.github.pronze.sba.specials.runners.BridgeEggRunnable;
import io.github.pronze.sba.utils.SBAUtil;
import org.bukkit.entity.Egg;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.screamingsandals.bedwars.Main;
import org.screamingsandals.bedwars.api.APIUtils;
import org.screamingsandals.bedwars.api.events.BedwarsApplyPropertyToBoughtItem;
import org.screamingsandals.bedwars.api.game.Game;
import org.screamingsandals.bedwars.api.game.GameStatus;
import org.screamingsandals.bedwars.game.GamePlayer;
import org.screamingsandals.lib.utils.annotations.Service;
import org.screamingsandals.lib.utils.annotations.methods.OnPostEnable;

import java.util.HashMap;
import java.util.Map;

@Service
public class BridgeEggListener implements Listener {
    private static final String BRIDGE_EGG_PREFIX = "Module:BridgeEgg:";
    private final Map<Egg, BridgeEggRunnable> bridgeTasks = new HashMap<>();

    @OnPostEnable
    public void onPostEnable() {
        if(SBA.isBroken())return;
        SBA.getInstance().registerListener(this);
    }

    @EventHandler
    public void onEggRegistration(BedwarsApplyPropertyToBoughtItem event) {
        if (event.getPropertyName().equalsIgnoreCase("bridgeegg")) {
            ItemStack stack = event.getStack();
            APIUtils.hashIntoInvisibleString(stack, applyProperty(event));
        }
    }

    @EventHandler
    public void onEggUse(PlayerInteractEvent event) {
        final var player = event.getPlayer();
        if (!Main.isPlayerInGame(player)) {
            return;
        }

        GamePlayer gamePlayer = Main.getPlayerGameProfile(player);
        Game game = gamePlayer.getGame();
        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (game.getStatus() == GameStatus.RUNNING && !gamePlayer.isSpectator && event.getItem() != null) {
                ItemStack stack = event.getItem();
                String unhidden = APIUtils.unhashFromInvisibleStringStartsWith(stack, BRIDGE_EGG_PREFIX);
                if (unhidden != null) {
                    event.setCancelled(true);
                    stack.setAmount(stack.getAmount() - 1);
                    player.updateInventory();
                    final var egg = player.launchProjectile(Egg.class);
                    final var playerTeam = game.getTeamOfPlayer(player);
                    bridgeTasks.put(egg, new BridgeEggRunnable(egg, playerTeam, player, game));
                }
            }
        }
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        if (!(event.getEntity() instanceof Egg)) {
            return;
        }

        final var egg = (Egg) event.getEntity();
        if (bridgeTasks.containsKey(egg)) {
            egg.remove();
            SBAUtil.cancelTask(bridgeTasks.get(egg).getTask());
            bridgeTasks.remove(egg);
        }
    }

    //TODO: configurable properties
    private String applyProperty(BedwarsApplyPropertyToBoughtItem event) {
        return BRIDGE_EGG_PREFIX;
    }
}
