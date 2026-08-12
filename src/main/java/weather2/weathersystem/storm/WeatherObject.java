package weather2.weathersystem.storm;

import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.util.thread.EffectiveSide;
import weather2.util.CachedNBTTagCompound;
import weather2.weathersystem.WeatherManager;

public class WeatherObject {

	public static long lastUsedStormID = 0;
	public long ID;
	public boolean isDead = false;


	public int ticksSinceNoNearPlayer = 0;

	public WeatherManager manager;

	public Vec3 pos = Vec3.ZERO;
	public Vec3 posGround = Vec3.ZERO;
	public Vec3 motion = Vec3.ZERO;


	public int size = 50;
	public int maxSize = 0;


	public EnumWeatherObjectType weatherObjectType = EnumWeatherObjectType.CLOUD;

	private CachedNBTTagCompound nbtCache;


	public WeatherObject(WeatherManager parManager) {
		manager = parManager;
		nbtCache = new CachedNBTTagCompound();
	}

	public void initFirstTime() {
		ID = lastUsedStormID++;
	}

	public void tick() {

	}

	@OnlyIn(Dist.CLIENT)
	public void tickRender(float partialTick) {

	}

	public void reset() {
		remove();
	}

	public void remove() {


		isDead = true;


		if (EffectiveSide.get().equals(LogicalSide.CLIENT)) {
			cleanupClient();
		}

		cleanup();
	}

	public void cleanup() {
		manager = null;
	}

	@OnlyIn(Dist.CLIENT)
	public void cleanupClient() {

	}

	public int getUpdateRateForNetwork() {
		return 100;
	}

	public void read() {

    }

	public void write() {

    }

	public void nbtSyncFromServer() {
		CachedNBTTagCompound parNBT = this.getNbtCache();
		ID = parNBT.getLong("ID");


		pos = new Vec3(parNBT.getDouble("posX"), parNBT.getDouble("posY"), parNBT.getDouble("posZ"));

		motion = new Vec3(parNBT.getDouble("vecX"), parNBT.getDouble("vecY"), parNBT.getDouble("vecZ"));
		size = parNBT.getInt("size");
		maxSize = parNBT.getInt("maxSize");
		this.weatherObjectType = EnumWeatherObjectType.get(parNBT.getInt("weatherObjectType"));
	}

	public void nbtSyncForClient() {
		CachedNBTTagCompound nbt = this.getNbtCache();
		nbt.putDouble("posX", pos.x);
		nbt.putDouble("posY", pos.y);
		nbt.putDouble("posZ", pos.z);


		nbt.putDouble("vecX", motion.x);
		nbt.putDouble("vecY", motion.y);
		nbt.putDouble("vecZ", motion.z);

		nbt.putLong("ID", ID);

		nbt.getNewNBT().putLong("ID", ID);

		nbt.putInt("size", size);
		nbt.putInt("maxSize", maxSize);
		nbt.putInt("weatherObjectType", this.weatherObjectType.ordinal());
	}

	public CachedNBTTagCompound getNbtCache() {
		return nbtCache;
	}

	public void setNbtCache(CachedNBTTagCompound nbtCache) {
		this.nbtCache = nbtCache;
	}

	public int getSize() {
		return size;
	}

}
