package pronze.hypixelify.utils;

import org.bukkit.Bukkit;

public class Logger {
    private static Logger instance;
    private java.util.logging.Logger logger;
    private boolean debug;

    public static void init(boolean debug) {
        instance = new Logger();
        instance.debug = debug;
        instance.logger = java.util.logging.Logger.getLogger("SBAHypixelify");
    }

    public static void trace(String message, Object... params) {
        if (instance.debug) {
            for (Object obj : params) {
                message = message.replaceFirst("\\{}", obj.toString());
            }
            instance.logger.info(message);
        }
    }
}
