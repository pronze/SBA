package io.github.pronze.sba.specials.listener;

import io.github.pronze.sba.SBA;
import io.github.pronze.sba.config.SBAConfig;
import io.github.pronze.sba.specials.PopupTower;
import io.github.pronze.sba.specials.SpawnerProtection;
import io.github.pronze.sba.utils.SBAUtil;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.screamingsandals.bedwars.Main;
import org.screamingsandals.bedwars.api.APIUtils;
import org.screamingsandals.bedwars.api.events.BedwarsApplyPropertyToBoughtItem;
import org.screamingsandals.bedwars.api.game.Game;
import org.screamingsandals.bedwars.api.game.GameStatus;
import org.screamingsandals.bedwars.game.GamePlayer;
import org.screamingsandals.bedwars.game.TeamColor;
import org.screamingsandals.lib.utils.annotations.Service;
import org.screamingsandals.lib.utils.annotations.methods.OnPostEnable;

@Service
public class PopupTowerListener implements Listener {
    private static final String POPUP_TOWER_PREFIX = "Module:PopupTower:";

    @OnPostEnable
    public void onPostEnable() {
        if(SBA.isBroken())return;
        SBA.getInstance().registerListener(this);
    }

    @EventHandler
    public void onTowerRegistration(BedwarsApplyPropertyToBoughtItem event) {
        if (event.getPropertyName().equalsIgnoreCase("PopupTower")) {
            ItemStack stack = event.getStack();
            APIUtils.hashIntoInvisibleString(stack, applyProperty(event));
        }
    }

    @EventHandler
    public void onPopupTowerUse(PlayerInteractEvent event) {
        final var player = event.getPlayer();
        if (!Main.isPlayerInGame(player)) {
            return;
        }

        GamePlayer gamePlayer = Main.getPlayerGameProfile(player);
        Game game = gamePlayer.getGame();
        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (game.getStatus() == GameStatus.RUNNING && !gamePlayer.isSpectator && event.getItem() != null) {
                ItemStack stack = event.getItem();
                String unhidden = APIUtils.unhashFromInvisibleStringStartsWith(stack, POPUP_TOWER_PREFIX);
                if (unhidden != null) {
                    event.setCancelled(true);
                    if (stack.getAmount() > 1) {
                        stack.setAmount(stack.getAmount() - 1);
                    } else {
                        try {
                            if (player.getInventory().getItemInOffHand().equals(stack)) {
                                player.getInventory().setItemInOffHand(new ItemStack(Material.AIR));
                            } else {
                                player.getInventory().remove(stack);
                            }
                        } catch (Throwable e) {
                            player.getInventory().remove(stack);
                        }
                    }
                    player.updateInventory();
                    final var team = game.getTeamOfPlayer(player);
                    final var playerFace = SBAUtil.yawToFace(player.getLocation().getYaw(), false);
                    final var wool =  TeamColor.fromApiColor(team.getColor()).getWool();
                    PopupTower tower = new PopupTower(
                            game,
                            wool.getType(),
                            Main.isLegacy() ? wool.getData().getData() : 0,
                            player.getLocation().getBlock().getRelative(playerFace).getRelative(BlockFace.DOWN)
                                    .getLocation(),
                            playerFace);
                    if (!SpawnerProtection.getInstance().isProtected(game, player.getLocation().getBlock()
                            .getRelative(playerFace).getRelative(BlockFace.DOWN).getLocation()))
                        tower.createTower();
                }
            }
        }
    }

    // TODO: configurable properties
    private String applyProperty(BedwarsApplyPropertyToBoughtItem event) {
        return POPUP_TOWER_PREFIX;
    }
}
