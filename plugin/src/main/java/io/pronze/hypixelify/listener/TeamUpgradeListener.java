package io.pronze.hypixelify.listener;

import io.pronze.hypixelify.SBAHypixelify;
import io.pronze.hypixelify.api.events.TeamUpgradePurchaseEvent;
import io.pronze.hypixelify.utils.ShopUtil;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.HashMap;
import java.util.Map;

import static org.screamingsandals.bedwars.lib.nms.title.Title.sendTitle;

public class TeamUpgradeListener implements Listener {

    public static Map<Integer, Integer> prices = new HashMap<>();

    public TeamUpgradeListener() {
        prices.put(0, SBAHypixelify.getConfigurator().config.getInt("upgrades.prices.Sharpness-Prot-I", 4));
        prices.put(1, SBAHypixelify.getConfigurator().config.getInt("upgrades.prices.Sharpness-Prot-I", 4));
        prices.put(2, SBAHypixelify.getConfigurator().config.getInt("upgrades.prices.Sharpness-Prot-II", 8));
        prices.put(3, SBAHypixelify.getConfigurator().config.getInt("upgrades.prices.Sharpness-Prot-III", 12));
        prices.put(4, SBAHypixelify.getConfigurator().config.getInt("upgrades.prices.Sharpness-Prot-IV", 16));
    }

    @EventHandler
    public void onPlayerToolUpgrade(TeamUpgradePurchaseEvent e) {
        final var player = e.getPlayer();
        final var newItem = e.getUpgradedItem();
        final var team = e.getTeam();
        final var name = e.getName();
        final var game = e.getGame();
        final var gameStorage = SBAHypixelify.getGamestorage(game);

        if (gameStorage == null) {
            e.setCancelled(true);
            player.sendMessage(SBAHypixelify.getConfigurator().getString("message.error-occured"));
            return;
        }

        if (name.equalsIgnoreCase("sharpness")) {
            if (!ShopUtil.addEnchantsToTeamTools(player, newItem, "SWORD", Enchantment.DAMAGE_ALL)) {
                e.setCancelled(true);
                player.sendMessage(SBAHypixelify.getConfigurator().getString("message.greatest-enchantment"));
            } else {
                var level = newItem.getEnchantmentLevel(Enchantment.DAMAGE_ALL);
                var price = prices.get(level);
                var materialItem = e.getStackFromPrice(price);
                if (player.getInventory().containsAtLeast(materialItem, materialItem.getAmount())) {
                    gameStorage.setSharpness(team.getName(), level);
                }
                e.setPrice(Integer.toString(price));
            }
        }//else if (name.equalsIgnoreCase("dragon")) {
        //  if (gameStorage.isTrapEnabled(team)) {
        //      player.sendMessage(SBAHypixelify.getConfigurator().getString("message.wait-trap"));
        //      e.setCancelled(true);
        //  } else {
        //      gameStorage.setDragon(team, true);
        //      team.getConnectedPlayers().forEach(pl -> sendTitle(pl, SBAHypixelify.getConfigurator().getString("message.dragon-trap-purchased"), "", 20, 40, 20));
        //  }
        //  }

        else if (name.equalsIgnoreCase("efficiency")) {
            if (!ShopUtil.addEnchantsToTeamTools(player, newItem, "PICKAXE", Enchantment.DIG_SPEED)) {
                e.setCancelled(true);
                player.sendMessage(SBAHypixelify.getConfigurator().getString("message.greatest-enchantment"));
            }
        } else if (name.equalsIgnoreCase("blindtrap")) {
            if (gameStorage.isTrapEnabled(team)) {
                player.sendMessage(SBAHypixelify.getConfigurator().getString("message.wait-trap"));
                e.setCancelled(true);
            } else {
                gameStorage.setTrap(team, true);
                team.getConnectedPlayers().forEach(pl -> sendTitle(pl, SBAHypixelify.getConfigurator().getString("message.blindness-trap-purchased-title"), "", 20, 40, 20));
            }
        } else if (name.equalsIgnoreCase("healpool")) {
            if (gameStorage.isPoolEnabled(team)) {
                player.sendMessage(SBAHypixelify.getConfigurator().getString("message.wait-trap"));
                e.setCancelled(true);
            } else {
                gameStorage.setPool(team, true);
                team.getConnectedPlayers().forEach(pl -> pl.sendMessage(SBAHypixelify.getConfigurator().getString("message.purchase-heal-pool")
                        .replace("{player}", player.getName())));
            }
        } else if (name.equalsIgnoreCase("protection")) {
            if (gameStorage.getProtection(team.getName()) >= 4) {
                e.setCancelled(true);
                player.sendMessage("§c§l" + SBAHypixelify.getConfigurator().getString("message.greatest-enchantment"));
            } else {
                var level = newItem.getEnchantmentLevel(Enchantment.PROTECTION_ENVIRONMENTAL);
                var price = prices.get(level);
                var materialItem = e.getStackFromPrice(price);
                if (player.getInventory().containsAtLeast(materialItem, materialItem.getAmount())) {
                    gameStorage.setProtection(team.getName(), level);
                }
                e.setPrice(Integer.toString(price));
                ShopUtil.addEnchantsToPlayerArmor(player, newItem);
                team.getConnectedPlayers().forEach(teamPlayer -> {
                    ShopUtil.addEnchantsToPlayerArmor(teamPlayer, newItem);
                    teamPlayer.sendMessage(SBAHypixelify.getConfigurator()
                            .getString("message.upgrade-team-protection")
                            .replace("{player}", player.getName()));
                });
            }
        }
    }
}
