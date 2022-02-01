/*******************************************************************************
 * Copyright 2019 grondag
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package tfar.darkness;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import tfar.darkness.network.MessageSyncConfig;
import tfar.darkness.network.PacketHandler;

@Mod(Darkness.MODID)
public class Darkness {
	public static Logger LOG = LogManager.getLogger("Darkness");

	public static final String MODID = "totaldarkness";

	public Darkness() {
		ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, COMMON_SPEC);
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onConfigChange);

		if (FMLEnvironment.dist.isClient()) {
			FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onConfigChange);
			MinecraftForge.EVENT_BUS.addListener(this::onDisconnect);
		} else {
			FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onConfigReload);
			MinecraftForge.EVENT_BUS.addListener(this::onPlayerLogin);
		}

		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
	}

	public void setup(final FMLCommonSetupEvent event) {
		PacketHandler.setup();
	}

	private void onConfigChange(ModConfig.ModConfigEvent e) {
		if (!Config.onServer) {
			updateConfigSettings();
		}
	}

	private void onDisconnect(ClientPlayerNetworkEvent.LoggedOutEvent e) {
		Darkness.LOG.info("Disconnecting from server, reverting back to client settings.");
		Config.onServer = false;
		updateConfigSettings();
	}

	private void onConfigReload(ModConfig.Reloading e) {
		if(!FMLEnvironment.dist.isClient()) {
			for(ServerPlayerEntity player : ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayers()) {
				sendConfigToClient(player);
			}
		}
	}

	public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
		Darkness.LOG.info("Server config settings being sent to player.");

		sendConfigToClient((ServerPlayerEntity) event.getPlayer());
	}

	public void sendConfigToClient(ServerPlayerEntity player) {
		PacketHandler.INSTANCE.sendTo(new MessageSyncConfig(Darkness.Config.blockLightOnly.get(), Darkness.Config.ignoreMoonPhase.get(), Darkness.Config.minimumMoonLevel.get(), Darkness.Config.maximumMoonLevel.get(),
						Darkness.Config.darkOverworld.get(), Darkness.Config.darkDefault.get(), Darkness.Config.darkNether.get(), Darkness.Config.darkNetherFogConfigured.get(),
						Darkness.Config.darkEnd.get(), Darkness.Config.darkEndFogConfigured.get(), Darkness.Config.darkSkyless.get(), Darkness.Config.minimumLight.get()),
				player.connection.netManager, NetworkDirection.PLAY_TO_CLIENT);
	}

	private void updateConfigSettings() {
		Config.client_blockLightOnly = Config.blockLightOnly.get();
		Config.client_ignoreMoonPhase = Config.ignoreMoonPhase.get();
		Config.client_minimumMoonLevel = Config.minimumMoonLevel.get();
		Config.client_maximumMoonLevel = Config.maximumMoonLevel.get();
		Config.client_darkOverworld = Config.darkOverworld.get();
		Config.client_darkEnd = Config.darkEnd.get();
		Config.client_darkSkyless = Config.darkSkyless.get();
		Config.client_minimumLight = Config.minimumLight.get();
		bake();
	}

	public static final Config COMMON;
	public static final ForgeConfigSpec COMMON_SPEC;

	static {
		final Pair<Config, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(Config::new);
		COMMON_SPEC = specPair.getRight();
		COMMON = specPair.getLeft();
	}

	public static void bake() {
		Config.darkNetherFogEffective = Config.client_darkNether ? Config.darkNetherFogConfigured.get() : 1.0;
		Config.darkEndFogEffective = Config.client_darkEnd ? Config.darkEndFogConfigured.get() : 1.0;
	}

	public static boolean blockLightOnly() {
		return Config.client_blockLightOnly;
	}

	public static double darkNetherFog() {
		return Config.darkNetherFogEffective;
	}

	public static double darkEndFog() {
		return Config.darkEndFogEffective;
	}

	public static class Config {

		public static boolean onServer = false;
		public static double darkNetherFogEffective;
		public static double darkEndFogEffective;

		public static boolean client_blockLightOnly;
		public static boolean client_ignoreMoonPhase;
		public static double client_minimumMoonLevel;
		public static double client_maximumMoonLevel;
		public static boolean client_darkOverworld;
		public static boolean client_darkDefault;
		public static boolean client_darkNether;
		public static double client_darkNetherFogEffective;
		public static boolean client_darkEnd;
		public static double client_darkEndFogEffective;
		public static boolean client_darkSkyless;
		public static double client_minimumLight;

		public static ForgeConfigSpec.BooleanValue blockLightOnly;
		public static ForgeConfigSpec.BooleanValue ignoreMoonPhase;
		public static ForgeConfigSpec.DoubleValue minimumMoonLevel;
		public static ForgeConfigSpec.DoubleValue maximumMoonLevel;
		public static ForgeConfigSpec.BooleanValue darkOverworld;
		public static ForgeConfigSpec.BooleanValue darkDefault;
		public static ForgeConfigSpec.BooleanValue darkNether;
		public static ForgeConfigSpec.DoubleValue darkNetherFogConfigured;
		public static ForgeConfigSpec.BooleanValue darkEnd;
		public static ForgeConfigSpec.DoubleValue darkEndFogConfigured;
		public static ForgeConfigSpec.BooleanValue darkSkyless;
		public static ForgeConfigSpec.DoubleValue minimumLight;


		public Config(ForgeConfigSpec.Builder builder) {
			builder.push("general");
			blockLightOnly = builder.define("only_affect_block_light", false);
			ignoreMoonPhase = builder.define("ignore_moon_phase", false);
			minimumMoonLevel = builder.defineInRange("minimum_moon_brightness", 0, 0, 1d);
			maximumMoonLevel = builder.defineInRange("maximum_moon_brightness", 1d, 0, 1d);
			darkOverworld = builder.define("dark_overworld", true);
			darkDefault = builder.define("dark_default", true);
			darkNether = builder.define("dark_nether", true);
			darkNetherFogConfigured = builder.defineInRange("dark_nether_fog", .5, 0, 1d);
			darkEnd = builder.define("dark_end", true);
			darkEndFogConfigured = builder.defineInRange("dark_end_fog", 0, 0, 1d);
			darkSkyless = builder.define("dark_skyless", true);
			minimumLight = builder.defineInRange("minimum_light", 0d,0d,1d);
			builder.pop();
		}
	}
}
