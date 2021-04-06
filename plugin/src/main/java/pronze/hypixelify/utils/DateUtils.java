package pronze.hypixelify.utils;
import pronze.hypixelify.api.SBAHypixelifyAPI;
import pronze.lib.core.Core;
import pronze.lib.core.annotations.AutoInitialize;

import java.text.SimpleDateFormat;
import java.util.Date;

@AutoInitialize
public class DateUtils {
    private final SimpleDateFormat format;

    public static DateUtils getInstance() {
        return Core.getObjectFromClass(DateUtils.class);
    }

    public DateUtils() {
        format = new SimpleDateFormat(SBAHypixelifyAPI.getInstance().getConfigurator0().getString("date.format", "MM/dd/yy"));
    }

    public static SimpleDateFormat getSimpleDateFormat() {
        return getInstance().format;
    }

    public static String getFormattedDate() {
        return getSimpleDateFormat().format(new Date());
    }
}
