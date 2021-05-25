package com.oskarsmc.twitch.permission;

import com.velocitypowered.api.command.CommandSource;

public class PermissionChecker {
    public static class PermissionContext {
        public static final String TWITCH_CHAT_USE = "osmc.twitch.use";

        private final String permission;

        public PermissionContext(String permission) {
            this.permission = permission;
        }

        public String permission() {
            return permission;
        }
    }

    public static boolean checkPerm(CommandSource commandSource, PermissionContext permissionContext) {
        return commandSource.hasPermission(permissionContext.permission);
    }
}
