package weather2.ltcompat;

import net.minecraft.server.level.ServerLevel;
import weather2.datatypes.StormState;

public class ServerWeatherIntegration {

    public static float getWindSpeed(ServerLevel level) {
        return 0;
    }

    public static StormState getSandstormForEverywhere(ServerLevel level) {
        return null;
    }

    public static StormState getSnowstormForEverywhere(ServerLevel level) {
        return null;
    }


}
