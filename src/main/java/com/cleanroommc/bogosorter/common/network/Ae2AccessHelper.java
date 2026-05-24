package com.cleanroommc.bogosorter.common.network;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;

import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.security.ISecurityGrid;
import appeng.core.worlddata.IWorldPlayerData;
import appeng.core.worlddata.WorldData;
import cpw.mods.fml.common.Loader;

final class Ae2AccessHelper {

    private static final int UNKNOWN_PLAYER_ID = Integer.MIN_VALUE;
    private static final long TEAM_ACCESS_CACHE_MS = 5000L;
    private static final int TEAM_ACCESS_CACHE_MAX_ENTRIES = 1024;
    private static final Map<String, TeamAccessEntry> TEAM_ACCESS_CACHE = new HashMap<>();

    private static Field playerMappingField;
    private static Field mappingsField;
    private static boolean playerMappingReflectionFailed;

    private Ae2AccessHelper() {}

    static boolean canPlayerUseGrid(EntityPlayerMP player, IGrid grid, IGridNode wirelessNode) {
        if (player == null || grid == null) {
            return false;
        }

        int playerId = getAe2PlayerId(player);
        int ownerId = getNetworkOwnerId(grid);
        if (ownerId == UNKNOWN_PLAYER_ID && wirelessNode != null) {
            ownerId = wirelessNode.getPlayerID();
        }

        return canPlayerAccessOwner(player, playerId, ownerId);
    }

    private static boolean canPlayerAccessOwner(EntityPlayerMP player, int playerId, int ownerId) {
        if (playerId < 0 || ownerId < 0 || playerId == UNKNOWN_PLAYER_ID || ownerId == UNKNOWN_PLAYER_ID) {
            return false;
        }

        if (playerId == ownerId) {
            return true;
        }

        UUID playerUuid = player.getGameProfile()
            .getId();
        UUID ownerUuid = getAe2PlayerUuid(ownerId);
        if (playerUuid == null || ownerUuid == null) {
            return false;
        }

        if (playerUuid.equals(ownerUuid)) {
            return true;
        }

        return hasSharedTeamAccess(player, playerUuid, ownerUuid);
    }

    private static int getAe2PlayerId(EntityPlayerMP player) {
        try {
            return WorldData.instance()
                .playerData()
                .getPlayerID(player.getGameProfile());
        } catch (Throwable ignored) {
            return UNKNOWN_PLAYER_ID;
        }
    }

    private static int getNetworkOwnerId(IGrid grid) {
        try {
            if (grid == null) {
                return UNKNOWN_PLAYER_ID;
            }

            ISecurityGrid securityGrid = grid.getCache(ISecurityGrid.class);
            if (securityGrid == null || !securityGrid.isAvailable()) {
                return UNKNOWN_PLAYER_ID;
            }

            int ownerId = securityGrid.getOwner();
            return ownerId < 0 ? UNKNOWN_PLAYER_ID : ownerId;
        } catch (Throwable ignored) {
            return UNKNOWN_PLAYER_ID;
        }
    }

    private static UUID getAe2PlayerUuid(int playerId) {
        try {
            IWorldPlayerData playerData = WorldData.instance()
                .playerData();
            EntityPlayer onlinePlayer = playerData.getPlayerFromID(playerId);
            if (onlinePlayer != null && onlinePlayer.getGameProfile() != null) {
                return onlinePlayer.getGameProfile()
                    .getId();
            }

            return getAe2PlayerUuidFromMapping(playerData, playerId);
        } catch (Throwable ignored) {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private static UUID getAe2PlayerUuidFromMapping(IWorldPlayerData playerData, int playerId) {
        if (playerMappingReflectionFailed || playerData == null) {
            return null;
        }

        try {
            if (playerMappingField == null) {
                playerMappingField = playerData.getClass()
                    .getDeclaredField("playerMapping");
                playerMappingField.setAccessible(true);
            }

            Object playerMapping = playerMappingField.get(playerData);
            if (playerMapping == null) {
                return null;
            }

            if (mappingsField == null) {
                mappingsField = playerMapping.getClass()
                    .getDeclaredField("mappings");
                mappingsField.setAccessible(true);
            }

            Object mappings = mappingsField.get(playerMapping);
            if (mappings instanceof Map) {
                Object uuid = ((Map<Integer, UUID>) mappings).get(playerId);
                return uuid instanceof UUID ? (UUID) uuid : null;
            }
        } catch (Throwable ignored) {
            playerMappingReflectionFailed = true;
        }

        return null;
    }

    private static boolean hasSharedTeamAccess(EntityPlayerMP player, UUID playerUuid, UUID ownerUuid) {
        long now = System.currentTimeMillis();
        String cacheKey = playerUuid.toString() + '|' + ownerUuid.toString();
        TeamAccessEntry cached = TEAM_ACCESS_CACHE.get(cacheKey);
        if (cached != null && now < cached.expiresAt) {
            return cached.allowed;
        }

        cleanupTeamAccessCache(now);

        boolean allowed = isSameServerUtilitiesTeam(playerUuid, ownerUuid)
            || isSameBetterQuestingParty(player, playerUuid, ownerUuid);
        TEAM_ACCESS_CACHE.put(cacheKey, new TeamAccessEntry(allowed, now + TEAM_ACCESS_CACHE_MS));
        return allowed;
    }

    private static void cleanupTeamAccessCache(long now) {
        if (TEAM_ACCESS_CACHE.size() < TEAM_ACCESS_CACHE_MAX_ENTRIES) {
            return;
        }

        java.util.Iterator<Map.Entry<String, TeamAccessEntry>> iterator = TEAM_ACCESS_CACHE.entrySet()
            .iterator();
        while (iterator.hasNext()) {
            if (now >= iterator.next()
                .getValue().expiresAt) {
                iterator.remove();
            }
        }
    }

    private static boolean isSameServerUtilitiesTeam(UUID playerUuid, UUID ownerUuid) {
        if (!Loader.isModLoaded("serverutilities")) {
            return false;
        }

        try {
            Class<?> universeClass = Class.forName("serverutils.lib.data.Universe");
            Object universe = universeClass.getMethod("getNullable")
                .invoke(null);
            if (universe == null) {
                return false;
            }

            Method getPlayer = universeClass.getMethod("getPlayer", UUID.class);
            Object player = getPlayer.invoke(universe, playerUuid);
            Object owner = getPlayer.invoke(universe, ownerUuid);
            if (player == null || owner == null) {
                return false;
            }

            Object playerTeam = player.getClass()
                .getField("team")
                .get(player);
            Object ownerTeam = owner.getClass()
                .getField("team")
                .get(owner);
            if (playerTeam == null || ownerTeam == null) {
                return false;
            }

            if (!Boolean.TRUE.equals(
                playerTeam.getClass()
                    .getMethod("isValid")
                    .invoke(playerTeam))) {
                return false;
            }

            return Boolean.TRUE.equals(
                playerTeam.getClass()
                    .getMethod("equalsTeam", playerTeam.getClass())
                    .invoke(playerTeam, ownerTeam));
        } catch (Throwable ignored) {
            return false;
        }
    }

    private static boolean isSameBetterQuestingParty(EntityPlayerMP player, UUID playerUuid, UUID ownerUuid) {
        if (!Loader.isModLoaded("betterquesting")) {
            return false;
        }

        try {
            UUID playerQuestUuid = getBetterQuestingUuid(player);
            if (playerQuestUuid == null) {
                playerQuestUuid = playerUuid;
            }

            Integer playerParty = getBetterQuestingPartyId(playerQuestUuid);
            if (playerParty == null && !playerUuid.equals(playerQuestUuid)) {
                playerParty = getBetterQuestingPartyId(playerUuid);
            }

            Integer ownerParty = getBetterQuestingPartyId(ownerUuid);
            if (playerParty == null || ownerParty == null) {
                return false;
            }

            return playerParty.equals(ownerParty);
        } catch (Throwable ignored) {
            return false;
        }
    }

    private static UUID getBetterQuestingUuid(EntityPlayer player) {
        if (player == null) {
            return null;
        }

        try {
            Class<?> questingApi = Class.forName("betterquesting.api.api.QuestingAPI");
            Object uuid = questingApi.getMethod("getQuestingUUID", EntityPlayer.class)
                .invoke(null, player);
            return uuid instanceof UUID ? (UUID) uuid : null;
        } catch (Throwable ignored) {
            return null;
        }
    }

    private static Integer getBetterQuestingPartyId(UUID uuid) {
        if (uuid == null) {
            return null;
        }

        try {
            Class<?> partyManagerClass = Class.forName("betterquesting.questing.party.PartyManager");
            Object partyManager = partyManagerClass.getField("INSTANCE")
                .get(null);
            Object entry = partyManagerClass.getMethod("getParty", UUID.class)
                .invoke(partyManager, uuid);
            if (entry == null) {
                return null;
            }

            Object id = entry.getClass()
                .getMethod("getID")
                .invoke(entry);
            return id instanceof Integer ? (Integer) id : null;
        } catch (Throwable ignored) {
            return null;
        }
    }

    private static final class TeamAccessEntry {

        private final boolean allowed;
        private final long expiresAt;

        private TeamAccessEntry(boolean allowed, long expiresAt) {
            this.allowed = allowed;
            this.expiresAt = expiresAt;
        }
    }
}
