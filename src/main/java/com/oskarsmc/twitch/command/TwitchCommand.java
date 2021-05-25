package com.oskarsmc.twitch.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.oskarsmc.twitch.Twitch;
import com.oskarsmc.twitch.configuration.TwitchSettings;
import com.oskarsmc.twitch.permission.PermissionChecker;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.ProxyServer;

public class TwitchCommand {
    public TwitchCommand(Twitch plugin, ProxyServer proxyServer, TwitchSettings settings) {
        LiteralCommandNode<CommandSource> sendCommand = LiteralArgumentBuilder
                .<CommandSource>literal("twitch")
                .executes(context -> {
                    if (PermissionChecker.checkPerm(context.getSource(), new PermissionChecker.PermissionContext(PermissionChecker.PermissionContext.TWITCH_CHAT_USE))) {
                        context.getSource().sendMessage(settings.getParsedToml("messages.usage"));
                        return 1;
                    }
                    context.getSource().sendMessage(settings.getParsedToml("messages.no-permission"));
                    return 0;
                })
                .build();

        ArgumentCommandNode<CommandSource, String> actionNode = RequiredArgumentBuilder
                .<CommandSource, String>argument("action", StringArgumentType.word())
                .suggests((context, builder) -> {
                    if (PermissionChecker.checkPerm(context.getSource(), new PermissionChecker.PermissionContext(PermissionChecker.PermissionContext.TWITCH_CHAT_USE))) {
                        builder.suggest("off");
                        builder.suggest("set");
                    }
                    return builder.buildFuture();
                })
                .executes(context -> {
                    if (PermissionChecker.checkPerm(context.getSource(), new PermissionChecker.PermissionContext(PermissionChecker.PermissionContext.TWITCH_CHAT_USE))) {
                        if (context.getArgument("action", String.class).equalsIgnoreCase("off")) {
                            plugin.twitchChatManager.removeWatcher(context.getSource());
                        } else {
                            context.getSource().sendMessage(settings.getParsedToml("messages.usage"));
                        }
                    } else {
                        context.getSource().sendMessage(settings.getParsedToml("messages.no-permission"));
                    }
                    return 0;
                }).build();

        ArgumentCommandNode<CommandSource, String> streamerNode = RequiredArgumentBuilder
                .<CommandSource, String>argument("streamer", StringArgumentType.greedyString())
                .executes(context -> {
                    if (PermissionChecker.checkPerm(context.getSource(), new PermissionChecker.PermissionContext(PermissionChecker.PermissionContext.TWITCH_CHAT_USE))) {
                        if (context.getArgument("action", String.class).equalsIgnoreCase("set")) {
                            plugin.twitchChatManager.addWatcher(context.getSource(), context.getArgument("streamer", String.class));
                        } else {
                            context.getSource().sendMessage(settings.getParsedToml("messages.usage"));
                        }
                    } else {
                        context.getSource().sendMessage(settings.getParsedToml("messages.no-permission"));
                    }
                    return 0;
                }).build();

        actionNode.addChild(streamerNode);
        sendCommand.addChild(actionNode);
        BrigadierCommand sendBrigadier = new BrigadierCommand(sendCommand);

        CommandMeta meta = proxyServer.getCommandManager().metaBuilder(sendBrigadier)
                .build();

        proxyServer.getCommandManager().register(meta, sendBrigadier);
    }
}
