package io.github.ladysnake.impersonate.impl;

import io.github.ladysnake.impersonate.Impersonate;
import io.github.ladysnake.impersonate.Impersonator;
import io.github.ladysnake.impersonate.impl.mixin.GameMessageS2CPacketAccessor;
import io.github.ladysnake.impersonate.impl.mixin.PlayerListS2CPacketAccessor;
import io.github.ladysnake.impersonate.impl.mixin.PlayerListS2CPacketEntryAccessor;
import io.netty.buffer.Unpooled;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public final class PacketMeddling {
    @NotNull
    public static Packet<?> resolveFakeTexts(GameMessageS2CPacket packet, ServerPlayerEntity player) {
        GameMessageS2CPacketAccessor accessiblePacket = (GameMessageS2CPacketAccessor) packet;
        accessiblePacket.setMessage(((RecipientAwareText) accessiblePacket.getMessage()).impersonateResolveAll(player));
        return packet;
    }

    public static void resolvePlayerListEntries(PlayerListS2CPacket packet, ServerPlayerEntity player) {
        boolean reveal = ImpersonateText.shouldBeRevealedBy(player);
        for (PlayerListS2CPacket.Entry entry : ((PlayerListS2CPacketAccessor) packet).getEntries()) {
            PlayerEntity playerEntry = player.server.getPlayerManager().getPlayer(entry.getProfile().getId());
            if (playerEntry != null) {
                Impersonator impersonator = Impersonate.IMPERSONATION.get(playerEntry);
                if (impersonator.isImpersonating()) {
                    // OPs get the true profile with semi-fake display name, others get a complete lie
                    if (reveal) {
                        ((PlayerListS2CPacketEntryAccessor) entry).setDisplayName(ImpersonateText.get(playerEntry, true));
                    } else {
                        ((PlayerListS2CPacketEntryAccessor) entry).setProfile(impersonator.getEditedProfile());
                    }
                }
            }
        }
    }

    public static <P extends Packet<?>> boolean copyPacket(P packet, P copy) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        try {
            packet.write(buf);
            copy.read(buf);
            return true;
        } catch (IOException e) {
            Impersonate.LOGGER.warn("Failed to copy packet " + packet, e);
            return false;
        } finally {
            buf.release();
        }
    }
}
