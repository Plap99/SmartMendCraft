package com.radig.smartmendcraft;

import net.fabricmc.api.ModInitializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.radig.smartmendcraft.network.SmartMendingNetworking;
import com.radig.smartmendcraft.network.SmartMendingClients;
import com.radig.smartmendcraft.repair.MendingRepairEvents;
import com.radig.smartmendcraft.repair.SmartMendingManager;
import com.radig.smartmendcraft.config.PlayerMendingConfig;
import com.radig.smartmendcraft.config.PlayerMendingConfigs;
import com.radig.smartmendcraft.config.PlayerMendingConfigStorage;

import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

public class SmartMendCraft implements ModInitializer {

    public static final String MOD_ID = "smartmendcraft";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    @Override
    public void onInitialize() {

        SmartMendingNetworking.registerPayloads();
        SmartMendingNetworking.registerServerReceivers();

        /*
         * Cargamos la configuracion del jugador al entrar.
         *
         * No enviamos CONFIG_SYNC aqui.
         * La sincronizacion ocurre despues de que el cliente
         * confirme que tiene SmartMendCraft mediante CLIENT_HELLO.
         */
        ServerPlayConnectionEvents.JOIN.register(
                (handler, sender, server) -> {

                    PlayerMendingConfig config =
                            PlayerMendingConfigStorage.load(
                                    handler.player.getUUID()
                            );

                    PlayerMendingConfigs.set(
                            handler.player.getUUID(),
                            config
                    );
                }
        );

        /*
         * Limpieza de datos temporales cuando
         * un jugador abandona el servidor.
         */
        ServerPlayConnectionEvents.DISCONNECT.register(
                (handler, server) -> {

                    SmartMendingManager.clearPlayer(
                            handler.player
                    );

                    SmartMendingClients.unregister(
                            handler.player
                    );
                }
        );

        /*
         * Evento que informa al cliente cuando
         * SmartMendCraft repara un objeto.
         */
        MendingRepairEvents.register(
                (player, stack, repairAmount) -> {

                    if (player instanceof ServerPlayer) {

                        SmartMendingNetworking.sendMendingUpdate(
                                (ServerPlayer) player,
                                stack,
                                repairAmount
                        );
                    }
                }
        );

        LOGGER.info(
                "SmartMendCraft iniciado correctamente."
        );
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(
                MOD_ID,
                path
        );
    }
}