package com.radig.smartmendcraft.network.payload;

import java.util.ArrayList;
import java.util.List;

import com.radig.smartmendcraft.SmartMendCraft;
import com.radig.smartmendcraft.hotbar.HotbarPreferences;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record HotbarPreferencesPayload(boolean enabled, List<String> rules)
        implements CustomPacketPayload {

    public static final Type<HotbarPreferencesPayload> TYPE =
            new Type<>(SmartMendCraft.id("hotbar_preferences"));

    public static final StreamCodec<RegistryFriendlyByteBuf, HotbarPreferencesPayload> CODEC =
            StreamCodec.of((buffer, payload) -> {
                buffer.writeBoolean(payload.enabled());
                for (String rule : payload.rules()) {
                    buffer.writeUtf(rule, HotbarPreferences.MAX_RULE_LENGTH);
                }
            }, buffer -> {
                boolean enabled = buffer.readBoolean();
                List<String> rules = new ArrayList<>(HotbarPreferences.SLOT_COUNT);
                for (int slot = 0; slot < HotbarPreferences.SLOT_COUNT; slot++) {
                    rules.add(buffer.readUtf(HotbarPreferences.MAX_RULE_LENGTH));
                }
                return new HotbarPreferencesPayload(enabled, List.copyOf(rules));
            });

    public HotbarPreferencesPayload(HotbarPreferences preferences) {
        this(preferences.enabled(), preferences.rules());
    }

    @Override
    public Type<HotbarPreferencesPayload> type() {
        return TYPE;
    }
}
