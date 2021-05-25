package com.oskarsmc.twitch;

import com.google.inject.Inject;
import com.oskarsmc.twitch.command.TwitchCommand;
import com.oskarsmc.twitch.configuration.TwitchSettings;
import com.oskarsmc.twitch.logic.TwitchChatManager;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import org.slf4j.Logger;

import java.nio.file.Path;

public class Twitch {

    @Inject
    private Logger logger;

    @Inject
    private ProxyServer proxyServer;

    @Inject
    private @DataDirectory
    Path dataDirectory;

    private TwitchSettings twitchSettings;
    public TwitchChatManager twitchChatManager;
    private TwitchCommand twitchCommand;

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        this.twitchSettings = new TwitchSettings(dataDirectory.toFile(), this.logger);
        if (this.twitchSettings.isEnabled()) {
            this.twitchChatManager = new TwitchChatManager(this.twitchSettings);
            this.twitchCommand = new TwitchCommand(this, this.proxyServer, this.twitchSettings);
        }
    }
    
    @Subscribe
    public void playerQuit(DisconnectEvent event) {
        if (event.getLoginStatus().equals(DisconnectEvent.LoginStatus.SUCCESSFUL_LOGIN)) {
            this.twitchChatManager.removeWatcher(((CommandSource) event.getPlayer()));
        }
    }

    @Subscribe
    public void shutdown(ProxyShutdownEvent event) {
        this.twitchChatManager.close();
    }
}
