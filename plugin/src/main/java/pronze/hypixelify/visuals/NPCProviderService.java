package pronze.hypixelify.visuals;

import lombok.Getter;
import net.jitse.npclib.NPCLib;
import org.screamingsandals.bedwars.lib.utils.annotations.Service;
import pronze.hypixelify.SBAHypixelify;
import pronze.hypixelify.registry.NPCRegistry;
import pronze.lib.core.Core;
import pronze.lib.core.annotations.AutoInitialize;
import pronze.lib.core.annotations.OnInit;

@AutoInitialize
@Getter
public class NPCProviderService {
    private NPCLib library;
    private NPCRegistry<String> registry;

    public static NPCProviderService getInstance() {
        return Core.getObjectFromClass(NPCProviderService.class);
    }

    @OnInit
    public void init() {
        library = new NPCLib(SBAHypixelify.getInstance());
        registry = new NPCRegistry<>();
    }
}
