package tfar.darkness.network;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import tfar.darkness.Darkness;

import java.util.function.Supplier;

public class MessageSyncConfig {

    public boolean blockLightOnly;
    public boolean ignoreMoonPhase;
    public double minimumMoonLevel;
    public double maximumMoonLevel;
    public boolean darkOverworld;
    public boolean darkDefault;
    public boolean darkNether;
    public double darkNetherFogEffective;
    public boolean darkEnd;
    public double darkEndFogEffective;
    public boolean darkSkyless;
    public double minimumLight;

    public MessageSyncConfig(boolean blockLightOnly, boolean ignoreMoonPhase, double minimumMoonLevel, double maximumMoonLevel,
                             boolean darkOverworld, boolean darkDefault, boolean darkNether, double darkNetherFogEffective,
                             boolean darkEnd, double darkEndFogEffective, boolean darkSkyless, double minimumLight) {
        this.blockLightOnly = blockLightOnly;
        this.ignoreMoonPhase = ignoreMoonPhase;
        this.minimumMoonLevel = minimumMoonLevel;
        this.maximumMoonLevel = maximumMoonLevel;
        this.darkOverworld = darkOverworld;
        this.darkDefault = darkDefault;
        this.darkNether = darkNether;
        this.darkNetherFogEffective = darkNetherFogEffective;
        this.darkEnd = darkEnd;
        this.darkEndFogEffective = darkEndFogEffective;
        this.darkSkyless = darkSkyless;
        this.minimumLight = minimumLight;
    }

    public static MessageSyncConfig decode(PacketBuffer buffer) {
        MessageSyncConfig message = null;
        try {
            message = new MessageSyncConfig(buffer.readBoolean(), buffer.readBoolean(),
                    buffer.readDouble(), buffer.readDouble(), buffer.readBoolean(),
                    buffer.readBoolean(), buffer.readBoolean(), buffer.readDouble(),
                    buffer.readBoolean(), buffer.readDouble(), buffer.readBoolean(),
                    buffer.readDouble());
        } catch (IllegalArgumentException | IndexOutOfBoundsException e) {
            Darkness.LOG.warn("Exception while reading MessageConfigSync: " + e);
        }
        return message;
    }

    public void encode(PacketBuffer buffer) {
        buffer.writeBoolean(Darkness.Config.blockLightOnly.get());
        buffer.writeBoolean(Darkness.Config.ignoreMoonPhase.get());
        buffer.writeDouble(Darkness.Config.minimumMoonLevel.get());
        buffer.writeDouble(Darkness.Config.maximumMoonLevel.get());
        buffer.writeBoolean(Darkness.Config.darkOverworld.get());
        buffer.writeBoolean(Darkness.Config.darkDefault.get());
        buffer.writeBoolean(Darkness.Config.darkNether.get());
        buffer.writeDouble(Darkness.Config.darkNetherFogConfigured.get());
        buffer.writeBoolean(Darkness.Config.darkEnd.get());
        buffer.writeDouble(Darkness.Config.darkEndFogConfigured.get());
        buffer.writeBoolean(Darkness.Config.darkSkyless.get());
        buffer.writeDouble(Darkness.Config.minimumLight.get());
    }

    public static void clientHandle(MessageSyncConfig msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            Darkness.Config.onServer = true;
            Darkness.Config.client_blockLightOnly = msg.blockLightOnly;
            Darkness.Config.client_ignoreMoonPhase = msg.ignoreMoonPhase;
            Darkness.Config.client_minimumMoonLevel = msg.minimumMoonLevel;
            Darkness.Config.client_maximumMoonLevel = msg.maximumMoonLevel;
            Darkness.Config.client_darkOverworld = msg.darkOverworld;
            Darkness.Config.client_darkDefault = msg.darkDefault;
            Darkness.Config.client_darkNether = msg.darkNether;
            Darkness.Config.client_darkNetherFogEffective = msg.darkNetherFogEffective;
            Darkness.Config.client_darkEnd = msg.darkEnd;
            Darkness.Config.client_darkEndFogEffective = msg.darkNetherFogEffective;
            Darkness.Config.client_darkSkyless = msg.darkSkyless;
            Darkness.Config.client_minimumLight = msg.minimumLight;

            Darkness.LOG.info("Settings synced with server config.");
        });
        ctx.get().setPacketHandled(true);
    }
}
