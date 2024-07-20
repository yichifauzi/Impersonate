package org.ladysnake.impersonate.impl;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import org.ladysnake.impersonate.Impersonate;

public final class ReloadSkinPacket implements CustomPayload {
    public static final CustomPayload.Id<ReloadSkinPacket> ID = new CustomPayload.Id<>(Impersonate.id("impersonation"));
    public static final ReloadSkinPacket INSTANCE = new ReloadSkinPacket();
    public static final PacketCodec<ByteBuf, ReloadSkinPacket> CODEC = PacketCodec.unit(INSTANCE);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    private ReloadSkinPacket() {}
}
