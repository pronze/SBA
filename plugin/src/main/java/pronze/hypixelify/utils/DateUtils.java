package pronze.hypixelify.utils;
import org.screamingsandals.lib.plugin.ServiceManager;
import org.screamingsandals.lib.utils.annotations.Service;
import pronze.hypixelify.config.SBAConfig;

import java.text.SimpleDateFormat;
import java.util.Date;

@Service(dependsOn = {
        SBAConfig.class
})
public class DateUtils {

    public static DateUtils getInstance() {
        return ServiceManager.get(DateUtils.class);
    }

    private final SimpleDateFormat format;

    public DateUtils() {
        format = new SimpleDateFormat(SBAConfig.getInstance().node("date", "format").getString("MM/dd/yy"));
    }

    public static SimpleDateFormat getSimpleDateFormat() {
        return getInstance().format;
    }

    public static String getFormattedDate() {
        return getSimpleDateFormat().format(new Date());
    }
}
