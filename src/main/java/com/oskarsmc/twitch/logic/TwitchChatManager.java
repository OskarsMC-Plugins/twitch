package com.oskarsmc.twitch.logic;

import com.github.twitch4j.chat.TwitchChat;
import com.github.twitch4j.chat.TwitchChatBuilder;
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;
import com.oskarsmc.twitch.configuration.TwitchSettings;
import com.velocitypowered.api.command.CommandSource;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.Template;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TwitchChatManager {
    private TwitchChat twitchChat;
    private ConcurrentHashMap<String, List<CommandSource>> watchers;
    private TwitchSettings settings;

    public TwitchChatManager(TwitchSettings settings) {
        this.settings = settings;
        this.watchers = new ConcurrentHashMap<String, List<CommandSource>>();
        this.twitchChat = TwitchChatBuilder.builder().build();

        this.twitchChat.getEventManager().onEvent(ChannelMessageEvent.class, event -> {
            List<Template> templates = new ArrayList<Template>();

            templates.add(Template.of(
                    "author",
                    Component.text(event.getUser().getName())
                    .clickEvent(ClickEvent.openUrl("https://twitch.tv/" + event.getUser().getName()))
                    .hoverEvent(HoverEvent.hoverEvent(HoverEvent.Action.SHOW_TEXT, Component.text("https://twitch.tv/" + event.getUser().getName())))
            ));
            templates.add(Template.of("message", Component.text(event.getMessage())));

            Component message = MiniMessage.get().parse(settings.getToml().getString("messages.chat-message"), templates);

            for (CommandSource source : watchers.get(event.getChannel().getName())) {
                source.sendMessage(message);
            }

        });
    }

    public void addWatcher(CommandSource watcher, String streamer) {
        this.removeWatcher(watcher); // remove watcher - implement multiple streams?
        MiniMessage miniMessage = MiniMessage.get();
        if (twitchChat.isChannelJoined(streamer)) {
            watchers.get(streamer).add(watcher);
        } else {
            twitchChat.joinChannel(streamer);
            watchers.put(streamer, new ArrayList<CommandSource>(List.of(watcher)));
        }

        watcher.sendMessage(miniMessage.parse(settings.getToml().getString("messages.channel-set"), Map.of("streamer", streamer)));
    }

    public void removeWatcher(CommandSource watcher) {
        MiniMessage miniMessage = MiniMessage.get();
        for (Map.Entry<String, List<CommandSource>> entry : watchers.entrySet()) {
            for (CommandSource source : entry.getValue()) {
                if (source == watcher) {
                    watchers.get(entry.getKey()).remove(watcher);
                    watcher.sendMessage(miniMessage.parse(settings.getToml().getString("messages.channel-off")));
                    return;
                }
            }
        }

        watcher.sendMessage(miniMessage.parse(settings.getToml().getString("messages.channel-off-not-found")));
    }

    public void close() {
        this.twitchChat.close();
    }
}
