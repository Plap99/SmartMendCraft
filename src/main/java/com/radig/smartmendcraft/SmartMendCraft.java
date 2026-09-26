package com.radig.smartmendcraft;

import net.fabricmc.api.ModInitializer;
import net.minecraft.util.Identifier;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.radig.smartmendcraft.network.SmartMendingNetworking;
import com.radig.smartmendcraft.network.SmartMendingClients;
import com.radig.smartmendcraft.repair.MendingRepairEvents;
import com.radig.smartmendcraft.repair.SmartMendingManager;

import net.minecraft.server.network.ServerPlayerEntity;

import com.radig.smartmendcraft.config.PlayerMendingConfig;
import com.radig.smartmendcraft.config.PlayerMendingConfigs;
import com.radig.smartmendcraft.config.PlayerMendingConfigStorage;

import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;

public class SmartMendCraft implements ModInitializer {

    public static final String MOD_ID = "smartmendcraft";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    @Override
    public void onInitialize() {

        SmartMendingNetworking.registerServerReceivers();

        /*
         * Cargamos la configuración del jugador al entrar.
         *
         * No enviamos CONFIG_SYNC aquí.
         * La sincronización ocurre después de que el cliente
         * confirme que tiene SmartMendCraft mediante CLIENT_HELLO.
         */
        ServerPlayConnectionEvents.JOIN.register(
                (handler, sender, server) -> {

                    PlayerMendingConfig config =
                            PlayerMendingConfigStorage.load(
                                    handler.player.getUuid()
                            );

                    PlayerMendingConfigs.set(
                            handler.player.getUuid(),
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

                    /*
                     * Limpiamos el objetivo de FINISH_ITEM
                     * asociado al jugador.
                     */
                    SmartMendingManager.clearPlayer(
                            handler.player
                    );

                    /*
                     * Eliminamos también el registro que indica
                     * que este jugador tenía SmartMendCraft
                     * instalado en el cliente.
                     *
                     * Así, si vuelve a entrar sin el mod,
                     * utilizará Mending vanilla.
                     */
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

                    if (player instanceof ServerPlayerEntity) {

                        SmartMendingNetworking.sendMendingUpdate(
                                (ServerPlayerEntity) player,
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
        return Identifier.tryParse(
                MOD_ID + ":" + path
        );
    }
}