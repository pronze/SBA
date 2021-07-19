package io.github.pronze.sba.service;

import lombok.Getter;
import org.bukkit.event.Listener;
import org.screamingsandals.lib.plugin.ServiceManager;
import org.screamingsandals.lib.utils.annotations.Service;


@Service(dependsOn = {
  //      NPCManager.class
})
@Getter
public class NPCStoreService implements Listener {

    public static NPCStoreService getInstance() {
        return ServiceManager.get(NPCStoreService.class);
    }

//private final GameStore shopStore;
//private final GameStore upgradeStore;

//private NPCSkin shopSkin;
//private NPCSkin upgradeShopSkin;

//private final List<Component> shopText = new ArrayList<>();
//private final List<Component> upgradeShopText = new ArrayList<>();

//@SneakyThrows
//public NPCStoreService() {
//    shopStore = new GameStore(null, "shop.yml", false, SBAConfig.getInstance().node("shop", "normal-shop", "name").getString(), false, false);
//    upgradeStore = new GameStore(null, "upgradeShop.yml", false, SBAConfig.getInstance().node("shop", "upgrade-shop", "name").getString(), false, false);

//    shopText.clear();
//    shopText.addAll(Objects.requireNonNull(SBAConfig.getInstance().node("shop", "normal-shop", "entity-name")
//            .getList(String.class))
//            .stream()
//            .map(AdventureHelper::toComponent)
//            .collect(Collectors.toList()));

//    upgradeShopText.clear();
//    upgradeShopText.addAll(Objects.requireNonNull(SBAConfig.getInstance().node("shop", "upgrade-shop", "entity-name")
//            .getList(String.class))
//            .stream()
//            .map(AdventureHelper::toComponent)
//            .collect(Collectors.toList()));
//}

//@OnPostEnable
//public void onPostEnabled() {
//    SBA.getInstance().registerListener(this);
//    EventManager.getDefaultEventManager().register(NPCInteractEvent.class, this::onNPCTouched);

//    shopSkin = new NPCSkin(
//            SBAConfig.getInstance().node("shop", "normal-shop", "skin", "value").getString(),
//            SBAConfig.getInstance().node("shop", "normal-shop", "skin", "signature").getString()
//            );

//    upgradeShopSkin = new NPCSkin(
//            SBAConfig.getInstance().node("shop","upgrade-shop", "skin", "value").getString(),
//            SBAConfig.getInstance().node("shop", "upgrade-shop", "skin", "signature").getString()
//    );

//    EventManager.getDefaultEventManager().register(SPlayerJoinEvent.class, this::onPlayerJoin);
//}

//public void onPlayerJoin(SPlayerJoinEvent event) {
//    final var player = event.getPlayer();
//    NPC.of(LocationMapper.wrapLocation(player.getLocation()))
//            .setDisplayName(shopText)
//            .setShouldLookAtViewer(true)
//            .setSkin(shopSkin)
//            .addViewer(player)
//            .show();
//}

//public void onNPCTouched(NPCInteractEvent event) {
//    Logger.trace("Clicked NPC with click type: {}", event.getInteractType().name());
//    if (event.getInteractType() == NPCInteractEvent.InteractType.LEFT_CLICK) {
//        return;
//    }

//    final var player = event.getPlayer().as(Player.class);
//    if (!Main.getInstance().isPlayerPlayingAnyGame(player)) {
//        return;
//    }

//    final var game = Main.getInstance().getGameOfPlayer(player);
//    final var npc = event.getNpc();
//    ArenaManager
//            .getInstance()
//            .getArenaMap()
//            .values()
//            .stream()
//            .filter(iArena -> iArena.getStoreNPCS().contains(npc) || iArena.getUpgradeStoreNPCS().contains(npc))
//            .forEach(arena -> {
//                GameStore store = arena.getStoreNPCS().contains(npc) ? shopStore : upgradeStore;
//                BedwarsOpenShopEvent openShopEvent = new BedwarsOpenShopEvent(game,
//                        player, store, null);
//                Bukkit.getServer().getPluginManager().callEvent(openShopEvent);
//            });
//}
}
