package io.taraxacum.finaltech.util;

import io.taraxacum.finaltech.FinalTech;
import me.mrCookieSlime.CSCoreLibPlugin.Configuration.Config;

import javax.annotation.Nonnull;

/**
 * @author Final_ROOT
 * @since 2.0
 */
public class AntiAccelerationUtil {
    public static String KEY = "anti-acceleration";

    /**
     * This method will determine if the given machine is accelerated.
     * @param config The storage info in the machine location
     * @return whether a machine can work
     */
    public static boolean isAccelerated(@Nonnull Config config) {
        String s = config.getString(KEY);
        if (s != null && Integer.parseInt(s) == FinalTech.getSlimefunTickCount()) {
            return true;
        }
        config.setValue(KEY, String.valueOf(FinalTech.getSlimefunTickCount()));
        return false;
    }
}
