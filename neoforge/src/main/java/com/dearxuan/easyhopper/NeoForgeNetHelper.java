package com.dearxuan.easyhopper;

import com.dearxuan.easyhopper.config.ModConfig;
import com.dearxuan.easyhopper.mixin.ServerPlayerMixin;
import com.dearxuan.easyhopper.net.ConfigSyncPayload;
import com.dearxuan.easyhopper.net.NetManager;
import com.dearxuan.easyhopper.utils.PlayerUtil;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/**
 * NeoForge 网络辅助类, 负责 ConfigSyncPayload 的注册与收发
 */
public class NeoForgeNetHelper {

    /**
     * 注册 Payload 类型和处理器 (在 Mod 事件总线上)
     */
    public static void register(IEventBus modEventBus) {
        modEventBus.addListener(RegisterPayloadHandlersEvent.class, event -> {
            final PayloadRegistrar registrar = event.registrar("1");

            // C2S: 客户端推送配置到服务端
            registrar.playToServer(
                    ConfigSyncPayload.TYPE,
                    ConfigSyncPayload.CODEC,
                    NeoForgeNetHelper::handleServerConfigPush
            );

            // S2C: 服务端同步配置到客户端
            registrar.playToClient(
                    ConfigSyncPayload.TYPE,
                    ConfigSyncPayload.CODEC,
                    NeoForgeNetHelper::handleClientConfigSync
            );
        });
    }

    /**
     * 注册玩家进服事件 (在 NeoForge 事件总线上)
     */
    public static void registerPlayerJoinEvent(IEventBus neoForgeEventBus) {
        neoForgeEventBus.addListener(PlayerEvent.PlayerLoggedInEvent.class, event -> {
            if (event.getEntity() instanceof ServerPlayer player) {
                ConfigSyncPayload payload = new ConfigSyncPayload(ModConfig.INSTANCE);
                PacketDistributor.sendToPlayer(player, payload);
            }
        });
    }

    private static void handleServerConfigPush(ConfigSyncPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) context.player();
            if (!PlayerUtil.hasOpPermission(player)) {
                Constants.LOG.warn(
                        "Player {} attempted to modify config without permission",
                        player.getName().getString()
                );
                return;
            }
            ModConfig config = payload.toConfig();
            ModConfig.INSTANCE = config;

            // 打印日志: 记录配置修改者
            Constants.LOG.info(
                    "Config modified by player {} ({}). New values: transferCooldown={}, inputCount={}, outputCount={}, filtering={}, extractCooldown={}, minecartCooldown={}, allowOpModify={}",
                    player.getName().getString(),
                    player.getUUID(),
                    config.HOPPER_TRANSFER_COOLDOWN,
                    config.HOPPER_INPUT_COUNT,
                    config.HOPPER_OUTPUT_COUNT,
                    config.HOPPER_FILTERING,
                    config.HOPPER_EXTRACT_COOLDOWN,
                    config.HOPPER_MINECART_TRANSFER_COOLDOWN,
                    config.ALLOW_OP_MODIFY
            );

            MinecraftServer server = ((ServerPlayerMixin) player).getServer();
            for (ServerPlayer otherPlayer : server.getPlayerList().getPlayers()) {
                if (otherPlayer != player) {
                    PacketDistributor.sendToPlayer(otherPlayer, payload);
                }
            }
        });
    }

    private static void handleClientConfigSync(ConfigSyncPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            NetManager.updateServerConfig(payload.toConfig());
        });
    }
}