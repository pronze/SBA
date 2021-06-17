package pronze.sba.utils;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.plugin.java.JavaPlugin;
import org.screamingsandals.lib.utils.annotations.Service;
import pronze.sba.config.SBAConfig;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class Logger {
    private static Logger instance;
    private Level level;
    private java.util.logging.Logger logger;

    public static void init(JavaPlugin plugin) {
        instance = new Logger();
        instance.level = Level.ALL;
        instance.logger = plugin.getLogger();
        if (!SBAConfig.getInstance().node("debug", "enabled").getBoolean()) {
            instance.level = Level.DISABLED;
        }
    }

    public static void trace(@NonNull String message, Object... params) {
        if (instance.level.getLevel() >= Level.TRACE.getLevel()) {
            instance.logger.info(getMessage(message, params));
        }
    }

    public static void warn(@NonNull String message, Object... params) {
        if (instance.level.getLevel() >= Level.WARNING.getLevel()) {
            instance.logger.warning(getMessage(message, params));
        }
    }

    public static void error(@NonNull String message, Object... params) {
        if (instance.level.getLevel() >= Level.ERROR.getLevel()) {
            instance.logger.warning(getMessage(message, params));
        }
    }

    private static String getMessage(String message, Object... params) {
        for (var param : params) {
            message = message.replaceFirst(Pattern.quote("{}"), Matcher.quoteReplacement(param.toString()));
        }
        return message;
    }

    public static void setMode(Level level) {
        instance.level = level;
    }

    public enum Level {
        DISABLED(0),
        TRACE(1),
        WARNING(2),
        ERROR(3),
        ALL(4);

        @Getter
        private final int level;

        Level(int level) {
            this.level = level;
        }
    }
}
