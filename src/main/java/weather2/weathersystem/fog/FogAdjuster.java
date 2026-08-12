package weather2.weathersystem.fog;

import com.corosus.coroutil.util.CULog;
import net.minecraft.util.Mth;
import net.minecraftforge.client.event.ViewportEvent;
import org.joml.Vector3f;
import weather2.datatypes.WeatherEventType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.world.entity.player.Player;
import weather2.ClientTickHandler;
import weather2.ClientWeatherProxy;
import weather2.client.SceneEnhancer;
import weather2.util.WeatherUtilEntity;

import java.util.Random;

public class FogAdjuster {

    private FogProfile fogHeatwave;
    private FogProfile fogSandstorm;
    private FogProfile fogSnowstorm;


    private FogProfile fogVanilla;

    private FogProfile targetProfile;
    private FogProfile activeProfile;
    private FogProfile activeProfileLerps;

    private int lerpTicksCur = 20 * 15;
    private int lerpTicksMax = 20 * 15;


    private boolean useFarFog = false;

    public static WeatherEventType lastWeatherType = null;

    public int randDelay = 0;

    private boolean firstUseInit = true;


    public FogAdjuster() {
        initProfiles(false);
        activeProfile = new FogProfile(fogVanilla);
        targetProfile = fogVanilla;
        activeProfileLerps = new FogProfile(new Vector3f(0F, 0F, 0F), 0, 0);
    }

    public void initProfiles(boolean spectator) {

        float distAmp = 1F;
        if (spectator) {
            distAmp = 4F;
        }
        fogHeatwave = new FogProfile(new Vector3f(0.5F, 0.2F, 0.1F), 0, 75);
        fogSandstorm = new FogProfile(new Vector3f(0.7F, 0.5F, 0.2F), 0, 18 * distAmp);
        fogSnowstorm = new FogProfile(new Vector3f(0.7F, 0.7F, 0.7F), 0, 20 * distAmp);
        fogVanilla = new FogProfile(new Vector3f(-1F, -1F, -1F), -1, -1);
    }

    public void tickGame(ClientWeatherProxy weather) {
        updateWeatherState();

        boolean fogDisco = false;
        if (fogDisco) {

                if (randDelay <= 0) {
                    Random rand = new Random();
                    randDelay = 20 + rand.nextInt(5);
                    startRandom();
                }


            randDelay--;
        }

        if ((SceneEnhancer.getWeatherState() == WeatherEventType.SANDSTORM || SceneEnhancer.getWeatherState() == WeatherEventType.SNOWSTORM)) {
            Player player = Minecraft.getInstance().player;

            boolean isPlayerOutside = WeatherUtilEntity.isEntityOutside(player);
            boolean playerOutside = isPlayerOutside || player.isInWater();
            boolean setFogFar = !playerOutside || player.isSpectator();


            if (player != null) {
                if ((setFogFar && !useFarFog) || !setFogFar && useFarFog) {
                    initProfiles(setFogFar);
                    if (SceneEnhancer.getWeatherState() == WeatherEventType.SANDSTORM) {
                        startSandstorm();
                    } else if (SceneEnhancer.getWeatherState() == WeatherEventType.SNOWSTORM) {
                        startSnowstorm();
                    }
                }
                useFarFog = setFogFar;
            }
        }

        if (lerpTicksCur < lerpTicksMax) {
            float newLerpX = activeProfile.getRgb().x() + activeProfileLerps.getRgb().x();
            float newLerpY = activeProfile.getRgb().y() + activeProfileLerps.getRgb().y();
            float newLerpZ = activeProfile.getRgb().z() + activeProfileLerps.getRgb().z();
            activeProfile.getRgb().set(newLerpX, newLerpY, newLerpZ);

            activeProfile.setFogStart(activeProfile.getFogStart() + activeProfileLerps.getFogStart());
            activeProfile.setFogEnd(activeProfile.getFogEnd() + activeProfileLerps.getFogEnd());

            activeProfile.setFogStartSky(activeProfile.getFogStartSky() + activeProfileLerps.getFogStartSky());
            activeProfile.setFogEndSky(activeProfile.getFogEndSky() + activeProfileLerps.getFogEndSky());

            lerpTicksCur++;


        }
    }

    public void onFogColors(ViewportEvent.ComputeFogColor event) {
        updateWeatherState();


        fogVanilla.getRgb().set(event.getRed(), event.getGreen(), event.getBlue());

        if (SceneEnhancer.isFogOverridding()) {
            float brightness = Mth.clamp(Mth.cos(Minecraft.getInstance().level.getTimeOfDay(1F) * ((float)Math.PI * 2F)) * 2.0F + 0.5F, 0.0F, 1.0F);
            event.setRed(activeProfile.getRgb().x() * brightness);
            event.setGreen(activeProfile.getRgb().y() * brightness);
            event.setBlue(activeProfile.getRgb().z() * brightness);
        }
    }

    public void onFogRender(ViewportEvent.RenderFog event) {
        updateWeatherState();


        if (event.getMode() == FogRenderer.FogMode.FOG_SKY) {
            fogVanilla.setFogStartSky(event.getNearPlaneDistance());
            fogVanilla.setFogEndSky(event.getFarPlaneDistance());
        } else {
            fogVanilla.setFogStart(event.getNearPlaneDistance());
            fogVanilla.setFogEnd(event.getFarPlaneDistance());
        }

        if (SceneEnhancer.isFogOverridding()) {
            if (event.getMode() == FogRenderer.FogMode.FOG_SKY) {
                event.setNearPlaneDistance(activeProfile.getFogStartSky());
                event.setFarPlaneDistance(activeProfile.getFogEndSky());
                event.setCanceled(true);
            } else {
                event.setNearPlaneDistance(activeProfile.getFogStart());
                event.setFarPlaneDistance(activeProfile.getFogEnd());
                event.setCanceled(true);
            }
        }
    }

    public void startRandom() {
        Random rand = new Random();
        int randFog = 0;
        if (activeProfile.getFogEnd() < 50) {
            randFog = 50 + rand.nextInt(50);
        } else {
            randFog = rand.nextInt(50);
        }
        randFog = rand.nextInt(100);
        targetProfile = new FogProfile(new Vector3f(rand.nextFloat(), rand.nextFloat(), rand.nextFloat()), 0, randFog);
        lerpTicksMax = 20 + rand.nextInt(50);
        setupNewLerpRates();
        CULog.dbg("startRandom: " + randFog);
    }

    public void startHeatwave() {
        CULog.dbg("startHeatwave");

        targetProfile = new FogProfile(fogHeatwave);
        setupNewLerpRates();
    }

    public void startSandstorm() {
        CULog.dbg("startSandstorm");

        targetProfile = new FogProfile(fogSandstorm);
        setupNewLerpRates();
    }

    public void startSnowstorm() {
        CULog.dbg("startSnowstorm");

        targetProfile = new FogProfile(fogSnowstorm);
        setupNewLerpRates();
    }

    public void restoreVanilla() {
        CULog.dbg("restoreVanilla");

        targetProfile = new FogProfile(fogVanilla);
        setupNewLerpRates();
    }

    public void setupNewLerpRates() {
        if (firstUseInit) {

            if (fogVanilla.getFogEnd() != -1 && fogVanilla.getFogEndSky() != -1) {
                activeProfile = new FogProfile(fogVanilla);
                firstUseInit = false;
            }
        }

        lerpTicksCur = 0;
        float partialLerpX = getLerpRate(activeProfile.getRgb().x(), targetProfile.getRgb().x(), lerpTicksMax);
        float partialLerpY = getLerpRate(activeProfile.getRgb().y(), targetProfile.getRgb().y(), lerpTicksMax);
        float partialLerpZ = getLerpRate(activeProfile.getRgb().z(), targetProfile.getRgb().z(), lerpTicksMax);
        activeProfileLerps.getRgb().set(partialLerpX, partialLerpY, partialLerpZ);

        activeProfileLerps.setFogStart(getLerpRate(activeProfile.getFogStart(), targetProfile.getFogStart(), lerpTicksMax));
        activeProfileLerps.setFogEnd(getLerpRate(activeProfile.getFogEnd(), targetProfile.getFogEnd(), lerpTicksMax));
        activeProfileLerps.setFogStartSky(getLerpRate(activeProfile.getFogStartSky(), targetProfile.getFogStartSky(), lerpTicksMax));
        activeProfileLerps.setFogEndSky(getLerpRate(activeProfile.getFogEndSky(), targetProfile.getFogEndSky(), lerpTicksMax));
    }

    public float getLerpRate(float curVal, float endVal, float fullLerpTicks) {
        return (endVal - curVal) / fullLerpTicks;
    }

    public boolean isFogOverriding() {
        ClientTickHandler.getClientWeather();
        ClientWeatherProxy weather = ClientWeatherProxy.get();
        return (weather.isHeatwave() || weather.isSandstorm() || weather.isSnowstorm()) || lerpTicksCur < lerpTicksMax;
    }


    public void updateWeatherState() {
        WeatherEventType curWeather = SceneEnhancer.getWeatherState();


        if (curWeather == WeatherEventType.ACID_RAIN || curWeather == WeatherEventType.HEAVY_RAIN || curWeather == WeatherEventType.HAIL) {
            curWeather = null;
        }

        boolean match = false;
        if (curWeather != lastWeatherType) {
            if (curWeather == WeatherEventType.SANDSTORM) {
                startSandstorm();
                match = true;
            } else if (curWeather == WeatherEventType.SNOWSTORM) {
                startSnowstorm();
                match = true;
            } else if (curWeather == WeatherEventType.HEATWAVE) {
                startHeatwave();
                match = true;
            } else if (curWeather == null) {
                restoreVanilla();
                match = true;
            }
        }
        if (match) {
            lastWeatherType = curWeather;
        }
    }


    public float getLerpFraction() {
        if (lerpTicksMax == 0) return 0;
        return lerpTicksCur / lerpTicksMax;
    }
}
