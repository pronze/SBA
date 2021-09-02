package io.github.pronze.sba.specials.listener;

import io.github.pronze.sba.SBA;
import io.github.pronze.sba.config.SBAConfig;
import io.github.pronze.sba.specials.PopupTower;
import io.github.pronze.sba.utils.SBAUtil;
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
                    stack.setAmount(stack.getAmount() - 1);
                    player.updateInventory();
                    final var team = game.getTeamOfPlayer(player);
                    PopupTower tower = new PopupTower(game, TeamColor.fromApiColor(team.getColor()).getWool().getType(), player.getLocation().getBlock().getRelative(SBAUtil.yawToFace(player.getLocation().getYaw()), 3).getLocation());
                    tower.setHeight(SBAConfig.getInstance().node("popup-tower", "height").getInt(10));
                    tower.setWidth(SBAConfig.getInstance().node("popup-tower", "width").getInt(4));
                    tower.createTower(SBAConfig.getInstance().node("popup-tower", "floor").getBoolean(false), SBAUtil.yawToFace(player.getLocation().getYaw()).getOppositeFace());
                }
            }
        }
    }

    //TODO: configurable properties
    private String applyProperty(BedwarsApplyPropertyToBoughtItem event) {
        return POPUP_TOWER_PREFIX;
    }
}
