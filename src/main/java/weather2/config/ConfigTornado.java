package weather2.config;

import com.corosus.modconfig.ConfigComment;
import com.corosus.modconfig.IConfigCategory;
import weather2.Weather;
import weather2.util.WeatherUtil;

import java.io.File;


public class ConfigTornado implements IConfigCategory {


    @ConfigComment("Prevents tearing up of dirt, grass, sand and logs, overrides strength based grabbing")
	public static boolean Storm_Tornado_RefinedGrabRules = true;
	@ConfigComment("Makes weather boring! or peacefull?")
	public static boolean Storm_NoTornadosOrCyclones = false;

	@ConfigComment("Grab player or not")
	public static boolean Storm_Tornado_grabPlayer = true;
	@ConfigComment("Prevent grabbing of non players")
	public static boolean Storm_Tornado_grabPlayersOnly = false;
	@ConfigComment("Grab hostile mobs, overridden by Storm_Tornado_grabPlayersOnly")
	public static boolean Storm_Tornado_grabMobs = true;
	@ConfigComment("Grab animals, overridden by Storm_Tornado_grabPlayersOnly")
	public static boolean Storm_Tornado_grabAnimals = true;
	@ConfigComment("Grab villagers, overridden by Storm_Tornado_grabPlayersOnly")
	public static boolean Storm_Tornado_grabVillagers = true;
	@ConfigComment("Tear up blocks from the ground based on conditions defined")
	public static boolean Storm_Tornado_grabBlocks = true;
	@ConfigComment("Grab entity items, overridden by Storm_Tornado_grabPlayersOnly")
	public static boolean Storm_Tornado_grabItems = false;
	@ConfigComment("Grab blocks based on how well a diamond axe can mine the block, so mostly wooden blocks")
	public static boolean Storm_Tornado_GrabCond_StrengthGrabbing = true;
	@ConfigComment("Use a list of blocks or block tags instead of grabbing based on calculated strength of block, if true this overrides StrengthGrabbing and RefinedGrabRules")
	public static boolean Storm_Tornado_GrabCond_List = false;


	@ConfigComment("Treat block grab list as a blacklist instead of whitelist")
	public static boolean Storm_Tornado_GrabListBlacklistMode = false;
	@ConfigComment("Enable Storm_Tornado_GrabCond_List to use, add registered block names or block tags to list, for tags, indicate with #, use commas to separate values, if namespace missing, 'minecraft:' is automatically used")
	public static String Storm_Tornado_GrabList = "#fences, #minecraft:fence_gates, #wooden_doors, #wooden_stairs, #wooden_slabs, #flowers, #planks, #wool, #wooden_trapdoors, #wooden_pressure_plates, #cave_vines, #saplings, #banners, #leaves, #small_flowers, #beds, #tall_flowers, #flowers, #candles, #wall_signs, #signs, #fire, #campfires, #replaceable_plants, #wall_post_override";
	@ConfigComment("Max amount of flying entity blocks allowed active, if it goes over this, it stops turning destroyed blocks into entities")
	public static int Storm_Tornado_maxFlyingEntityBlocks = 200;
	public static int Storm_Tornado_maxBlocksGrabbedPerTick = 5;


	@ConfigComment("Make tornados initial heading aimed towards closest player")
	public static boolean Storm_Tornado_aimAtPlayerOnSpawn = true;
	@ConfigComment("Accuracy of tornado aimed at player")
	public static int Storm_Tornado_aimAtPlayerAngleVariance = 5;

	@ConfigComment("Extra bit of grab angle for the tornado, tweak for different grab shapes, higher = tigher grab, lower = wider grab, might toss them away")
	public static int Storm_Tornado_extraGrabAngle = 20;

	public static boolean Storm_Tornado_fallDamage = true;


    @Override
    public String getName() {
        return "Tornado";
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
		WeatherUtil.updateGrabBlockList(Storm_Tornado_GrabList);


    }
}
