package com.radig.smartmendcraft;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.radig.smartmendcraft.config.PlayerMendingConfig;
import com.radig.smartmendcraft.config.PlayerMendingConfigStorage;
import com.radig.smartmendcraft.config.PlayerMendingConfigs;
import com.radig.smartmendcraft.network.SmartMendingClients;
import com.radig.smartmendcraft.network.SmartMendingNetworking;
import com.radig.smartmendcraft.repair.MendingRepairEvents;
import com.radig.smartmendcraft.repair.SmartMendingManager;

import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;

@Mod(SmartMendCraft.MOD_ID)
public final class SmartMendCraft {

    public static final String MOD_ID = "smartmendcraft";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    public SmartMendCraft(FMLJavaModLoadingContext context) {
        SmartMendingNetworking.register();

        PlayerEvent.PlayerLoggedInEvent.BUS.addListener(event -> {
            if (event.getEntity() instanceof ServerPlayer player) {
                PlayerMendingConfig config = PlayerMendingConfigStorage.load(player.getUUID());
                PlayerMendingConfigs.set(player.getUUID(), config);
            }
        });

        PlayerEvent.PlayerLoggedOutEvent.BUS.addListener(event -> {
            if (event.getEntity() instanceof ServerPlayer player) {
                SmartMendingManager.clearPlayer(player);
                SmartMendingClients.unregister(player);
            }
        });

        MendingRepairEvents.register((player, stack, repairAmount) -> {
            if (player instanceof ServerPlayer serverPlayer) {
                SmartMendingNetworking.sendMendingUpdate(serverPlayer, stack, repairAmount);
            }
        });

        if (FMLEnvironment.dist == Dist.CLIENT) {
            SmartMendCraftClientBootstrap.initialize();
        }

        LOGGER.info("SmartMendCraft Forge iniciado correctamente.");
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }
}
