package tfar.darkness.network;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;
import tfar.darkness.Darkness;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static net.minecraftforge.fml.network.NetworkDirection.PLAY_TO_CLIENT;

public class PacketHandler {

    private static final String PROTOCOL_VERSION = "1.0";
    private static final List<String> PROTOCOLS = Arrays.asList(new String[]{PROTOCOL_VERSION, NetworkRegistry.ABSENT, NetworkRegistry.ACCEPTVANILLA});

    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(new ResourceLocation(
            Darkness.MODID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOLS::contains,
            PROTOCOL_VERSION::equals
    );

    public static void setup() {
        Darkness.LOG.info("Registering network messages");

        INSTANCE.registerMessage(0, MessageSyncConfig.class, MessageSyncConfig::encode, MessageSyncConfig::decode, MessageSyncConfig::clientHandle, Optional.of(PLAY_TO_CLIENT));
    }
}
