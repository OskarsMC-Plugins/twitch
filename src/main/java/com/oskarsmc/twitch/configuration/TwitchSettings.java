package com.oskarsmc.twitch.configuration;

import com.moandjiezana.toml.Toml;
import com.oskarsmc.twitch.util.VersionUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

public class TwitchSettings {
    private final File dataFolder;
    private final File file;

    private final Double configVersion;
    private boolean enabled;

    private Toml toml;

    public TwitchSettings(File dataFolder, Logger logger) {
        this.dataFolder = dataFolder;
        this.file = new File(this.dataFolder, "config.toml");

        saveDefaultConfig();
        Toml toml = loadConfig();

        this.enabled = toml.getBoolean("plugin.enabled");

        // Version
        this.configVersion = toml.getDouble("developer-info.config-version");

        if (!VersionUtils.isLatestConfigVersion(this)) {
            logger.warn("Your Config is out of date (Latest: " + VersionUtils.CONFIG_VERSION + ", Config Version: " + this.getConfigVersion() + ")!");
            logger.warn("Please backup your current config.toml, and delete the current one. A new config will then be created on the next proxy launch.");
            logger.warn("The plugin's functionality will not be enabled until the config is updated.");
            this.setEnabled(false);
            return;
        }

        // Load Toml
        this.toml = toml;
    }

    private void saveDefaultConfig() {
        if (!dataFolder.exists()) dataFolder.mkdir();
        if (file.exists()) return;

        try (InputStream in = TwitchSettings.class.getResourceAsStream("/config.toml")) {
            Files.copy(in, file.toPath());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private File getConfigFile() {
        return new File(dataFolder, "config.toml");
    }

    private Toml loadConfig() {
        return new Toml().read(getConfigFile());
    }

    public Double getConfigVersion() {
        return configVersion;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Toml getToml() {
        return toml;
    }

    public Component getParsedToml(String path) {
        return MiniMessage.get().parse(getToml().getString(path));
    }
}
