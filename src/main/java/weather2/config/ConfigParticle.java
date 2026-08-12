package weather2.config;

import com.corosus.modconfig.ConfigComment;
import com.corosus.modconfig.IConfigCategory;
import weather2.Weather;

import java.io.File;


public class ConfigParticle implements IConfigCategory {


    @ConfigComment("Adjust amount of precipitation based particles, works as a multiplier")
	public static double Precipitation_Particle_effect_rate = 0.7D;


    @ConfigComment("Adjust amount of all weather2 based particles, works as a multiplier")
    public static double Particle_effect_rate = 1D;

    @ConfigComment("If true, uses vanilla rain/snow non particle precipitation")
    public static boolean Particle_vanilla_precipitation = false;

    @ConfigComment("If set to false, particles are spawned in using the vanilla particle renderer, may cause issues, performance seems worse")
    public static boolean Particle_engine_weather2 = true;

    @ConfigComment("Extra flying block particles to spawn when the tornado rips up a block")
    public static int Particle_Tornado_extraParticleCubes = 2;

    @Override
    public String getName() {
        return "Particle";
    }

    @Override
    public String getRegistryName() {
        return Weather.MODID + getName();
    }

    @Override
    public String getConfigFileName() {
        return "Weather2" + File.separator + getName();
    }

    @Override
    public String getCategory() {
        return "Weather2: " + getName();
    }

    @Override
    public void hookUpdatedValues() {

    }
}
