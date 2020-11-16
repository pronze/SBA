package org.pronze.hypixelify.placeholderapi;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.pronze.hypixelify.SBAHypixelify;
import org.pronze.hypixelify.database.PlayerDatabase;

public class SBAExpansion extends PlaceholderExpansion {
    @Override
    public @NotNull String getIdentifier() {
        return "sba";
    }

    @Override
    public @NotNull String getAuthor() {
        return "pronze";
    }

    @Override
    public @NotNull String getVersion() {
        return SBAHypixelify.getVersion();
    }

    @Override
    public String onPlaceholderRequest(Player player, @NotNull String identifier) {


        if(identifier.startsWith("sbaplayer_")){
            final PlayerDatabase database = SBAHypixelify.getDatabaseManager().getDatabase(player);
            if(database == null){
                return null;
            }
            switch (identifier.substring(10).toLowerCase()){
                case "level":
                    return Integer.toString(database.getLevel());
            }
        }

        return super.onPlaceholderRequest(player, identifier);
    }
}
