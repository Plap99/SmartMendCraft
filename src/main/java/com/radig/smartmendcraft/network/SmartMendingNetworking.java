package com.radig.smartmendcraft.network;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.radig.smartmendcraft.SmartMendCraft;
import com.radig.smartmendcraft.config.InventoryMendingMode;
import com.radig.smartmendcraft.config.MendingTarget;
import com.radig.smartmendcraft.config.PlayerMendingConfig;
import com.radig.smartmendcraft.config.PlayerMendingConfigs;
import com.radig.smartmendcraft.config.PlayerMendingConfigStorage;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public final class SmartMendingNetworking {

    public static final Identifier MENDING_UPDATE =
            SmartMendCraft.id("mending_update");

    public static final Identifier CONFIG_UPDATE =
            SmartMendCraft.id("config_update");

    public static final Identifier CONFIG_SYNC =
            SmartMendCraft.id("config_sync");

    /*
     * El cliente envía este paquete al entrar para indicar
     * que tiene SmartMendCraft instalado.
     */
    public static final Identifier CLIENT_HELLO =
            SmartMendCraft.id("client_hello");

    private SmartMendingNetworking() {
    }

    public static void sendMendingUpdate(
            ServerPlayerEntity player,
            ItemStack stack,
            int repairAmount) {

        if (player == null
                || stack == null
                || stack.isEmpty()
                || repairAmount <= 0
                || !SmartMendingClients.hasSmartMendCraft(player)) {

            return;
        }

        PacketByteBuf buffer =
                PacketByteBufs.create();

        /*
         * Enviamos una copia del objeto para que el cliente
         * pueda renderizar exactamente su icono.
         */
        buffer.writeItemStack(stack);

        /*
         * Cantidad reparada en este evento.
         */
        buffer.writeInt(repairAmount);

        /*
         * Daño actual después de la reparación.
         */
        buffer.writeInt(stack.getDamage());

        /*
         * Durabilidad máxima.
         */
        buffer.writeInt(stack.getMaxDamage());

        ServerPlayNetworking.send(
                player,
                MENDING_UPDATE,
                buffer
        );
    }

    public static void registerServerReceivers() {

        /*
         * -------------------------------------------------
         * HANDSHAKE DEL CLIENTE
         * -------------------------------------------------
         *
         * Si recibimos este paquete sabemos que ese jugador
         * tiene SmartMendCraft instalado en su cliente.
         */
        ServerPlayNetworking.registerGlobalReceiver(
                CLIENT_HELLO,
                (server, player, handler, buffer, responseSender) -> {

                    server.execute(() -> {

                        SmartMendingClients.register(
                                player
                        );

                        PlayerMendingConfig config =
                                PlayerMendingConfigs.get(
                                        player
                                );

                        sendConfigSync(
                                player,
                                config
                        );
                    });
                }
        );

        /*
         * -------------------------------------------------
         * ACTUALIZACIÓN DE CONFIGURACIÓN
         * -------------------------------------------------
         */
        ServerPlayNetworking.registerGlobalReceiver(
                CONFIG_UPDATE,
                (server, player, handler, buffer, responseSender) -> {

                    /*
                     * Un jugador sin SmartMendCraft registrado
                     * no debería estar enviando este paquete.
                     */
                    if (!SmartMendingClients
                            .hasSmartMendCraft(player)) {

                        return;
                    }

                    boolean repairMainHand =
                            buffer.readBoolean();

                    boolean repairOffHand =
                            buffer.readBoolean();

                    boolean repairArmor =
                            buffer.readBoolean();

                    boolean repairInventory =
                            buffer.readBoolean();

                    int priorityCount =
                            buffer.readInt();

                    /*
                     * Tenemos exactamente cuatro categorías.
                     * Cualquier otra cantidad se considera
                     * un paquete inválido.
                     */
                    if (priorityCount !=
                            MendingTarget.values().length) {

                        SmartMendCraft.LOGGER.warn(
                                "Configuración inválida recibida de {}: "
                                        + "priorityCount={}",
                                player.getName().getString(),
                                priorityCount
                        );

                        return;
                    }

                    List<MendingTarget> priority =
                            new ArrayList<>();

                    Set<MendingTarget> usedTargets =
                            new HashSet<>();

                    for (int i = 0;
                            i < priorityCount;
                            i++) {

                        int ordinal =
                                buffer.readInt();

                        /*
                         * Evita ArrayIndexOutOfBoundsException
                         * si un cliente modificado manda un
                         * ordinal inexistente.
                         */
                        if (ordinal < 0
                                || ordinal >=
                                MendingTarget.values().length) {

                            SmartMendCraft.LOGGER.warn(
                                    "Configuración inválida recibida de {}: "
                                            + "ordinal={}",
                                    player.getName().getString(),
                                    ordinal
                            );

                            return;
                        }

                        MendingTarget target =
                                MendingTarget.values()[
                                        ordinal
                                ];

                        /*
                         * Tampoco permitimos categorías
                         * repetidas en la prioridad.
                         */
                        if (!usedTargets.add(target)) {

                            SmartMendCraft.LOGGER.warn(
                                    "Configuración inválida recibida de {}: "
                                            + "categoría duplicada {}",
                                    player.getName().getString(),
                                    target
                            );

                            return;
                        }

                        priority.add(target);
                    }

                    int inventoryModeOrdinal =
                            buffer.readInt();

                    if (inventoryModeOrdinal < 0
                            || inventoryModeOrdinal >=
                            InventoryMendingMode.values().length) {

                        SmartMendCraft.LOGGER.warn(
                                "Configuración inválida recibida de {}: "
                                        + "inventoryMode={}",
                                player.getName().getString(),
                                inventoryModeOrdinal
                        );

                        return;
                    }

                    InventoryMendingMode inventoryMode =
                            InventoryMendingMode.values()[
                                    inventoryModeOrdinal
                            ];

                    /*
                     * La modificación real de los datos
                     * del jugador se hace en el hilo
                     * principal del servidor.
                     */
                    server.execute(() -> {

                        PlayerMendingConfig config =
                                PlayerMendingConfigs.get(
                                        player
                                );

                        config.setRepairMainHand(
                                repairMainHand
                        );

                        config.setRepairOffHand(
                                repairOffHand
                        );

                        config.setRepairArmor(
                                repairArmor
                        );

                        config.setRepairInventory(
                                repairInventory
                        );

                        config.setInventoryMode(
                                inventoryMode
                        );

                        config.setPriority(
                                priority
                        );

                        PlayerMendingConfigStorage.save(
                                player.getUuid(),
                                config
                        );
                    });
                }
        );
    }

    public static void sendConfigSync(
            ServerPlayerEntity player,
            PlayerMendingConfig config) {

        if (player == null
                || config == null
                || !SmartMendingClients
                .hasSmartMendCraft(player)) {

            return;
        }

        PacketByteBuf buffer =
                PacketByteBufs.create();

        buffer.writeBoolean(
                config.isRepairMainHand()
        );

        buffer.writeBoolean(
                config.isRepairOffHand()
        );

        buffer.writeBoolean(
                config.isRepairArmor()
        );

        buffer.writeBoolean(
                config.isRepairInventory()
        );

        List<MendingTarget> priority =
                config.getPriority();

        buffer.writeInt(
                priority.size()
        );

        for (MendingTarget target : priority) {

            buffer.writeInt(
                    target.ordinal()
            );
        }

        buffer.writeInt(
                config.getInventoryMode()
                        .ordinal()
        );

        ServerPlayNetworking.send(
                player,
                CONFIG_SYNC,
                buffer
        );
    }
}