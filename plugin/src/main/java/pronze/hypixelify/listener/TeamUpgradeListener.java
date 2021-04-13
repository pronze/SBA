package pronze.hypixelify.listener;

import org.screamingsandals.bedwars.lib.player.PlayerMapper;
import pronze.hypixelify.SBAHypixelify;
import pronze.hypixelify.api.MessageKeys;
import pronze.hypixelify.api.events.SBATeamUpgradePurchaseEvent;
import pronze.hypixelify.config.SBAConfig;
import pronze.hypixelify.lib.lang.LanguageService;
import pronze.hypixelify.utils.SBAUtil;
import pronze.hypixelify.utils.ShopUtil;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import pronze.lib.core.annotations.AutoInitialize;

import java.util.HashMap;
import java.util.Map;


@AutoInitialize(listener = true)
public class TeamUpgradeListener implements Listener {

    public static Map<Integer, Integer> sharpnessPrices = new HashMap<>();
    public static Map<Integer, Integer> protectionPrices = new HashMap<>();

    public TeamUpgradeListener() {
        sharpnessPrices.put(0, SBAConfig.getInstance().node("upgrades","prices","Sharpness-I").getInt(4));
        sharpnessPrices.put(1, SBAConfig.getInstance().node("upgrades","prices","Sharpness-I").getInt( 4));
        sharpnessPrices.put(2, SBAConfig.getInstance().node("upgrades","prices","Sharpness-II").getInt(8));
        sharpnessPrices.put(3, SBAConfig.getInstance().node("upgrades","prices","Sharpness-III").getInt(12));
        sharpnessPrices.put(4, SBAConfig.getInstance().node("upgrades","prices","Sharpness-IV").getInt(16));

        protectionPrices.put(0, SBAConfig.getInstance().node("upgrades","prices","Prot-I").getInt(4));
        protectionPrices.put(1, SBAConfig.getInstance().node("upgrades","prices","Prot-I").getInt( 4));
        protectionPrices.put(2, SBAConfig.getInstance().node("upgrades","prices","Prot-II").getInt(8));
        protectionPrices.put(3, SBAConfig.getInstance().node("upgrades","prices","Prot-III").getInt(12));
        protectionPrices.put(4, SBAConfig.getInstance().node("upgrades","prices","Prot-IV").getInt(16));
    }

    @EventHandler
    public void onPlayerToolUpgrade(SBATeamUpgradePurchaseEvent e) {
        final var player = e.getPlayer();
        final var wrappedPlayer = PlayerMapper.wrapPlayer(player);
        final var newItem = e.getUpgradedItem();
        final var team = e.getTeam();
        final var name = e.getName().toLowerCase();
        final var game = e.getGame();
        final var gameStorage = SBAHypixelify
                .getInstance()
                .getArenaManager()
                .getGameStorage(game.getName())
                .orElseThrow();


        switch (name) {
            case "sharpness":
                if (!ShopUtil.addEnchantsToTeamTools(player, newItem, "SWORD", Enchantment.DAMAGE_ALL)) {
                    e.setCancelled(true);
                    LanguageService
                            .getInstance()
                            .get(MessageKeys.GREATEST_ENCHANTMENT)
                            .send(wrappedPlayer);
                } else {
                    var level = newItem.getEnchantmentLevel(Enchantment.DAMAGE_ALL);
                    var price = sharpnessPrices.get(level);
                    var materialItem = e.getStackFromPrice(price);
                    if (player.getInventory().containsAtLeast(materialItem, materialItem.getAmount())) {
                        gameStorage.setSharpness(team.getName(), level);
                    }
                    e.setPrice(Integer.toString(price));
                }
                break;

            case "efficiency":
                if (!ShopUtil.addEnchantsToTeamTools(player, newItem, "PICKAXE", Enchantment.DIG_SPEED)) {
                    e.setCancelled(true);
                    LanguageService
                            .getInstance()
                            .get(MessageKeys.GREATEST_ENCHANTMENT)
                            .send(wrappedPlayer);
                }
                break;
            case "blindtrap":
                    if (gameStorage.isTrapEnabled(team)) {
                        LanguageService
                                .getInstance()
                                .get(MessageKeys.WAIT_FOR_TRAP)
                                .send(wrappedPlayer);
                        e.setCancelled(true);
                    } else {
                        final var blindnessTrapTitle = LanguageService
                                .getInstance()
                                .get(MessageKeys.BLINDNESS_TRAP_PURCHASED_TITLE)
                                .toString();

                        gameStorage.setTrap(team, true);
                        team.getConnectedPlayers().forEach(pl ->
                                SBAUtil.sendTitle(PlayerMapper.wrapPlayer(pl), blindnessTrapTitle, "", 20, 40, 20));
                    }
                    break;

            case "healpool":
                if (gameStorage.isPoolEnabled(team)) {
                    LanguageService
                            .getInstance()
                            .get(MessageKeys.WAIT_FOR_TRAP)
                            .send(wrappedPlayer);
                    e.setCancelled(true);
                } else {
                    var purchaseHealPoolMessage = LanguageService
                            .getInstance()
                            .get(MessageKeys.PURCHASED_HEAL_POOL_MESSAGE)
                            .replace("%player%", player.getName())
                            .toComponent();

                    gameStorage.setPool(team, true);
                    team.getConnectedPlayers().forEach(pl -> PlayerMapper.wrapPlayer(pl).sendMessage(purchaseHealPoolMessage));
                }
                break;

            case "protection":
                if (gameStorage.getProtection(team.getName()) >= 4) {
                    LanguageService
                            .getInstance()
                            .get(MessageKeys.GREATEST_ENCHANTMENT)
                            .send(wrappedPlayer);
                    e.setCancelled(true);
                } else {
                    var level = newItem.getEnchantmentLevel(Enchantment.PROTECTION_ENVIRONMENTAL);
                    var price = protectionPrices.get(level);
                    var materialItem = e.getStackFromPrice(price);

                    if (player.getInventory().containsAtLeast(materialItem, materialItem.getAmount())) {
                        gameStorage.setProtection(team.getName(), level);
                    }

                    e.setPrice(Integer.toString(price));
                    ShopUtil.addEnchantsToPlayerArmor(player, newItem);

                    var upgradeMessage = LanguageService
                            .getInstance()
                            .get(MessageKeys.UPGRADE_TEAM_PROTECTION)
                            .replace("%player%", player.getName())
                            .toComponent();

                    team.getConnectedPlayers().forEach(teamPlayer -> {
                        ShopUtil.addEnchantsToPlayerArmor(teamPlayer, newItem);
                        PlayerMapper.wrapPlayer(teamPlayer).sendMessage(upgradeMessage);
                    });
                }
                break;
        }
    }
}
