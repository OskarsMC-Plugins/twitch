package com.oskarsmc.twitch.util;

import com.moandjiezana.toml.Toml;
import com.oskarsmc.twitch.configuration.TwitchSettings;

public class VersionUtils {
    public static final double CONFIG_VERSION = getDefaultConfiguration().getDouble("developer-info.config-version");

    public static boolean isLatestConfigVersion(TwitchSettings twitchSettings) {
        if (twitchSettings.getConfigVersion() == null) {
            return false;
        }
        return twitchSettings.getConfigVersion() == CONFIG_VERSION;
    }

    public static Toml getDefaultConfiguration() {
        return new Toml().read(VersionUtils.class.getResourceAsStream("/config.toml"));
    }
}
