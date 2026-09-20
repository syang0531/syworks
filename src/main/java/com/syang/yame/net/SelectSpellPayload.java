package com.syang.yame.net;

import com.syang.yame.Yame;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/**
 * Client → server: cycle the held staff's active spell by {@code direction} (+1 next, −1 previous).
 * Sent by the Shift+scroll handler; applied server-side against the {@code active_spell} component.
 */
public record SelectSpellPayload(int direction) implements CustomPacketPayload {

    public static final Type<SelectSpellPayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(Yame.MOD_ID, "select_spell"));

    public static final StreamCodec<ByteBuf, SelectSpellPayload> STREAM_CODEC =
            ByteBufCodecs.INT.map(SelectSpellPayload::new, SelectSpellPayload::direction);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
