package tfar.darkness;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.potion.Effects;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.DimensionType;
import net.minecraft.world.World;

public class DarknessClientUtils {

    private static boolean isDark(World world) {
        final RegistryKey<World> dimType = world.getDimensionKey();
        if (dimType == World.OVERWORLD) {
            return Darkness.Config.client_darkOverworld;
        } else if (dimType == World.THE_NETHER) {
            return Darkness.Config.client_darkNether;
        } else if (dimType == World.THE_END) {
            return Darkness.Config.client_darkEnd;
        } else if (world.getDimensionType().hasSkyLight()) {
            return Darkness.Config.client_darkDefault;
        } else {
            return Darkness.Config.client_darkSkyless;
        }
    }

    private static float skyFactor(World world) {
        if (!Darkness.Config.client_blockLightOnly && isDark(world)) {
            if (world.getDimensionType().hasSkyLight()) {
                final float angle = world.func_242415_f(0);
                if (angle > 0.25f && angle < 0.75f) {
                    final float oldWeight = Math.max(0, (Math.abs(angle - 0.5f) - 0.2f)) * 20;
                    final float moon = Darkness.Config.client_ignoreMoonPhase ? 0 : world.getMoonFactor();
                    final float moonInterpolated = (float) MathHelper.lerp(moon, Darkness.Config.client_minimumMoonLevel, Darkness.Config.client_maximumMoonLevel);
                    return MathHelper.lerp(oldWeight * oldWeight * oldWeight, moonInterpolated, 1f) ;
                } else {
                    return 1;
                }
            } else {
                return 0;
            }
        } else {
            return 1;
        }
    }

    public static boolean enabled = false;
    private static final float[][] LUMINANCE = new float[16][16];

    public static int darken(int c, int blockIndex, int skyIndex) {
        final float lTarget = LUMINANCE[blockIndex][skyIndex];
        final float r = (c & 0xFF) / 255f;
        final float g = ((c >> 8) & 0xFF) / 255f;
        final float b = ((c >> 16) & 0xFF) / 255f;
        final float l = luminance(r, g, b);
        final float f = l > 0 ? Math.min(1, lTarget / l) : 0;

        return f == 1f ? c : 0xFF000000 | Math.round(f * r * 255) | (Math.round(f * g * 255) << 8) | (Math.round(f * b * 255) << 16);
    }

    public static float luminance(float r, float g, float b) {
        return r * 0.2126f + g * 0.7152f + b * 0.0722f;
    }

    public static void updateLuminance(float tickDelta, Minecraft client, GameRenderer worldRenderer, float prevFlicker) {
        final ClientWorld world = client.world;
        if (world != null) {

            if (!isDark(world) || client.player.isPotionActive(Effects.NIGHT_VISION) ||
                    (client.player.isPotionActive(Effects.CONDUIT_POWER) && client.player.getWaterBrightness() > 0) || world.getTimeLightningFlash() > 0) {
                enabled = false;
                return;
            } else {
                enabled = true;
            }

            final float dimSkyFactor = skyFactor(world);
            final float ambient = world.getSunBrightness(1.0F);
            final DimensionType dim = world.getDimensionType();
            final boolean blockAmbient = !isDark(world);

            for (int skyIndex = 0; skyIndex < 16; ++skyIndex) {
                float skyFactor = 1f - skyIndex / 15f;
                skyFactor = 1 - skyFactor * skyFactor * skyFactor * skyFactor;
                skyFactor *= dimSkyFactor;

                float min = Math.max(skyFactor * 0.05f, (float) Darkness.Config.client_minimumLight);
                final float rawAmbient = ambient * skyFactor;
                final float minAmbient = rawAmbient * (1 - min) + min;
                final float skyBase = dim.getAmbientLight(skyIndex) * minAmbient;

                min = Math.max(0.35f * skyFactor, (float) Darkness.Config.client_minimumLight);
                float v = skyBase * (rawAmbient * (1 - min) + min);
                float skyRed = v;
                float skyGreen = v;
                float skyBlue = skyBase;

                if (worldRenderer.getBossColorModifier(tickDelta) > 0.0F) {
                    final float skyDarkness = worldRenderer.getBossColorModifier(tickDelta);
                    skyRed = skyRed * (1.0F - skyDarkness) + skyRed * 0.7F * skyDarkness;
                    skyGreen = skyGreen * (1.0F - skyDarkness) + skyGreen * 0.6F * skyDarkness;
                    skyBlue = skyBlue * (1.0F - skyDarkness) + skyBlue * 0.6F * skyDarkness;
                }

                for (int blockIndex = 0; blockIndex < 16; ++blockIndex) {
                    float blockFactor = 1f;
                    if (!blockAmbient) {
                        blockFactor = 1f - blockIndex / 15f;
                        blockFactor = 1 - blockFactor * blockFactor * blockFactor * blockFactor;
                    }

                    final float blockBase = blockFactor * dim.getAmbientLight(blockIndex) * (prevFlicker * 0.1F + 1.5F);
                    min = 0.4f * blockFactor;
                    final float blockGreen = blockBase * ((blockBase * (1 - min) + min) * (1 - min) + min);
                    final float blockBlue = blockBase * (blockBase * blockBase * (1 - min) + min);

                    float red = skyRed + blockBase;
                    float green = skyGreen + blockGreen;
                    float blue = skyBlue + blockBlue;

                    final float f = Math.max(skyFactor, blockFactor);
                    min = 0.03f * f;
                    red = red * (0.99F - min) + min;
                    green = green * (0.99F - min) + min;
                    blue = blue * (0.99F - min) + min;

                    //the end
                    if (world.getDimensionKey() == World.THE_END) {
                        red = skyFactor * 0.22F + blockBase * 0.75f;
                        green = skyFactor * 0.28F + blockGreen * 0.75f;
                        blue = skyFactor * 0.25F + blockBlue * 0.75f;
                    }

                    if (red > 1.0F) {
                        red = 1.0F;
                    }

                    if (green > 1.0F) {
                        green = 1.0F;
                    }

                    if (blue > 1.0F) {
                        blue = 1.0F;
                    }

                    final float gamma = (float) client.gameSettings.gamma * f;
                    float invRed = 1.0F - red;
                    float invGreen = 1.0F - green;
                    float invBlue = 1.0F - blue;
                    invRed = 1.0F - invRed * invRed * invRed * invRed;
                    invGreen = 1.0F - invGreen * invGreen * invGreen * invGreen;
                    invBlue = 1.0F - invBlue * invBlue * invBlue * invBlue;
                    red = red * (1.0F - gamma) + invRed * gamma;
                    green = green * (1.0F - gamma) + invGreen * gamma;
                    blue = blue * (1.0F - gamma) + invBlue * gamma;

                    min = Math.max(0.03f * f, (float) Darkness.Config.client_minimumLight);
                    red = red * (0.99F - min) + min;
                    green = green * (0.99F - min) + min;
                    blue = blue * (0.99F - min) + min;

                    if (red > 1.0F) {
                        red = 1.0F;
                    }

                    if (green > 1.0F) {
                        green = 1.0F;
                    }

                    if (blue > 1.0F) {
                        blue = 1.0F;
                    }

                    if (red < 0.0F) {
                        red = 0.0F;
                    }

                    if (green < 0.0F) {
                        green = 0.0F;
                    }

                    if (blue < 0.0F) {
                        blue = 0.0F;
                    }

                    LUMINANCE[blockIndex][skyIndex] = luminance(red, green, blue);
                }
            }
        }
    }
}
