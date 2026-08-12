package weather2.config;

import com.corosus.modconfig.IConfigCategory;
import weather2.Weather;

import java.io.File;

public class ConfigFoliage implements IConfigCategory {


    @Override
    public String getName() {
        return "Foliage";
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
