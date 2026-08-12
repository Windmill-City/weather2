package weather2.weathersystem.storm;

import com.corosus.coroutil.config.ConfigCoroUtil;
import com.corosus.coroutil.util.*;
import extendedrenderer.particle.ParticleRegistry;
import extendedrenderer.particle.behavior.ParticleBehaviorFog;
import extendedrenderer.particle.entity.EntityRotFX;
import extendedrenderer.particle.entity.ParticleCrossSection;
import extendedrenderer.particle.entity.ParticleCube;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Dolphin;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.util.thread.EffectiveSide;
import net.minecraftforge.registries.ForgeRegistries;
import weather2.EntityRegistry;
import weather2.ServerTickHandler;
import weather2.Weather;
import weather2.client.SceneEnhancer;
import weather2.config.*;
import weather2.player.PlayerData;
import weather2.util.*;
import weather2.weathersystem.WeatherManager;
import weather2.weathersystem.WeatherManagerServer;
import weather2.weathersystem.tornado.ActiveTornadoConfig;
import weather2.weathersystem.tornado.simple.Layer;
import weather2.weathersystem.tornado.simple.TornadoFunnelSimple;

import java.util.*;

public class StormObject extends WeatherObject {


	public String spawnerUUID = "";


	@OnlyIn(Dist.CLIENT)
	public HashMap<Integer, EntityRotFX> lookupParticlesCloud;

	@OnlyIn(Dist.CLIENT)
	public HashMap<Integer, EntityRotFX> lookupParticlesCloudLower;

	@OnlyIn(Dist.CLIENT)
	public HashMap<Integer, EntityRotFX> lookupParticlesFunnel;

	@OnlyIn(Dist.CLIENT)
	public List<EntityRotFX> listParticlesCloud;
	@OnlyIn(Dist.CLIENT)
	public List<EntityRotFX> listParticlesGround;
	@OnlyIn(Dist.CLIENT)
	public List<EntityRotFX> listParticlesFunnel;
	@OnlyIn(Dist.CLIENT)
	public List<EntityRotFX> listParticlesDebris;
	@OnlyIn(Dist.CLIENT)
	public ParticleBehaviorFog particleBehaviorFog;

	public int sizeMaxFunnelParticles = 600;


	public static int static_YPos_layer0 = ConfigMisc.Cloud_Layer0_Height;
	public static int static_YPos_layer1 = ConfigMisc.Cloud_Layer1_Height;
	public static int static_YPos_layer2 = ConfigMisc.Cloud_Layer2_Height;
	public static List<Integer> layers = new ArrayList<Integer>(Arrays.asList(static_YPos_layer0, static_YPos_layer1, static_YPos_layer2));
	public int layer = 0;

	public boolean angleIsOverridden = false;
	public float angleMovementTornadoOverride = 0;

	public float tempAngleFormingTornado = 0;


	public boolean isGrowing = true;


	public int levelWater = 0;
	public float levelWindMomentum = 0;
	public float levelTemperature = 0;


	public int levelWaterStartRaining = 100;


	public int levelStormIntensityMax = 0;


	public int levelCurIntensityStage = 0;
	public float levelCurStagesIntensity = 0;

	public boolean hasStormPeaked = false;

	public int maxIntensityStage = STATE_STAGE5;


	public int stormType = TYPE_LAND;
	public static int TYPE_LAND = 0;
	public static int TYPE_WATER = 1;


	public static int STATE_NORMAL = 0;
	public static int STATE_THUNDER = 1;
	public static int STATE_HIGHWIND = 2;
	public static int STATE_HAIL = 3;
	public static int STATE_FORMING = 4;
	public static int STATE_STAGE1 = 5;
	public static int STATE_STAGE2 = 6;
	public static int STATE_STAGE3 = 7;
	public static int STATE_STAGE4 = 8;
	public static int STATE_STAGE5 = 9;


	public static float levelStormIntensityFormingStartVal = STATE_FORMING;


	public double spinSpeed = 0.02D;


	public boolean attrib_precipitation = false;
	public boolean attrib_waterSpout = false;


	public float scale = 1F;
	public float strength = 100;
	public int maxHeight = 60;

	public int currentTopYBlock = -1;

	public TornadoHelper tornadoHelper = new TornadoHelper(this);
	private TornadoFunnelSimple tornadoFunnelSimple;


	public int updateLCG = (new Random()).nextInt();

    public float formingStrength = 0;

    public Vec3 posBaseFormationPos = new Vec3(pos.x, pos.y, pos.z);

    public boolean naturallySpawned = true;

	public boolean weatherMachineControlled = false;
    public boolean canSnowFromCloudTemperature = false;
    public boolean alwaysProgresses = false;


    public long ticksSinceLastPacketReceived = 0;


    public boolean canBeDeadly = true;


	public boolean cloudlessStorm = false;


	public float cachedAngleAvoidance = 0;

	public boolean isFirenado = false;

	public List<LivingEntity> listEntitiesUnderClouds = new ArrayList<>();

	private boolean playerControlled = false;
	private int playerControlledTimeLeft = 20;
	private boolean baby = false;
	private boolean pet = false;
	private boolean petGrabsItems = false;
	private boolean sharknado = false;

	private boolean configNeedsSync = true;

	private int age;
	private int ageSinceTornadoTouchdown;

	private boolean isBeingDeflectedCached = true;

	private boolean debugCloudTemperature = false;

	public StormObject(WeatherManager parManager) {
		super(parManager);

		pos = new Vec3(0, static_YPos_layer0, 0);
		maxSize = ConfigStorm.Storm_MaxRadius;

		if (parManager.getWorld().isClientSide()) {
			listParticlesCloud = new ArrayList<>();
			listParticlesFunnel = new ArrayList<>();
			listParticlesDebris = new ArrayList<>();
			listParticlesGround = new ArrayList<>();
			lookupParticlesCloud = new HashMap<>();
			lookupParticlesCloudLower = new HashMap<>();
			lookupParticlesFunnel = new HashMap<>();

		}
	}

	public void initFirstTime() {
		super.initFirstTime();

		Biome bgb = manager.getWorld().getBiome(WeatherUtilBlock.getPrecipitationHeightSafe(manager.getWorld(), new BlockPos(Mth.floor(pos.x), 0, Mth.floor(pos.z)))).get();

		float temp = 1;

		if (bgb != null) {

			temp = CoroUtilCompatibility.getAdjustedTemperature(manager.getWorld(), bgb, new BlockPos(Mth.floor(pos.x), Mth.floor(pos.y), Mth.floor(pos.z)));
		}


		if (naturallySpawned) {
			levelTemperature = getTemperatureMCToWeatherSys(temp);
			CULog.dbg("init levelTemperature: " + levelTemperature);
		}

		levelWindMomentum = 0;


	}

	public boolean isCloudlessStorm() {
		return cloudlessStorm;
	}

	public void setCloudlessStorm(boolean cloudlessStorm) {
		this.cloudlessStorm = cloudlessStorm;
	}

	public boolean isPrecipitating() {
		return attrib_precipitation;
	}

	public void setPrecipitating(boolean parVal) {
		attrib_precipitation = parVal;
	}

	public boolean isRealStorm() {
		return levelCurIntensityStage > STATE_NORMAL;
	}

	public boolean isTornadoFormingOrGreater() {
		return (stormType == TYPE_LAND && levelCurIntensityStage >= STATE_FORMING) || isPet();
	}

	public boolean isCycloneFormingOrGreater() {
		return stormType == TYPE_WATER && levelCurIntensityStage >= STATE_FORMING;
	}

	public boolean isSpinning() {
		return levelCurIntensityStage >= STATE_HIGHWIND;
	}

	public boolean isTropicalCyclone() {
		return levelCurIntensityStage >= STATE_STAGE1;
	}

	public boolean isHurricane() {
		return levelCurIntensityStage >= STATE_STAGE5;
	}

	@Override
	public void read()
    {
		super.read();
		nbtSyncFromServer();

		CachedNBTTagCompound var1 = this.getNbtCache();


		angleIsOverridden = var1.getBoolean("angleIsOverridden");
		angleMovementTornadoOverride = var1.getFloat("angleMovementTornadoOverride");

		spawnerUUID = var1.getString("spawnerUUID");
    }

    @Override
	public void write()
    {
		super.write();
		nbtSyncForClient();

		CachedNBTTagCompound nbt = this.getNbtCache();


		nbt.putBoolean("angleIsOverridden", angleIsOverridden);
		nbt.putFloat("angleMovementTornadoOverride", angleMovementTornadoOverride);

		nbt.putString("spawnerUUID", spawnerUUID);

    }


	@Override
	public void nbtSyncFromServer() {

		CachedNBTTagCompound parNBT = this.getNbtCache();

		boolean testNetworkData = false;
		if (testNetworkData) {
			System.out.println("Received payload from server; length=" + parNBT.getNewNBT().getAllKeys().size());
			Iterator iterator = parNBT.getNewNBT().getAllKeys().iterator();
			String keys = "";
			while (iterator.hasNext()) {
				keys = keys.concat((String) iterator.next() + "; ");
			}
			System.out.println("Received    " + keys);
		}

		super.nbtSyncFromServer();


		attrib_precipitation = parNBT.getBoolean("attrib_rain");
		attrib_waterSpout = parNBT.getBoolean("attrib_waterSpout");

		currentTopYBlock = parNBT.getInt("currentTopYBlock");

		levelTemperature = parNBT.getFloat("levelTemperature");
		levelWater = parNBT.getInt("levelWater");

		layer = parNBT.getInt("layer");


		levelCurIntensityStage = parNBT.getInt("levelCurIntensityStage");
		levelStormIntensityMax = parNBT.getInt("levelStormIntensityMax");
		levelCurStagesIntensity = parNBT.getFloat("levelCurStagesIntensity");
		stormType = parNBT.getInt("stormType");

		hasStormPeaked = parNBT.getBoolean("hasStormPeaked");


		isDead = parNBT.getBoolean("isDead");

		cloudlessStorm = parNBT.getBoolean("cloudlessStorm");

		isFirenado = parNBT.getBoolean("isFirenado");

		ticksSinceLastPacketReceived = 0;

		weatherMachineControlled = parNBT.getBoolean("weatherMachineControlled");


		String prefix = "tornadoFunnelData_layer_";
		if (parNBT.contains(prefix + "count") && tornadoFunnelSimple != null) {
			int count = parNBT.getInt(prefix + "count");
			for (int i = 0; i < tornadoFunnelSimple.listLayers.size(); i++) {
				Layer layer = tornadoFunnelSimple.listLayers.get(i);
				Vec3 newPos = new Vec3(parNBT.getDouble(prefix + i + "_posX"), parNBT.getDouble(prefix + i + "_posY"), parNBT.getDouble(prefix + i + "_posZ"));
				layer.setPos(newPos);
			}
		}

		playerControlled = parNBT.getBoolean("playerControlled");
		spawnerUUID = parNBT.getString("spawnerUUID");

		if (tornadoFunnelSimple != null && parNBT.contains("config")) {
			tornadoFunnelSimple.setConfig(ActiveTornadoConfig.deserialize(parNBT.get("config")));
		}

		baby = parNBT.getBoolean("baby");
		pet = parNBT.getBoolean("pet");
		petGrabsItems = parNBT.getBoolean("petGrabsItems");
		sharknado = parNBT.getBoolean("sharknado");

		if (posBaseFormationPos == Vec3.ZERO) {
			posBaseFormationPos = new Vec3(parNBT.getDouble("posBaseFormationPosX"), parNBT.getDouble("posBaseFormationPosX"), parNBT.getDouble("posBaseFormationPosX"));
		}

		isBeingDeflectedCached = parNBT.getBoolean("isBeingDeflectedCached");
	}


	@Override
	public void nbtSyncForClient() {
		super.nbtSyncForClient();

		CachedNBTTagCompound data = this.getNbtCache();


		if (attrib_precipitation) {

		}
		data.putBoolean("attrib_rain", attrib_precipitation);
		data.putBoolean("attrib_waterSpout", attrib_waterSpout);

		data.putInt("currentTopYBlock", currentTopYBlock);

		data.putFloat("levelTemperature", levelTemperature);
		data.putInt("levelWater", levelWater);

		data.putInt("layer", layer);


		data.putInt("levelCurIntensityStage", levelCurIntensityStage);
		data.putFloat("levelCurStagesIntensity", levelCurStagesIntensity);
		data.putFloat("levelStormIntensityMax", levelStormIntensityMax);
		data.putInt("stormType", stormType);

		data.putBoolean("hasStormPeaked", hasStormPeaked);


		data.putBoolean("isDead", isDead);

		data.putBoolean("cloudlessStorm", cloudlessStorm);


		data.putBoolean("isFirenado", isFirenado);

		data.putBoolean("weatherMachineControlled", weatherMachineControlled);


		if (manager != null && tornadoFunnelSimple != null && (manager.getWorld().getGameTime() % 100 == 0 || configNeedsSync)) {
			String prefix = "tornadoFunnelData_layer_";
			data.putInt(prefix + "count", tornadoFunnelSimple.listLayers.size());
			for (int i = 0; i < tornadoFunnelSimple.listLayers.size(); i++) {
				Vec3 layerPos = tornadoFunnelSimple.listLayers.get(i).getPos();
				data.putDouble(prefix + i + "_posX", layerPos.x);
				data.putDouble(prefix + i + "_posY", layerPos.y);
				data.putDouble(prefix + i + "_posZ", layerPos.z);
			}
		}

		data.putBoolean("playerControlled", playerControlled);
		data.putString("spawnerUUID", spawnerUUID);

		if (configNeedsSync && tornadoFunnelSimple != null) {
			data.put("config", tornadoFunnelSimple.getConfig().serialize());

			configNeedsSync = false;
		}

		data.putBoolean("baby", baby);
		data.putBoolean("pet", pet);
		data.putBoolean("petGrabsItems", petGrabsItems);
		data.putBoolean("sharknado", sharknado);


		if (manager != null && (manager.getWorld().getGameTime()) % (20*30) == 0) {
			data.setUpdateForced(true);
		} else {
			data.setUpdateForced(false);
		}

		data.putDouble("posBaseFormationPosX", posBaseFormationPos.x);
		data.putDouble("posBaseFormationPosY", posBaseFormationPos.y);
		data.putDouble("posBaseFormationPosZ", posBaseFormationPos.z);

		data.putBoolean("isBeingDeflectedCached", isBeingDeflectedCached);

	}

	public CompoundTag nbtForIMC() {

		nbtSyncForClient();
		return getNbtCache().getNewNBT();
	}

	@OnlyIn(Dist.CLIENT)
	public void tickRender(float partialTick) {
		super.tickRender(partialTick);


		if (!WeatherUtil.isPaused()) {

			int count = 8+1;


			Iterator<Map.Entry<Integer, EntityRotFX>> it = lookupParticlesCloud.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry<Integer, EntityRotFX> entry = it.next();
				EntityRotFX ent = entry.getValue();
				if (!ent.isAlive()) {
					it.remove();
				} else {
					int i = entry.getKey();
					Vec3 tryPos = null;
					double spawnRad = 120;
					double speed = 2D / (spawnRad);
					if (isSpinning()) {
						speed = 50D / (spawnRad);
					}
					ent.rotationSpeedAroundCenter = (float)speed;
					if (i == 0) {
						tryPos = new Vec3(pos.x, layers.get(layer), pos.z);
						ent.rotationYaw = ent.rotationAroundCenter;
					} else {
						double rad = Math.toRadians(ent.rotationAroundCenter - ent.rotationSpeedAroundCenter + (ent.rotationSpeedAroundCenter * partialTick));
						double x = -Math.sin(rad) * spawnRad;
						double z = Math.cos(rad) * spawnRad;
						tryPos = new Vec3(pos.x + x, layers.get(layer), pos.z + z);

						double var16 = this.pos.x - ent.getPosX();
						double var18 = this.pos.z - ent.getPosZ();
						ent.rotationYaw = (float)(Math.atan2(var18, var16) * 180.0D / Math.PI) - 90.0F;


					}
					ent.setPosition(tryPos.x, tryPos.y, tryPos.z);
				}
			}


			count = 16*2;

			it = lookupParticlesCloudLower.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry<Integer, EntityRotFX> entry = it.next();
				EntityRotFX ent = entry.getValue();
				if (!ent.isAlive()) {
					it.remove();
				} else {
					int i = entry.getKey();
					Vec3 tryPos = null;

					ent.setScale(800 * 0.15F);

					double countPerLayer = 16;
					double rotPos = i % 16;
					int layerRot = i / 16;
					double spawnRad = 80;
					if (layerRot == 1) {
						spawnRad = 60;
						ent.setScale(600 * 0.15F);
					}
					double speed = 50D / (spawnRad * 2D);

					ent.rotationSpeedAroundCenter = (float)speed;
					double rad = Math.toRadians(ent.rotationAroundCenter - ent.rotationSpeedAroundCenter + (ent.rotationSpeedAroundCenter * partialTick));
					double x = -Math.sin(rad) * spawnRad;
					double z = Math.cos(rad) * spawnRad;
					tryPos = new Vec3(pos.x + x, layers.get(layer) - 20, pos.z + z);

					ent.setPosition(tryPos.x, tryPos.y, tryPos.z);

					double var16 = this.pos.x - ent.getPosX();
					double var18 = this.pos.z - ent.getPosZ();
					ent.rotationYaw = (float)(Math.atan2(var18, var16) * 180.0D / Math.PI) - 90.0F;
					ent.rotationPitch = -20F;
				}
			}
		}
	}

	public void setupTornado() {
		ActiveTornadoConfig activeTornadoConfig;
		if (isPet()) {
			activeTornadoConfig = new ActiveTornadoConfig()
					.setHeight(1.7F)

					.setRadiusOfBase(0.5F)
					.setSpinSpeed(360F / 20F)
					.setRadiusIncreasePerLayer(0.02F)

					.setEntityPullDistXZ(2)
					.setEntityPullDistXZForY(2);
		} else if (isBaby()) {
			activeTornadoConfig = new ActiveTornadoConfig()
					.setHeight(20)

					.setRadiusOfBase(5F + 0F)
					.setSpinSpeed(360F / 20F)
					.setRadiusIncreasePerLayer(0.2F)
					.setEntityPullDistXZ(20)
					.setEntityPullDistXZForY(5);
		} else {
			activeTornadoConfig = new ActiveTornadoConfig()
					.setHeight(150)

					.setRadiusOfBase(5F + 5F)
					.setSpinSpeed(360F / 20F)
					.setRadiusIncreasePerLayer(0.2F)
					.setEntityPullDistXZ(120)
					.setEntityPullDistXZForY(90);
		}
		tornadoFunnelSimple = new TornadoFunnelSimple(activeTornadoConfig, this);
	}

	public void tick() {
		super.tick();
		age++;
		if (levelCurIntensityStage >= STATE_STAGE1) ageSinceTornadoTouchdown++;


		if (!playerControlled) {
			posGround = new Vec3(pos.x, currentTopYBlock, pos.z);
		} else {
			posGround = new Vec3(pos.x, pos.y, pos.z);
		}

		LogicalSide side = EffectiveSide.get();
		if (side == LogicalSide.CLIENT) {
			if (!WeatherUtil.isPaused()) {


				if (isTornadoFormingOrGreater() || isCycloneFormingOrGreater()) {
					setAndUpdateTornado();

					tornadoFunnelSimple.tick();
				} else {
					if (tornadoFunnelSimple != null && tornadoFunnelSimple.listLayers.size() > 0) {
						tornadoFunnelSimple.fadeOut();
					}
				}

				ticksSinceLastPacketReceived++;


					tickClient();


				if (isTornadoFormingOrGreater() || isCycloneFormingOrGreater()) {
					setAndUpdateTornado();
					if (!isBeingDeflectedCached()) {
						tornadoHelper.tick(manager.getWorld());
					}

					tornadoFunnelSimple.tickClient();
				}

				if (levelCurIntensityStage >= STATE_HIGHWIND) {
					if (manager.getWorld().isClientSide()) {
						tornadoHelper.soundUpdates(true, isTornadoFormingOrGreater() || isCycloneFormingOrGreater());
			        }
				}

				tickMovementClient();

				if (layer == 0) {

					posBaseFormationPos = calculateBaseFormationPos();
				}
			}
		} else {


			if (isTornadoFormingOrGreater() || isCycloneFormingOrGreater()) {
				setAndUpdateTornado();

				tornadoFunnelSimple.tick();
			}

			if (isCloudlessStorm()) {
				if (ConfigMisc.overcastMode && manager.getWorld().isRaining()) {
					this.setCloudlessStorm(false);
				}
			}

			if (isTornadoFormingOrGreater() || isCycloneFormingOrGreater()) {
				if (!isBeingDeflectedCached()) {
					tornadoHelper.tick(manager.getWorld());
				}
			}

			if (levelCurIntensityStage >= STATE_HIGHWIND) {
				if (manager.getWorld().isClientSide()) {
					tornadoHelper.soundUpdates(true, isTornadoFormingOrGreater() || isCycloneFormingOrGreater());
		        }
			}


			tickMovement();


			if (layer == 0) {
				if (!isCloudlessStorm()) {
					tickWeatherEvents();
					tickProgression();

				}
			} else {

				size = maxSize;
			}

			if (layer == 0) {

				posBaseFormationPos = calculateBaseFormationPos();
			}


		}

	}

	public Vec3 calculateBaseFormationPos() {
		Vec3 calculatedPos = new Vec3(pos.x, pos.y, pos.z);

		if (levelCurIntensityStage >= StormObject.levelStormIntensityFormingStartVal) {
			if (levelCurIntensityStage >= StormObject.levelStormIntensityFormingStartVal + 1) {
				formingStrength = 1;
				calculatedPos = new Vec3(calculatedPos.x, posGround.y, calculatedPos.z);
			} else {


				float intensityAdj = Math.min(1F, levelCurStagesIntensity * 2F);


				float val = (levelCurIntensityStage + intensityAdj) - StormObject.levelStormIntensityFormingStartVal;
				formingStrength = val;
				double yDiff = pos.y - posGround.y;
				calculatedPos = new Vec3(calculatedPos.x, pos.y - (yDiff * formingStrength), calculatedPos.z);
			}
		} else {
			if (levelCurIntensityStage == STATE_HIGHWIND) {
				formingStrength = 1;
				calculatedPos = new Vec3(calculatedPos.x, posGround.y, calculatedPos.z);
			} else {
				formingStrength = 0;
				calculatedPos = new Vec3(calculatedPos.x, pos.y, calculatedPos.z);
			}
		}
		return calculatedPos;
	}

	public void tickMovement() {

		double distToTarget = 1F;
		if (playerControlled) {
			Player entP = getPlayer();
			if (entP != null) {
				if (!isPet()) {


					this.pos = new Vec3(entP.position().x, entP.position().y, entP.position().z);
					this.posGround = new Vec3(entP.position().x, entP.position().y, entP.position().z);
				} else {
					distToTarget = entP.position().distanceTo(posGround);
					if (distToTarget > 5F) {
						aimStormAtClosestOrProvidedPlayer(entP);
					}
				}
			}
		}


		float angle = getAdjustedAngle();
		Random rand = new Random();

		if (angleIsOverridden) {
			angle = angleMovementTornadoOverride;


		}

		if (levelCurIntensityStage == STATE_FORMING) {
			angle = tempAngleFormingTornado;


			angle += (rand.nextFloat() - rand.nextFloat()) * 30F;
		}


		angle += (rand.nextFloat() - rand.nextFloat()) * 0.15F;


		double scanDist = 50;
		double scanX = this.pos.x + (-Math.sin(Math.toRadians(angle)) * scanDist);
		double scanZ = this.pos.z + (Math.cos(Math.toRadians(angle)) * scanDist);

		int height = WeatherUtilBlock.getPrecipitationHeightSafe(this.manager.getWorld(), CoroUtilBlock.blockPos(scanX, 0, scanZ)).getY();

		if (this.pos.y < height) {
			float angleAdj = 45;
			if (this.ID % 2 == 0) {
				angleAdj = -45;
			}
			angle += angleAdj;
		}


		tempAngleFormingTornado = angle;


		double vecX = -Math.sin(Math.toRadians(angle));
		double vecZ = Math.cos(Math.toRadians(angle));

		float cloudSpeedAmp = 0.2F;

		boolean love_tropics_tweaks = Weather.isLoveTropicsInstalled();

		if (love_tropics_tweaks) cloudSpeedAmp = 3F;


		float finalSpeed = getAdjustedSpeed() * cloudSpeedAmp;

		if (levelCurIntensityStage == STATE_FORMING) {
			finalSpeed = 0.2F;
		} else if (levelCurIntensityStage >= STATE_THUNDER) {
			finalSpeed = 0.15F;
		}

		if (!love_tropics_tweaks && levelCurIntensityStage >= levelStormIntensityFormingStartVal) {
			finalSpeed /= ((float)(levelCurIntensityStage-levelStormIntensityFormingStartVal+1F));
		}

		if (finalSpeed < 0.03F) {
			finalSpeed = 0.03F;
		}

		if (!love_tropics_tweaks && finalSpeed > 0.3F) {
			finalSpeed = 0.3F;
		}

		if (levelCurIntensityStage == STATE_FORMING) {
			finalSpeed = 0.3F;
		}

		if (love_tropics_tweaks) {
			finalSpeed = 0.1F;

		}

		if (playerControlled) {
			finalSpeed = 0.5F;
			Player player = getPlayer();
			if (player != null) {
				if (posGround.distanceTo(player.position()) > 30) {
					pos = new Vec3(player.position().x, player.position().y, player.position().z);
				}
			}
		}

		if (isPet()) {
			finalSpeed = 0.05F;
			if (distToTarget > 5.5F) {
				finalSpeed = 0.5F;
			}
		}

		if (isSharknado()) {
			finalSpeed = 0.1F;
		}

		if (manager.getWorld().getGameTime() % 100 == 0 && levelCurIntensityStage >= STATE_FORMING) {


		}


		if (isBeingDeflectedCached()) {
			finalSpeed *= 5;
		}

		boolean testing = false;
		if (testing) {
			finalSpeed = 0.5F;
		}

		if (!weatherMachineControlled) {
			motion = new Vec3(vecX * finalSpeed, 0, vecZ * finalSpeed);

			double max = 0.2D;


			pos = pos.add(motion);
		}

		if (levelCurIntensityStage >= STATE_FORMING) {
			Optional<BlockPos> optional = ((WeatherManagerServer) manager).findWeatherDeflector(CoroUtilBlock.blockPos(posGround), 128);
			if (optional.isPresent()) {
				isBeingDeflectedCached = true;

				aimAwayFromCoords(new Vec3(optional.get().getX(), optional.get().getY(), optional.get().getZ()));
				((ServerLevel) this.manager.getWorld()).sendParticles(DustParticleOptions.REDSTONE, optional.get().getX() + 0.5D, optional.get().getY() + 0.5D, optional.get().getZ() + 0.5D, 1, 0.3D, 0D, 0.3D, 1D);
			} else {
				isBeingDeflectedCached = false;
			}
		} else {
			isBeingDeflectedCached = false;
		}

	}

	public void tickMovementClient() {
		if (!weatherMachineControlled) {
			pos = pos.add(motion);
		}
	}

	public void tickWeatherEvents() {
		Random rand = new Random();
		Level world = manager.getWorld();


			currentTopYBlock = calculateTopYBlock();


		if (levelCurIntensityStage >= STATE_THUNDER && !isBaby() && !isPet()) {
			if (rand.nextInt((int)Math.max(1, ConfigStorm.Storm_LightningStrikeBaseValueOddsTo1 - (levelCurIntensityStage * 10))) == 0) {
				int x = (int) Math.floor(pos.x + rand.nextInt(size) - rand.nextInt(size));
				int z = (int) Math.floor(pos.z + rand.nextInt(size) - rand.nextInt(size));
				if (world.isLoaded(new BlockPos(x, 0, z))) {
					int y = world.getHeight(Heightmap.Types.MOTION_BLOCKING, x, z);
					if (world instanceof ServerLevel) {
						Optional<BlockPos> optional = ((ServerLevel)world).findLightningRod(new BlockPos(x, y, z));
						if (optional.isPresent()) {
							x = optional.get().getX();
							y = optional.get().getY();
							z = optional.get().getZ();
						}
					}

					LightningBoltWeatherNew ent = new LightningBoltWeatherNew(EntityRegistry.LIGHTNING_BOLT.get(), world);
					ent.setPos(x + 0.5, y, z + 0.5);
					addWeatherEffectLightning(ent);
				}
			}
		}


		if (isPrecipitating() && levelCurIntensityStage == STATE_HAIL && stormType == TYPE_LAND) {

			for (int i = 0; i < Math.max(1, ConfigStorm.Storm_HailPerTick * (size/maxSize)); i++) {
				int x = (int) Math.floor(pos.x + rand.nextInt(size) - rand.nextInt(size));
				int z = (int) Math.floor(pos.z + rand.nextInt(size) - rand.nextInt(size));
				if (world.isLoaded(new BlockPos(x, static_YPos_layer0, z)) && (world.getNearestPlayer(x, 50, z, 80, false) != null)) {


				} else {

				}
			}
		}

		trackAndExtinguishEntities();
	}

	public void trackAndExtinguishEntities() {

		if (ConfigStorm.Storm_Rain_TrackAndExtinguishEntitiesRate <= 0) return;

		if (isPrecipitating()) {


			if ((manager.getWorld().getGameTime() + (ID * 20)) % ConfigStorm.Storm_Rain_TrackAndExtinguishEntitiesRate == 0) {
				listEntitiesUnderClouds.clear();
				BlockPos posBP = CoroUtilBlock.blockPos(posGround.x, posGround.y, posGround.z);
				List<LivingEntity> listEnts = manager.getWorld().getEntitiesOfClass(LivingEntity.class, new AABB(posBP).inflate(size));
				for (LivingEntity ent : listEnts) {
					if (ent.level().canSeeSky(ent.blockPosition())) {
						listEntitiesUnderClouds.add(ent);
					}
				}
			}

			for (LivingEntity ent : listEntitiesUnderClouds) {
				if (!isFirenado) {
					ent.clearFire();
				}
			}
		}
	}

	public void tickProgression() {
		Level world = manager.getWorld();


		if (world.getGameTime() % 3 == 0) {
			if (isGrowing) {
				if (size < maxSize) {
					size++;
				} else {

				}
			} else {


			}


		}

		float tempAdjustRate = (float)ConfigStorm.Storm_TemperatureAdjustRate;
		int levelWaterBuildRate = ConfigStorm.Storm_Rain_WaterBuildUpRate;
		int levelWaterSpendRate = ConfigStorm.Storm_Rain_WaterSpendRate;
		int randomChanceOfWaterBuildFromWater = ConfigStorm.Storm_Rain_WaterBuildUpOddsTo1FromSource;
		int randomChanceOfWaterBuildFromNothing = ConfigStorm.Storm_Rain_WaterBuildUpOddsTo1FromNothing;
		int randomChanceOfWaterBuildFromOvercastRaining = ConfigStorm.Storm_Rain_WaterBuildUpOddsTo1FromOvercastRaining;
		randomChanceOfWaterBuildFromOvercastRaining = 10;


		boolean isInOcean = false;
		boolean isOverWater = false;
		boolean tryFormStorm = false;
		float levelStormIntensityRate = 0.02F;
		float minIntensityToProgress = 0.6F;

		if (world.getGameTime() % ConfigStorm.Storm_AllTypes_TickRateDelay == 0) {


			Player player = getPlayer();
			CompoundTag playerNBT = null;
			if (player != null) {
				playerNBT = player.getPersistentData();
			} else {
				playerNBT = new CompoundTag();
			}


			long lastStormDeadlyTime = playerNBT.getLong("lastStormDeadlyTime");


			if (lastStormDeadlyTime == 0) {
				lastStormDeadlyTime = world.getGameTime();
			}
			WeatherManagerServer wm = ServerTickHandler.getWeatherManagerFor(world.dimension());
			if (wm.lastStormFormed == 0) {
				wm.lastStormFormed = world.getGameTime();
			}


			Holder<Biome> bgb = world.getBiome(WeatherUtilBlock.getPrecipitationHeightSafe(world, new BlockPos(Mth.floor(pos.x), 0, Mth.floor(pos.z))));


			if (bgb.get() != null) {

				isInOcean = bgb.unwrap().left().toString().toLowerCase().contains("ocean");


				float biomeTempAdj = getTemperatureMCToWeatherSys(CoroUtilCompatibility.getAdjustedTemperature(manager.getWorld(), bgb.get(), new BlockPos(Mth.floor(pos.x), Mth.floor(pos.y), Mth.floor(pos.z))));
				if (levelTemperature > biomeTempAdj) {
					levelTemperature -= tempAdjustRate;
				} else {
					levelTemperature += tempAdjustRate;
				}
			}

			boolean performBuildup = false;

			Random rand = new Random();

			if (!isPrecipitating() && rand.nextInt(randomChanceOfWaterBuildFromNothing) == 0) {
				performBuildup = true;
			}

			if (!isPrecipitating() && ConfigMisc.overcastMode && manager.getWorld().isRaining() &&
					rand.nextInt(randomChanceOfWaterBuildFromOvercastRaining) == 0) {
				performBuildup = true;
			}

			BlockPos tryPos = new BlockPos(Mth.floor(pos.x), currentTopYBlock - 1, Mth.floor(pos.z));
			if (world.isLoaded(tryPos)) {
				BlockState state = world.getBlockState(tryPos);
				if (!CoroUtilBlock.isAir(state.getBlock())) {

					if (state.liquid()) {
						isOverWater = true;
					}
				}
			}


			if (!performBuildup && !isPrecipitating() && rand.nextInt(randomChanceOfWaterBuildFromWater) == 0) {
				if (isOverWater) {
					performBuildup = true;
				}

				if (bgb.get() != null) {
					String biomecat = bgb.unwrap().left().toString().toLowerCase();

					if (!performBuildup && (isInOcean || biomecat.contains("swamp") || biomecat.contains("jungle") || biomecat.contains("river"))) {
						performBuildup = true;
					}
				}
			}

			if (performBuildup) {

				levelWater += levelWaterBuildRate;
				Weather.dbg(ID + ": building rain: " + levelWater);
			}


			if (isPrecipitating()) {
				levelWater -= levelWaterSpendRate;


				if (levelWater < 0) levelWater = 0;

				if (levelWater <= 0) {
					setPrecipitating(false);
					Weather.dbg("ending raining for: " + ID);
				}
			} else {
				if (levelWater >= levelWaterStartRaining) {
					if (ConfigMisc.overcastMode) {
						if (manager.getWorld().isRaining()) {
							if (ConfigStorm.Storm_Rain_Overcast_OddsTo1 != -1 && rand.nextInt(ConfigStorm.Storm_Rain_Overcast_OddsTo1) == 0) {
								setPrecipitating(true);
								Weather.dbg("starting raining for: " + ID);
							}
						}
					} else {
						if (ConfigStorm.Storm_Rain_OddsTo1 != -1 && rand.nextInt(ConfigStorm.Storm_Rain_OddsTo1) == 0) {
							setPrecipitating(true);
							Weather.dbg("starting raining for: " + ID);
						}
					}
				}

			}


			boolean tempAlwaysFormStorm = false;

			if (this.canBeDeadly && this.levelCurIntensityStage == STATE_NORMAL) {
				if (ConfigStorm.Server_Storm_Deadly_UseGlobalRate) {
					if (ConfigStorm.Server_Storm_Deadly_TimeBetweenInTicks != -1) {
						if (wm.lastStormFormed == 0 || wm.lastStormFormed + ConfigStorm.Server_Storm_Deadly_TimeBetweenInTicks < world.getGameTime()) {
							tryFormStorm = true;
						} else if (ConfigStorm.Server_Storm_Deadly_TimeBetweenInTicks_Land_Based != -1 && wm.lastStormFormed + ConfigStorm.Server_Storm_Deadly_TimeBetweenInTicks_Land_Based < world.getGameTime()) {
							tryFormStorm = true;
						}
					}
				} else {
					if (tempAlwaysFormStorm || ConfigStorm.Player_Storm_Deadly_TimeBetweenInTicks != -1) {
						if (tempAlwaysFormStorm || lastStormDeadlyTime == 0 || lastStormDeadlyTime + ConfigStorm.Player_Storm_Deadly_TimeBetweenInTicks < world.getGameTime()) {
							tryFormStorm = true;
						} else if (ConfigStorm.Player_Storm_Deadly_TimeBetweenInTicks_Land_Based != -1 && lastStormDeadlyTime + ConfigStorm.Player_Storm_Deadly_TimeBetweenInTicks_Land_Based < world.getGameTime()) {
							tryFormStorm = true;
						}
					}
				}
			}

			if (weatherMachineControlled) {
			    return;
            }

			if (((ConfigMisc.overcastMode && manager.getWorld().isRaining()) || !ConfigMisc.overcastMode)
					&& WeatherUtilConfig.listDimensionsStorms.contains(manager.getWorld().dimension().location().toString()) && tryFormStorm) {
				int stormFrontCollideDist = ConfigStorm.Storm_Deadly_CollideDistance;
				int randomChanceOfCollide = ConfigStorm.Player_Storm_Deadly_OddsTo1;
				int randomChanceOfCollideLand = ConfigStorm.Player_Storm_Deadly_OddsTo1_Land_Based;

				if (ConfigStorm.Server_Storm_Deadly_UseGlobalRate) {
					randomChanceOfCollide = ConfigStorm.Server_Storm_Deadly_OddsTo1;
					randomChanceOfCollideLand = ConfigStorm.Server_Storm_Deadly_OddsTo1_Land_Based;
				}

				if (isInOcean && (ConfigStorm.Storm_OddsTo1OfOceanBasedStorm > 0 && rand.nextInt(ConfigStorm.Storm_OddsTo1OfOceanBasedStorm) == 0)) {
					Player entP = getPlayer();

					if (entP != null) {
						initRealStorm(entP, null);
					} else {
						initRealStorm(null, null);
					}

					if (ConfigStorm.Server_Storm_Deadly_UseGlobalRate) {
						wm.lastStormFormed = world.getGameTime();
					} else {
						playerNBT.putLong("lastStormDeadlyTime", world.getGameTime());
					}
				} else if ((!isInOcean && randomChanceOfCollideLand > 0 && rand.nextInt(randomChanceOfCollideLand) == 0)) {
					Player entP = getPlayer();

					if (entP != null) {
						initRealStorm(entP, null);
					} else {
						initRealStorm(null, null);
					}

					if (ConfigStorm.Server_Storm_Deadly_UseGlobalRate) {
						wm.lastStormFormed = world.getGameTime();
					} else {
						playerNBT.putLong("lastStormDeadlyTime", world.getGameTime());
					}
				} else if (rand.nextInt(randomChanceOfCollide) == 0) {
					for (int i = 0; i < manager.getStormObjects().size(); i++) {
						WeatherObject wo = manager.getStormObjects().get(i);

						if (wo instanceof StormObject) {
							StormObject so = (StormObject) wo;


							boolean startStorm = false;

							if (so.ID != this.ID && so.levelCurIntensityStage <= 0 && !so.isCloudlessStorm() && !so.weatherMachineControlled) {
								if (so.pos.distanceTo(pos) < stormFrontCollideDist) {
									if (this.levelTemperature < 0) {
										if (so.levelTemperature > 0) {
											startStorm = true;
										}
									} else if (this.levelTemperature > 0) {
										if (so.levelTemperature < 0) {
											startStorm = true;
										}
									}
								}
							}

							if (startStorm) {


								playerNBT.putLong("lastStormDeadlyTime", world.getGameTime());


								Player entP = getPlayer();

								if (entP != null) {
									initRealStorm(entP, so);
								} else {
									initRealStorm(null, so);


								}

								break;
							}
						}

					}
				}
			}

			if (isRealStorm()) {


				if (ConfigMisc.overcastMode) {
					if (!manager.getWorld().isRaining()) {
						hasStormPeaked = true;
					}
				}


				if (!hasStormPeaked) {
					levelWater = levelWaterStartRaining;
					setPrecipitating(true);
				}


				if ((levelCurIntensityStage == STATE_HIGHWIND || levelCurIntensityStage == STATE_HAIL) && isOverWater) {
					if (ConfigStorm.Storm_OddsTo1OfHighWindWaterSpout != 0 && rand.nextInt(ConfigStorm.Storm_OddsTo1OfHighWindWaterSpout) == 0) {
						attrib_waterSpout = true;
					}
				} else {
					attrib_waterSpout = false;
				}


				if (!hasStormPeaked) {

					if (levelCurIntensityStage < maxIntensityStage && (!ConfigTornado.Storm_NoTornadosOrCyclones || levelCurIntensityStage < STATE_FORMING-1)) {
						if (levelCurStagesIntensity >= minIntensityToProgress) {

							if (alwaysProgresses || levelCurIntensityStage < levelStormIntensityMax) {
								stageNext();
								Weather.dbg("storm ID: " + this.ID + " - growing, stage: " + levelCurIntensityStage + " pos: " + pos);


								if (isInOcean && false) {

									if (levelCurIntensityStage == STATE_FORMING) {
										Weather.dbg("storm ID: " + this.ID + " marked as tropical cyclone!");
										stormType = TYPE_WATER;


										levelStormIntensityMax = rollDiceOnMaxIntensity();
										Weather.dbg("rerolled odds for ocean storm, max stage will be: " + levelStormIntensityMax);
									}
								}
							}
						}


						if (levelCurIntensityStage == STATE_FORMING) {
							if (levelCurStagesIntensity >= 0.5 && levelCurStagesIntensity < 0.9) {
								levelCurStagesIntensity = 0.90F;
							}
						}
					}


					Weather.dbg("storm ID: " + this.ID + " - growing, stage " + levelCurIntensityStage + " of max " + levelStormIntensityMax + ", at intensity: " + levelCurStagesIntensity + " pos: " + pos);

					if (levelCurStagesIntensity >= 1F) {
						Weather.dbg("storm peaked at: " + levelCurIntensityStage);
						hasStormPeaked = true;
					}
				} else {

					if (ConfigMisc.overcastMode && manager.getWorld().isRaining()) {
						levelCurStagesIntensity -= levelStormIntensityRate * 0.9F;
					} else {
						levelCurStagesIntensity -= levelStormIntensityRate * 0.3F;
						if (levelCurIntensityStage >= STATE_FORMING) {
							Weather.dbg("storm ID: " + this.ID + " - active info, stage: " + levelCurIntensityStage + " levelCurStagesIntensity: " + levelCurStagesIntensity + " pos: " + pos);
						}
					}


					if (levelCurIntensityStage == STATE_FORMING) {
						if (levelCurStagesIntensity > 0.5) {
							levelCurStagesIntensity = 0.5F;
						}


						levelCurStagesIntensity -= levelStormIntensityRate * 0.9F;
					}


					if (levelCurStagesIntensity <= 0) {
						stagePrev();
						Weather.dbg("storm ID: " + this.ID + " - dying, stage: " + levelCurIntensityStage + " pos: " + pos);
						if (levelCurIntensityStage <= 0) {
							setNoStorm();
						}
					}


				}


			} else {
				if (ConfigMisc.overcastMode) {
					if (!manager.getWorld().isRaining()) {
						if (attrib_precipitation) {
							setPrecipitating(false);
						}
					}
				}
			}
		}


		if (world.getGameTime() % 2 == 0) {
			if (((ConfigMisc.overcastMode && manager.getWorld().isRaining()) || !ConfigMisc.overcastMode) && WeatherUtilConfig.listDimensionsStorms.contains(manager.getWorld().dimension().location().toString())) {
				if (isRealStorm() && !hasStormPeaked) {


					if (levelCurIntensityStage >= levelStormIntensityFormingStartVal) {
						levelStormIntensityRate *= 3;

					}


					levelCurStagesIntensity += levelStormIntensityRate / 30F;
				}
			}
		}

		if (playerControlled) {
			if (playerControlledTimeLeft > 0) {
				playerControlledTimeLeft--;

				if (playerControlledTimeLeft <= 0) {
					featherFallAllNearbyPlayers();
					remove();
				}
			}
		}
	}

	public void featherFallAllNearbyPlayers() {


		tornadoHelper.forceRotate(manager.getWorld(), true);
	}

	public WeatherEntityConfig getWeatherEntityConfigForStorm() {

		WeatherEntityConfig weatherConfig = WeatherTypes.weatherEntTypes.get(0);
		if (levelCurIntensityStage >= STATE_STAGE5) {
			weatherConfig = WeatherTypes.weatherEntTypes.get(5);
		} else if (levelCurIntensityStage >= STATE_STAGE4) {
			weatherConfig = WeatherTypes.weatherEntTypes.get(4);
		} else if (levelCurIntensityStage >= STATE_STAGE3) {
			weatherConfig = WeatherTypes.weatherEntTypes.get(3);
		} else if (levelCurIntensityStage >= STATE_STAGE2) {
			weatherConfig = WeatherTypes.weatherEntTypes.get(2);
		} else if (levelCurIntensityStage >= STATE_STAGE1) {
			weatherConfig = WeatherTypes.weatherEntTypes.get(1);
		} else if (levelCurIntensityStage >= STATE_FORMING) {
			weatherConfig = WeatherTypes.weatherEntTypes.get(0);
		}
		return weatherConfig;
	}

	public void stageNext() {
		levelCurIntensityStage++;
		levelCurStagesIntensity = 0F;
		if (ConfigTornado.Storm_Tornado_aimAtPlayerOnSpawn) {
			if (!hasStormPeaked && levelCurIntensityStage == STATE_FORMING) {
				if (!Weather.isLoveTropicsInstalled()) {
					aimStormAtClosestOrProvidedPlayer(null);
				}
			}
		}
	}

	public void stagePrev() {
		levelCurIntensityStage--;
		levelCurStagesIntensity = 1F;
	}

	public void initRealStorm(Player entP, StormObject stormToAbsorb) {


		levelCurIntensityStage = STATE_THUNDER;


		float diff = 4;
		if (stormToAbsorb != null) {
			diff = this.levelTemperature - stormToAbsorb.levelTemperature;
		}
		if (naturallySpawned) {
			this.levelWater = this.levelWaterStartRaining * 2;


		}

		this.levelStormIntensityMax = rollDiceOnMaxIntensity();
		Weather.dbg("rolled odds for storm, unless it becomes ocean storm, max stage will be: " + levelStormIntensityMax);

		this.attrib_precipitation = true;

		if (stormToAbsorb != null) {
			Weather.dbg("stormfront collision happened between ID " + this.ID + " and " + stormToAbsorb.ID);
			manager.removeStormObject(stormToAbsorb.ID);
			((WeatherManagerServer)manager).syncStormRemove(stormToAbsorb);
		} else {
			Weather.dbg("ocean storm happened, ID " + this.ID);
		}

		if (ConfigTornado.Storm_Tornado_aimAtPlayerOnSpawn) {
			if (!Weather.isLoveTropicsInstalled()) {
				aimStormAtClosestOrProvidedPlayer(entP);
			}

		}
	}

	public int rollDiceOnMaxIntensity() {
		Random rand = new Random();
		int randVal = rand.nextInt(100);
		if (stormType == TYPE_LAND) {
			if (randVal <= ConfigStorm.Storm_PercentChanceOf_F5_Tornado) {
				return STATE_STAGE5;
			} else if (randVal <= ConfigStorm.Storm_PercentChanceOf_F4_Tornado) {
				return STATE_STAGE4;
			} else if (randVal <= ConfigStorm.Storm_PercentChanceOf_F3_Tornado) {
				return STATE_STAGE3;
			} else if (randVal <= ConfigStorm.Storm_PercentChanceOf_F2_Tornado) {
				return STATE_STAGE2;
			} else if (randVal <= ConfigStorm.Storm_PercentChanceOf_F1_Tornado) {
				return STATE_STAGE1;
			} else if (randVal <= ConfigStorm.Storm_PercentChanceOf_F0_Tornado) {
				return STATE_FORMING;
			} else if (randVal <= ConfigStorm.Storm_PercentChanceOf_Hail) {
				return STATE_HAIL;
			} else if (randVal <= ConfigStorm.Storm_PercentChanceOf_HighWind) {
				return STATE_HIGHWIND;
			}
		} else if (stormType == TYPE_WATER) {
			if (randVal <= ConfigStorm.Storm_PercentChanceOf_C5_Cyclone) {
				return STATE_STAGE5;
			} else if (randVal <= ConfigStorm.Storm_PercentChanceOf_C4_Cyclone) {
				return STATE_STAGE4;
			} else if (randVal <= ConfigStorm.Storm_PercentChanceOf_C3_Cyclone) {
				return STATE_STAGE3;
			} else if (randVal <= ConfigStorm.Storm_PercentChanceOf_C2_Cyclone) {
				return STATE_STAGE2;
			} else if (randVal <= ConfigStorm.Storm_PercentChanceOf_C1_Cyclone) {
				return STATE_STAGE1;
			} else if (randVal <= ConfigStorm.Storm_PercentChanceOf_C0_Cyclone) {
				return STATE_FORMING;
			} else if (randVal <= ConfigStorm.Storm_PercentChanceOf_Hail) {
				return STATE_HAIL;
			} else if (randVal <= ConfigStorm.Storm_PercentChanceOf_HighWind) {
				return STATE_HIGHWIND;
			}
		}

		return STATE_THUNDER;
	}

	public void aimStormAtClosestOrProvidedPlayer(Player entP) {

		if (entP == null) {
			entP = manager.getWorld().getNearestPlayer(pos.x, pos.y, pos.z, -1, false);
		}

		if (entP != null) {
			aimAtCoords(entP.position());
		}
	}

	public void aimAtCoords(Vec3 vec) {
		Random rand = new Random();
		double var11 = vec.x() - pos.x;
		double var15 = vec.z() - pos.z;
		float yaw = -(float)(Math.atan2(var11, var15) * 180.0D / Math.PI);


		int size = ConfigTornado.Storm_Tornado_aimAtPlayerAngleVariance;
		if (size > 0) {
			if (!Weather.isLoveTropicsInstalled()) {
				yaw += rand.nextInt(size) - (size / 2);
			}
		}

		angleIsOverridden = true;
		angleMovementTornadoOverride = yaw;


	}

	public void aimAwayFromCoords(Vec3 vec) {
		Random rand = new Random();
		double var11 = vec.x() - pos.x;
		double var15 = vec.z() - pos.z;
		float yaw = -(float)(Math.atan2(var11, var15) * 180.0D / Math.PI);

		yaw += 180;

		angleIsOverridden = true;
		angleMovementTornadoOverride = yaw;


	}


	public void setNoStorm() {
		Weather.dbg("storm ID: " + this.ID + " - ended storm event");
		levelCurIntensityStage = STATE_NORMAL;
		levelCurStagesIntensity = 0;
	}

	@OnlyIn(Dist.CLIENT)
	public void tickClient() {

		if (false && ConfigCoroUtil.useLoggingDebug && SceneEnhancer.particleBehavior != null) {
			TextureAtlasSprite sprite = ParticleRegistry.tumbleweed;

			ParticleCrossSection part = new ParticleCrossSection(manager.getWorld(), pos.x, pos.y - 20, pos.z,
					0, 0, 0, sprite);
			SceneEnhancer.particleBehavior.initParticle(part);
			SceneEnhancer.particleBehavior.initParticleSandstormTumbleweed(part);
			part.windWeight = 9999;
			part.setGravity(0.5F);
			if (cloudlessStorm) {
				part.setGravity(0);
			}
			SceneEnhancer.particleBehavior.particles.add(part);
			part.spawnAsWeatherEffect();
		}

		if (isCloudlessStorm()) return;

		if (particleBehaviorFog == null) {
			particleBehaviorFog = new ParticleBehaviorFog(new Vec3(pos.x, pos.y, pos.z));

		} else {
			if (!Minecraft.getInstance().isPaused()) {
				particleBehaviorFog.tickUpdateList();
			}
		}

		Player entP = Minecraft.getInstance().player;

		spinSpeed = 0.02D;
		double spinSpeedMax = 0.4D;


if (isCycloneFormingOrGreater()) {
			spinSpeed = spinSpeedMax * 0.00D + ((levelCurIntensityStage-levelStormIntensityFormingStartVal+1) * spinSpeedMax * 0.2D);

		} else if (isTornadoFormingOrGreater()) {
			spinSpeed = spinSpeedMax * 0.2D;
		} else if (levelCurIntensityStage >= STATE_HIGHWIND) {
			spinSpeed = spinSpeedMax * 0.05D;
		} else {
			spinSpeed = spinSpeedMax * 0.02D;
		}


		if (isHurricane()) {
			spinSpeed += 0.1D;
		}

		if (size == 0) size = 1;
		int delay = Math.max(1, (int)(100F / size * 1F));
		int loopSize = 1;

		int extraSpawning = 0;

		if (isSpinning()) {
			loopSize += 4;
			extraSpawning = 300;
		}


		if (stormType == TYPE_WATER) {
			if (levelCurIntensityStage >= STATE_STAGE5) {
				loopSize = 10;
				extraSpawning = 800;
			} else if (levelCurIntensityStage >= STATE_STAGE4) {
				loopSize = 8;
				extraSpawning = 700;
			} else if (levelCurIntensityStage >= STATE_STAGE3) {
				loopSize = 6;
				extraSpawning = 500;
			} else if (levelCurIntensityStage >= STATE_STAGE2) {
				loopSize = 4;
				extraSpawning = 400;
			} else {
				extraSpawning = 300;
			}
		}


		Random rand = new Random();

		Vec3 playerAdjPos = new Vec3(entP.getX(), pos.y, entP.getZ());
		double maxSpawnDistFromPlayer = 512;


		if (!pet && !baby && this.manager.getWorld().getGameTime() % (delay + (isSpinning() ? ConfigStorm.Storm_ParticleSpawnDelay : ConfigMisc.Cloud_ParticleSpawnDelay)) == 0) {
			for (int i = 0; i < loopSize; i++) {


				if (listParticlesCloud.size() < (size + extraSpawning) / 1F) {
					double spawnRad = size;


					Vec3 tryPos = new Vec3(pos.x + (rand.nextDouble()*spawnRad) - (rand.nextDouble()*spawnRad), getPosTop().y + 30, pos.z + (rand.nextDouble()*spawnRad) - (rand.nextDouble()*spawnRad));
					if (tryPos.distanceTo(playerAdjPos) < maxSpawnDistFromPlayer) {
						if (getAvoidAngleIfTerrainAtOrAheadOfPosition(getAdjustedAngle(), tryPos) == 0) {
							EntityRotFX particle;
							if (WeatherUtil.isAprilFoolsDay() && !Weather.isLoveTropicsInstalled()) {
								particle = spawnFogParticle(tryPos.x, tryPos.y, tryPos.z, 0, ParticleRegistry.chicken);
							} else {

								particle = spawnFogParticle(tryPos.x, tryPos.y, tryPos.z, 0);
								if (isFirenado && isSpinning()) {

										particle.setSprite(ParticleRegistry.cloud256_fire);
										particle.setColor(1F, 1F, 1F);


								}
							}


							listParticlesCloud.add(particle);
						}
					}
				}


			}
		}


		if (levelCurIntensityStage >= STATE_HIGHWIND && !baby && !pet) {
			for (int i = 0; i < (stormType == TYPE_WATER ? 50 : 3); i++) {
				if (listParticlesGround.size() < (stormType == TYPE_WATER ? 600 : 150)) {
					double spawnRad = size/4*3;

					if (stormType == TYPE_WATER) {
						spawnRad = size*3;
					}


					Vec3 tryPos = new Vec3(pos.x + (rand.nextDouble()*spawnRad) - (rand.nextDouble()*spawnRad), posGround.y, pos.z + (rand.nextDouble()*spawnRad) - (rand.nextDouble()*spawnRad));
					if (tryPos.distanceTo(playerAdjPos) < maxSpawnDistFromPlayer) {
						int groundY = WeatherUtilBlock.getPrecipitationHeightSafe(manager.getWorld(), new BlockPos((int)tryPos.x, 0, (int)tryPos.z)).getY();
						EntityRotFX particle;
						if (WeatherUtil.isAprilFoolsDay() && !Weather.isLoveTropicsInstalled()) {
							particle = spawnFogParticle(tryPos.x, groundY + 3, tryPos.z, 0, ParticleRegistry.potato);
						} else {
							particle = spawnFogParticle(tryPos.x, groundY + 3, tryPos.z, 0);
						}

						particle.setScale(200 * 0.15F);
						particle.rotationYaw = rand.nextInt(360);
						particle.rotationPitch = rand.nextInt(360);

						listParticlesGround.add(particle);


					}
				}


			}
		}

		delay = 1;
		loopSize = 2;

		double spawnRad = size/48;

		if (levelCurIntensityStage >= STATE_STAGE5) {
			spawnRad = 200;
			loopSize = 10;
			sizeMaxFunnelParticles = 1200;
		} else if (levelCurIntensityStage >= STATE_STAGE4) {
			spawnRad = 150;
			loopSize = 8;
			sizeMaxFunnelParticles = 1000;
		} else if (levelCurIntensityStage >= STATE_STAGE3) {
			spawnRad = 100;
			loopSize = 6;
			sizeMaxFunnelParticles = 800;
		} else if (levelCurIntensityStage >= STATE_STAGE2) {
			spawnRad = 50;
			loopSize = 4;
			sizeMaxFunnelParticles = 600;
		} else {
			sizeMaxFunnelParticles = 600;
		}

		boolean tornadoV2 = true;


		if (!tornadoV2) {
			if (isTornadoFormingOrGreater() || (attrib_waterSpout)) {
				if (this.manager.getWorld().getGameTime() % (delay + ConfigStorm.Storm_ParticleSpawnDelay) == 0) {
					for (int i = 0; i < loopSize; i++) {


						if (listParticlesFunnel.size() >= sizeMaxFunnelParticles) {
							listParticlesFunnel.get(0).remove();
							listParticlesFunnel.remove(0);
						}

						if (listParticlesFunnel.size() < sizeMaxFunnelParticles) {


							Vec3 tryPos = new Vec3(pos.x + (rand.nextDouble() * spawnRad) - (rand.nextDouble() * spawnRad), pos.y, pos.z + (rand.nextDouble() * spawnRad) - (rand.nextDouble() * spawnRad));


							if (tryPos.distanceTo(playerAdjPos) < maxSpawnDistFromPlayer) {
								EntityRotFX particle;
								if (!isFirenado) {
									if (WeatherUtil.isAprilFoolsDay() && !Weather.isLoveTropicsInstalled()) {
										particle = spawnFogParticle(tryPos.x, posBaseFormationPos.y, tryPos.z, 1, ParticleRegistry.potato);
									} else {
										particle = spawnFogParticle(tryPos.x, posBaseFormationPos.y, tryPos.z, 1);
									}
								} else {
									particle = spawnFogParticle(tryPos.x, posBaseFormationPos.y, tryPos.z, 1, ParticleRegistry.cloud256_fire);

								}


								particle.setMaxAge(150 + ((levelCurIntensityStage - 1) * 100) + rand.nextInt(100));

								float baseBright = 0.3F;
								float randFloat = (rand.nextFloat() * 0.6F);

								particle.rotationYaw = rand.nextInt(360);

								float finalBright = Math.min(1F, baseBright + randFloat);


								if (levelCurIntensityStage == STATE_HIGHWIND) {
									particle.setScale(150 * 0.15F);
									particle.setColor(finalBright - 0.2F, finalBright - 0.2F, finalBright);
								} else {
									particle.setScale(250 * 0.15F);
									particle.setColor(finalBright, finalBright, finalBright);
								}

								if (isFirenado) {
									particle.setColor(1F, 1F, 1F);
									particle.setScale(particle.getScale() * 0.7F);
								}


								listParticlesFunnel.add(particle);


							}
						} else {

						}
					}
				}
			}

			for (int i = 0; i < listParticlesFunnel.size(); i++) {
				EntityRotFX ent = listParticlesFunnel.get(i);

				if (!ent.isAlive()) {
					listParticlesFunnel.remove(ent);
				} else if (ent.getPosY() > pos.y) {
					ent.remove();
					listParticlesFunnel.remove(ent);

				} else {
					double var16 = this.pos.x - ent.getPosX();
					double var18 = this.pos.z - ent.getPosZ();
					ent.rotationYaw = -(float) (Math.atan2(var18, var16) * 180.0D / Math.PI) - 90.0F;
					ent.rotationYaw -= ent.getEntityId() % 90;
					ent.rotationPitch = 30F;


					if (levelCurIntensityStage == STATE_HIGHWIND) {
						int fadingDistStart = 30;
						if (ent.getPosY() > posGround.y + fadingDistStart) {
							float maxVal = ent.bCol;
							float fadeRate = 0.002F;
							ent.setColor(Math.min(maxVal, ent.rCol + fadeRate), Math.min(maxVal, ent.gCol + fadeRate), maxVal);
						}
					}

					spinEntity(ent);
				}
			}
		}

		if (isTornadoFormingOrGreater() || (attrib_waterSpout)) {
			if (manager.getWorld().getGameTime() % 5 == 0) {

			}

			for (int i = 0; i < listParticlesDebris.size(); i++) {
				EntityRotFX ent = listParticlesDebris.get(i);
				if (!ent.isAlive()) {

					listParticlesDebris.remove(ent);
				} else {


					double speedXZ = Math.sqrt(ent.getMotionX() * ent.getMotionX() + ent.getMotionY() * ent.getMotionY() + ent.getMotionZ() * ent.getMotionZ());
					if (speedXZ < 30) {
						Vec3 motion = spinObject(ent.getPos(), new Vec3(ent.getMotionX(), ent.getMotionY(), ent.getMotionZ()), false, 0.91F, 1F, ent instanceof ParticleCube, 0);

						float damp = 1F;
						damp = ent.getWindWeight() / 5F;
						motion = motion.multiply(damp, 1F, damp);

						ent.setMotionX(motion.x);
						ent.setMotionY(motion.y);
						ent.setMotionZ(motion.z);


					}
				}
			}
		}

		for (int i = 0; i < listParticlesCloud.size(); i++) {
			EntityRotFX ent = listParticlesCloud.get(i);
			if (!ent.isAlive()) {
				listParticlesCloud.remove(ent);
			} else {


				double curSpeed = Math.sqrt(ent.getMotionX() * ent.getMotionX() + ent.getMotionY() * ent.getMotionY() + ent.getMotionZ() * ent.getMotionZ());

				double curDist = ent.getDistance(getPosTop().x, ent.getPosY(), getPosTop().z);

				float dropDownRange = 15F;

		        float extraDropCalc = 0;
		        if (curDist < 200 && ent.getEntityId() % 20 < 5) {

		        	extraDropCalc = ((ent.getEntityId() % 20) * dropDownRange);
		        	if (isCycloneFormingOrGreater()) {
		        		extraDropCalc = ((ent.getEntityId() % 20) * dropDownRange * 5F);

		        	}
		        }


				if (isSpinning()) {
					double speed = spinSpeed + (rand.nextDouble() * 0.01D);
					double distt = size;


					double vecX = ent.getPosX() - getPosTop().x;
			        double vecZ = ent.getPosZ() - getPosTop().z;
			        float angle = (float)(Math.atan2(vecZ, vecX) * 180.0D / Math.PI);


			        angle += speed * 50D;


			        angle -= (ent.getEntityId() % 10) * 3D;


			        angle += rand.nextInt(10) - rand.nextInt(10);

			        if (curDist > distt) {

			        	angle += 40;

			        }


			        if (ent.getEntityId() % 20 < 5) {
			        	if (levelCurIntensityStage >= STATE_FORMING) {
			        		if (stormType == TYPE_WATER) {
			        			angle += 40 + ((ent.getEntityId() % 5) * 4);
			        			if (curDist > 150 + ((levelCurIntensityStage-levelStormIntensityFormingStartVal+1) * 30)) {
			        				angle += 10;
			        			}
			        		} else {
			        			angle += 30 + ((ent.getEntityId() % 5) * 4);
			        		}

			        	} else {

			        		if (curDist > 150) {
			        			angle += 50 + ((ent.getEntityId() % 5) * 4);
			        		}
			        	}

			        	double diffX = this.getPosTop().x - ent.getPosX();
		                double diffZ = this.getPosTop().z - ent.getPosZ();
		                ent.rotationYaw = (float)(Math.atan2(diffZ, diffX) * 180.0D / Math.PI) - 90.0F;
		                ent.rotationYaw = -(float)(Math.atan2(diffZ, diffX) * 180.0D / Math.PI) - 90.0F;

		                ent.rotationPitch = 20F - (ent.getEntityId() % 10);
			        }


			        if (curSpeed < speed * 20D) {
			        	ent.setMotionX(ent.getMotionX() + -Math.sin(Math.toRadians(angle)) * speed);
				        ent.setMotionZ(ent.getMotionZ() + Math.cos(Math.toRadians(angle)) * speed);
			        }
				} else {
					float cloudMoveAmp = 0.2F * (1 + layer);

					float speed = getAdjustedSpeed() * cloudMoveAmp;
					float angle = getAdjustedAngle();


					if ((manager.getWorld().getGameTime()+this.ID) % 40 == 0) {
						ent.avoidTerrainAngle = getAvoidAngleIfTerrainAtOrAheadOfPosition(angle, ent.getPos());
					}

					angle += ent.avoidTerrainAngle;

					if (ent.avoidTerrainAngle != 0) {


						speed *= 0.5D;
					}

					dropDownRange = 5;
			        if (ent.getEntityId() % 20 < 5) {
			        	extraDropCalc = ((ent.getEntityId() % 20) * dropDownRange);
			        }

					if (curSpeed < speed * 1D) {
			        	ent.setMotionX(ent.getMotionX() + -Math.sin(Math.toRadians(angle)) * speed);
				        ent.setMotionZ(ent.getMotionZ() + Math.cos(Math.toRadians(angle)) * speed);
			        }
				}

				if (Math.abs(ent.getPosY() - (getPosTop().y - extraDropCalc)) > 2F) {
			        if (ent.getPosY() < getPosTop().y - extraDropCalc) {
		        		ent.setMotionY(ent.getMotionY() + 0.1D);
		        	} else {
		        		ent.setMotionY(ent.getMotionY() - 0.1D);
		        	}
				}

				float dropDownSpeedMax = 0.15F;

				if (isCycloneFormingOrGreater()) {
					dropDownSpeedMax = 0.9F;
				}

		        if (ent.getMotionY() < -dropDownSpeedMax) {
		        	ent.setMotionY(-dropDownSpeedMax);
		        }

		        if (ent.getMotionY() > dropDownSpeedMax) {
		        	ent.setMotionY(dropDownSpeedMax);
		        }


			}


		}

		for (int i = 0; i < listParticlesGround.size(); i++) {
			EntityRotFX ent = listParticlesGround.get(i);

			double curDist = ent.getDistance(pos.x, ent.getPosY(), pos.z);

			if (!ent.isAlive()) {
				listParticlesGround.remove(ent);
			} else {
				double curSpeed = Math.sqrt(ent.getMotionX() * ent.getMotionX() + ent.getMotionY() * ent.getMotionY() + ent.getMotionZ() * ent.getMotionZ());

				double speed = Math.max(0.2F, 5F * spinSpeed) + (rand.nextDouble() * 0.01D);
				double distt = size;


				double vecX = ent.getPosX() - pos.x;
		        double vecZ = ent.getPosZ() - pos.z;
		        float angle = (float)(Math.atan2(vecZ, vecX) * 180.0D / Math.PI);

		        angle += 85;

		        int maxParticleSize = 60;

		        if (stormType == TYPE_WATER) {
		        	maxParticleSize = 150;
		        	speed /= 5D;
				}

		        ent.setScale((float) Math.min(maxParticleSize * 0.15F, curDist * 2F * 0.15F));

		        if (curDist < 20) {
		        	ent.remove();
		        }

	        	double var16 = this.pos.x - ent.getPosX();
                double var18 = this.pos.z - ent.getPosZ();


                if (curSpeed < speed * 20D) {
		        	ent.setMotionX(ent.getMotionX() + -Math.sin(Math.toRadians(angle)) * speed);
			        ent.setMotionZ(ent.getMotionZ() + Math.cos(Math.toRadians(angle)) * speed);
		        }
			}
		}


	}

	public float getAdjustedSpeed() {
		return manager.getWindManager().getWindSpeedForClouds();
	}

	public float getAdjustedAngle() {
		float angle = manager.getWindManager().getWindAngleForClouds();


		float angleAdjust = 45;
		float targetYaw = 0;


		if (levelTemperature > 0) {

			targetYaw = 180;
		} else {

			targetYaw = 0;
		}

		float bestMove = Mth.wrapDegrees(targetYaw - angle);

		if (Math.abs(bestMove) < 180) {
			if (bestMove > 0) angle -= angleAdjust;
			if (bestMove < 0) angle += angleAdjust;
		}

		if (manager.getWorld().getGameTime() % 40 == 0) {

		}

		return angle;
	}

	public float getAvoidAngleIfTerrainAtOrAheadOfPosition(float angle, Vec3 pos) {
		double scanDistMax = 120;
		for (int scanAngle = -20; scanAngle < 20; scanAngle += 10) {
			for (double scanDistRange = 20; scanDistRange < scanDistMax; scanDistRange += 10) {
				double scanX = pos.x + (-Math.sin(Math.toRadians(angle + scanAngle)) * scanDistRange);
				double scanZ = pos.z + (Math.cos(Math.toRadians(angle + scanAngle)) * scanDistRange);

				int height = WeatherUtilBlock.getPrecipitationHeightSafe(this.manager.getWorld(), CoroUtilBlock.blockPos(scanX, 0, scanZ)).getY();

				if (pos.y < height) {
					if (scanAngle <= 0) {
						return 90;
					} else {
						return -90;
					}
				}
			}
		}
		return 0;
	}

	public Player getRandomNearPlayer(Entity entityLineOfSightSource) {


		List<? extends Player> players = manager.getWorld().players();
		List<Player> playersNear = new ArrayList<>();
		for (Player player : players) {
			if (!player.isSpectator() && player.position().distanceTo(posGround) < 196 && player.hasLineOfSight(entityLineOfSightSource)) {
				playersNear.add(player);
			}
		}
		if (!playersNear.isEmpty()) {
			Collections.shuffle(playersNear);
			return playersNear.get(0);
		}
		return null;
	}

	public void spinEntityv2(Entity entity) {

		if (entity.getPersistentData().getBoolean("tornado_shoot")) {
			if (!entity.getPersistentData().contains("tornado_shoot_target")) {
				Player player = getRandomNearPlayer(entity);
				if (player != null) {
					entity.getPersistentData().putString("tornado_shoot_target", player.getUUID().toString());
				}
			} else {
				UUID uuid = UUID.fromString(entity.getPersistentData().getString("tornado_shoot_target"));
				Player player = manager.getWorld().getPlayerByUUID(uuid);
				if (player != null) {
					double vecx = player.position().x - entity.position().x;
					double vecy = player.position().y - entity.position().y;
					double vecz = player.position().z - entity.position().z;
					Vec3 vec = new Vec3(vecx, vecy, vecz);
					double speed = 2F;
					vec = vec.normalize().multiply(speed, speed, speed);
					entity.setDeltaMovement(vec.x, vec.y, vec.z);
					float rotationYaw = (float)(Mth.atan2(vecz, vecx) * 180.0D / Math.PI) - 90.0F;
					entity.setYRot(rotationYaw);

					if (player.position().distanceTo(entity.position()) < 3 && entity.isAlive()) {
						player.hurt(manager.getWorld().damageSources().cactus(), 1.5F);
						entity.kill();
					}

					if (entity.isInWater()) {
						entity.getPersistentData().putBoolean("tornado_shoot", false);
					}
				}
			}
		} else {
			float dampenY = 1F;
			float dampenXZ = 1F;
			float grabAdj = 0F;

			double entHeightFromBase = Math.max(0.1F, entity.getY() - posBaseFormationPos.y);

			if (entity instanceof Player) {
				if (((Player) entity).getMainHandItem().getItem().toString().contains("acid_repellent_umbrella") ||
						((Player) entity).getOffhandItem().getItem().toString().contains("acid_repellent_umbrella")) {
					if (entHeightFromBase > 80) {
						dampenY = 0.7F;
					}
					dampenXZ = 1.4F;
					grabAdj -= 20;
				}

				float weightAdj = WeatherUtilEntity.getWeightAdjFromEquipment(1F, (Player) entity);

				dampenY /= weightAdj;
				dampenXZ /= weightAdj;
			}


			entity.setDeltaMovement(spinObject(entity.position(), entity.getDeltaMovement(), entity instanceof Player, dampenXZ, dampenY, false, grabAdj));

			if (!ConfigTornado.Storm_Tornado_fallDamage || CoroUtilEntOrParticle.getMotionY(entity) > -0.8)
			{
				entity.fallDistance = 0F;
			}

			if (isFirenado) {
				Vec3 posEnt = entity.position();
				Vec3 posFunnel = getFunnelCenter(posEnt);
				double distXZ = entity.position().distanceTo(new Vec3(posFunnel.x, posEnt.y, posFunnel.z));
				if (entHeightFromBase > 5 && entHeightFromBase < 70 && distXZ < (entHeightFromBase * 1.3)) {
					entity.setSecondsOnFire(6);
				}
			}

			if (entHeightFromBase > 90) {
				if (Weather.isLoveTropicsInstalled()) {


					if (isSharknado() && entity instanceof Dolphin) {
						entity.getPersistentData().putBoolean("tornado_shoot", true);
					}
				} else {
					if (isSharknado() && entity instanceof Dolphin) {
						entity.getPersistentData().putBoolean("tornado_shoot", true);
					}
				}
			}
		}


	}

	public Vec3 getFunnelCenter(Vec3 position) {
		Vec3 posCenter = getPosTop();
		for (Layer layer : tornadoFunnelSimple.listLayers) {
			if (position.y - 1.5F < layer.getPos().y) {
				posCenter = layer.getPos();
				break;
			}
		}
		return posCenter;
	}

	public Vec3 spinObject(Vec3 position, Vec3 motion, boolean forPlayer, float dampenXZ, float dampenY, boolean forCube, float grabAdj) {
		Vec3 posCenter = getFunnelCenter(position);

		double vecx = posCenter.x - position.x;
		double vecz = posCenter.z - position.z;

		Vec3 vecXZ = new Vec3(posCenter.x, position.y, posCenter.z);

		double distXZMax = tornadoFunnelSimple.getConfig().getEntityPullDistXZ();
		double distXZMaxForYGrab = tornadoFunnelSimple.getConfig().getEntityPullDistXZForY();
		double distXZ = Math.min(position.distanceTo(vecXZ), distXZMax);
		double distXZForYGrab = Math.min(position.distanceTo(vecXZ), distXZMaxForYGrab);

		double grabAmp = (float)(levelCurIntensityStage - STATE_FORMING) / (float)(STATE_STAGE5 - STATE_FORMING);

		float angle = (float)(Mth.atan2(vecz, vecx) * 180.0D / Math.PI + 180F);


		angle += ConfigTornado.Storm_Tornado_extraGrabAngle;
		if (pet) angle += 50;

		if (forCube) {
			angle += 20;
		}

		angle += grabAdj;

		double entHeightFromBase = Math.max(0.1F, position.y - posBaseFormationPos.y);
		double heightMathMax = 50 * 2.5;
		heightMathMax = 50 * 3.5;

		if (baby) heightMathMax = 15;
		if (pet) heightMathMax = 4;
		if (playerControlled) heightMathMax = 40;

		double heightAmp = (heightMathMax - entHeightFromBase) / heightMathMax;


		if (!pet) {
			angle += (40 * heightAmp);
		}


		angle -= 40 * grabAmp;

		angle = (float) Math.toRadians(angle);
		double pullStrength = 0.2;
		double pullStrengthY = 0.2;
		pullStrengthY += 0.2 * grabAmp;
		double pullYAmp = 1D;

		if (levelCurIntensityStage == STATE_FORMING) {
			pullYAmp = levelCurStagesIntensity;
		}
		pullStrengthY *= pullYAmp;
		pullStrength *= pullYAmp;
		if (pet) pullStrength = 0.05;
		if (pet) pullStrengthY = 0.05;
		if (sharknado && forPlayer) pullStrength = 0.05;
		if (sharknado && forPlayer) pullStrengthY = 0.1;
		double pullY = pullStrengthY * (distXZMaxForYGrab - distXZForYGrab) / distXZMaxForYGrab;
		double pullXZ = pullStrength * (distXZMax - distXZ) / distXZMax;
		double xx = -Math.sin(angle) * pullXZ;
		double zz = Math.cos(angle) * pullXZ;
		double yy = pullY;

		if (!baby && motion.y > 0.5F) {
			yy = 0;
		}

		if (pet && motion.y > 0.15F) {
			yy = 0;
		}

		return new Vec3(motion.x + (xx * dampenXZ), motion.y + (yy * dampenY), motion.z + (zz * dampenXZ));
	}

	public void spinEntity(Object entity1) {

		StormObject entT = this;
		StormObject entity = this;
		WeatherEntityConfig conf = getWeatherEntityConfigForStorm();

		Random rand = new Random();


    	boolean forTornado = true;

    	Level world = CoroUtilEntOrParticle.getWorld(entity1);
    	long worldTime = world.getGameTime();

    	Entity ent = null;
    	if (entity1 instanceof Entity) {
    		ent = (Entity) entity1;
    	}


        double radius = 10D;
        double scale = conf.tornadoWidthScale;
        double d1 = entity.pos.x - CoroUtilEntOrParticle.getPosX(entity1);
        double d2 = entity.pos.z - CoroUtilEntOrParticle.getPosZ(entity1);

        if (conf.type == conf.TYPE_SPOUT) {
        	float range = 30F * (float) Math.sin((Math.toRadians(((worldTime * 0.5F) + (ID * 50)) % 360)));
        	float heightPercent = (float) (1F - ((CoroUtilEntOrParticle.getPosY(entity1) - posGround.y) / (pos.y - posGround.y)));
        	float posOffsetX = (float) Math.sin((Math.toRadians(heightPercent * 360F)));
        	float posOffsetZ = (float) -Math.cos((Math.toRadians(heightPercent * 360F)));


        	d1 += range*posOffsetX;
        	d2 += range*posOffsetZ;
        }

        float f = (float)((Math.atan2(d2, d1) * 180D) / Math.PI) - 90F;
        float f1;

        for (f1 = f; f1 < -180F; f1 += 360F) { }

        for (; f1 >= 180F; f1 -= 360F) { }

        double distY = entity.pos.y - CoroUtilEntOrParticle.getPosY(entity1);
        double distXZ = Math.sqrt(Math.abs(d1)) + Math.sqrt(Math.abs(d2));

        if (CoroUtilEntOrParticle.getPosY(entity1) - entity.pos.y < 0.0D)
        {
            distY = 1.0D;
        }
        else
        {
            distY = CoroUtilEntOrParticle.getPosY(entity1) - entity.pos.y;
        }

        if (distY > maxHeight)
        {
            distY = maxHeight;
        }


        float weight = WeatherUtilEntity.getWeight(entity1);
        double grab = (10D / weight) * ((Math.abs((maxHeight - distY)) / maxHeight));
        float pullY = 0.0F;


        if (rand.nextInt(5) != 0)
        {

        }

        if (distXZ > 5D)
        {
            grab = grab * (radius / distXZ);
        }


        pullY += (float)(conf.tornadoLiftRate / (weight / 2F));


        if (entity1 instanceof Player)
        {
            double adjPull = 0.2D / ((weight * ((distXZ + 1D) / radius)));


            pullY += adjPull;


            double adjGrab = (10D * (((float)(((double)0 + 1D) / 400D))));

            if (adjGrab > 50)
            {
                adjGrab = 50D;
            }

            if (adjGrab < -50)
            {
                adjGrab = -50D;
            }

            grab = grab - adjGrab;

            if (CoroUtilEntOrParticle.getMotionY(entity1) > -0.8)
            {

                ent.fallDistance = 0F;
            }


        }
        else if (entity1 instanceof LivingEntity)
        {
            double adjPull = 0.005D / ((weight * ((distXZ + 1D) / radius)));


            pullY += adjPull;


            int airTime = ent.getPersistentData().getInt("timeInAir");
            double adjGrab = (10D * (((float)(((double)(airTime) + 1D) / 400D))));

            if (adjGrab > 50)
            {
                adjGrab = 50D;
            }

            if (adjGrab < -50)
            {
                adjGrab = -50D;
            }

            grab = grab - adjGrab;

            if (ent.getDeltaMovement().y > -1.5)
            {
                ent.fallDistance = 0F;
            }

            if (ent.getDeltaMovement().y > 0.3F) ent.setDeltaMovement(ent.getDeltaMovement().x, 0.3F, ent.getDeltaMovement().z);

            if (forTornado) ent.setOnGround(false);


			if (!isFirenado) {
				ent.clearFire();
			}


        }


        grab += conf.relTornadoSize;

        double profileAngle = Math.max(1, (75D + grab - (10D * scale)));

        f1 = (float)((double)f1 + profileAngle);


        if (entT != null) {

        	if (entT.scale != 1F) f1 += 20 - (20 * entT.scale);
        }

        float f3 = (float)Math.cos(-f1 * 0.01745329F - (float)Math.PI);
        float f4 = (float)Math.sin(-f1 * 0.01745329F - (float)Math.PI);
        float f5 = conf.tornadoPullRate * 1;

        if (entT != null) {
        	if (entT.scale != 1F) f5 *= entT.scale * 1.2F;
        }

        if (entity1 instanceof LivingEntity)
        {

            f5 /= (WeatherUtilEntity.getWeight(entity1) * ((distXZ + 1D) / radius));
        }


        if (entity1 instanceof Player && conf.type != 0) {

        	if (ent.onGround()) {
        		f5 *= 10.5F;
        	} else {
        		f5 *= 5F;
        	}

        } else if (entity1 instanceof LivingEntity && conf.type != 0) {
        	f5 *= 1.5F;
        }

        if (conf.type == conf.TYPE_SPOUT && entity1 instanceof LivingEntity) {
        	f5 *= 0.3F;
        }

        float moveX = f3 * f5;
        float moveZ = f4 * f5;

        float str = 1F;


        str = strength;

        if (conf.type == conf.TYPE_SPOUT && entity1 instanceof LivingEntity) {
        	str *= 0.3F;
        }

        pullY *= str / 100F;

        if (entT != null) {
        	if (entT.scale != 1F) {
        		pullY *= entT.scale * 1.0F;
        		pullY += 0.002F;
        	}
        }


        if (entity1 instanceof Entity) {
	        long lastPullTime = ent.getPersistentData().getLong("lastPullTime");
	        if (lastPullTime == worldTime) {

	        	pullY = 0;
	        }
	        ent.getPersistentData().putLong("lastPullTime", worldTime);
        }

        setVel(entity1, -moveX, pullY, moveZ);
	}

	public void setVel(Object entity, float f, float f1, float f2)
    {
        CoroUtilEntOrParticle.setMotionX(entity, CoroUtilEntOrParticle.getMotionX(entity) + f);
		CoroUtilEntOrParticle.setMotionY(entity, CoroUtilEntOrParticle.getMotionY(entity) + f1);
		CoroUtilEntOrParticle.setMotionZ(entity, CoroUtilEntOrParticle.getMotionZ(entity) + f2);
    }

	@OnlyIn(Dist.CLIENT)
	public EntityRotFX spawnFogParticle(double x, double y, double z, int parRenderOrder) {
		return spawnFogParticle(x, y, z, parRenderOrder, ParticleRegistry.cloud256);
	}

	@OnlyIn(Dist.CLIENT)
    public EntityRotFX spawnFogParticle(double x, double y, double z, int parRenderOrder, TextureAtlasSprite tex) {
    	double speed = 0D;
		Random rand = new Random();
    	EntityRotFX entityfx = particleBehaviorFog.spawnNewParticleIconFX(Minecraft.getInstance().level, tex, x, y, z, (rand.nextDouble() - rand.nextDouble()) * speed, 0.0D, (rand.nextDouble() - rand.nextDouble()) * speed, parRenderOrder);
		particleBehaviorFog.initParticle(entityfx);


		entityfx.setCanCollide(false);
    	entityfx.callUpdatePB = false;

		debugCloudTemperature = false;
    	boolean debug = debugCloudTemperature;

    	if (debug) {

    	} else {

    	}

    	if (levelCurIntensityStage == STATE_NORMAL) {
    		entityfx.setMaxAge(300 + rand.nextInt(100));
    	} else {
    		entityfx.setMaxAge((size/2) + rand.nextInt(100));
    	}


		if (entityfx.getEntityId() % 20 < 5 && isSpinning()) {
			entityfx.renderOrder = 1;

			entityfx.setMaxAge((size) + rand.nextInt(100));
		}

    	float randFloat = (rand.nextFloat() * 0.6F);
		float baseBright = 0.7F;
		if (levelCurIntensityStage > STATE_NORMAL) {
			baseBright = 0.2F;
		} else if (attrib_precipitation) {
			baseBright = 0.2F;
		} else if (manager.isVanillaRainActiveOnServer) {
			baseBright = 0.2F;
		} else {
			float adj = Math.min(1F, levelWater / levelWaterStartRaining) * 0.6F;
			baseBright -= adj;
		}


		float finalBright = Math.min(1F, baseBright+randFloat);


		entityfx.setColor(finalBright, finalBright, finalBright);


		if (debug) {
			if (levelTemperature < 0) {
				entityfx.setColor(0, 0, finalBright);
			} else if (levelTemperature > 0) {
				entityfx.setColor(finalBright, 0, 0);
			}
		}

		entityfx.setTicksFadeInMax(20);
		entityfx.setTicksFadeOutMax(20);
		entityfx.setTicksFadeOutMaxOnDeath(20);

		entityfx.setUseDynamicWindSpeed(false);

		if (ConfigParticle.Particle_effect_rate != 0) {
			entityfx.spawnAsWeatherEffect();

			particleBehaviorFog.particles.add(entityfx);
		}
		return entityfx;
    }

	@Override
	public void cleanup() {
		if (tornadoHelper != null) {

			if (this.levelCurIntensityStage >= STATE_FORMING) {
				featherFallAllNearbyPlayers();
			}
			tornadoHelper.cleanup();
		}
		super.cleanup();
		tornadoHelper = null;
		if (tornadoFunnelSimple != null) {
			tornadoFunnelSimple.cleanup();
		}
	}

	@OnlyIn(Dist.CLIENT)
	@Override
	public void cleanupClient() {
		super.cleanupClient();
		listParticlesCloud.clear();
		listParticlesFunnel.clear();
		if (particleBehaviorFog != null && particleBehaviorFog.particles != null) particleBehaviorFog.particles.clear();
		particleBehaviorFog = null;
		tornadoHelper = null;
		if (tornadoFunnelSimple != null) {
			tornadoFunnelSimple.cleanupClient();
		}
	}

	public static float getTemperatureMCToWeatherSys(float parOrigVal) {
		if (parOrigVal > 0) {
			return 1;
		} else {
 			return -1;
		}
	}

	public void addWeatherEffectLightning(LightningBoltWeatherNew parEnt) {
		manager.getWorld().addFreshEntity(parEnt);
	}

	@Override
	public int getUpdateRateForNetwork() {
		if (levelCurIntensityStage >= StormObject.STATE_HIGHWIND) {
			return 10;
		} else {
			return super.getUpdateRateForNetwork();
		}
	}

	public Vec3 getPosTop() {
		if (isTornadoFormingOrGreater()) {
			return tornadoFunnelSimple.getPosTop();
		}
		return pos;
	}

	public void setupStorm(Entity entity) {
		if (entity != null) {
			this.spawnerUUID = entity.getUUID().toString();
			initPositions(entity.position());
		}

		this.layer = 0;
		this.naturallySpawned = false;
		this.levelTemperature = 0.1F;
		this.levelWater = this.levelWaterStartRaining * 2;
		this.attrib_precipitation = true;
		this.levelCurIntensityStage = this.STATE_STAGE1;
		this.alwaysProgresses = false;

		this.initFirstTime();


		this.levelStormIntensityMax = this.levelCurIntensityStage;
	}

	public void initPositions(Vec3 pos) {
		this.pos = pos;

		currentTopYBlock = calculateTopYBlock();
		posGround = new Vec3(pos.x, currentTopYBlock, pos.z);
		posBaseFormationPos = calculateBaseFormationPos();
	}

	public int calculateTopYBlock() {
		Level world = manager.getWorld();
		int calculatedYPos = WeatherUtilBlock.getPrecipitationHeightSafe(world, new BlockPos(Mth.floor(pos.x), 0, Mth.floor(pos.z)), Heightmap.Types.WORLD_SURFACE_WG).getY();


		boolean filterOutLogs = true;
		if (filterOutLogs && world.hasChunkAt(CoroUtilBlock.blockPos(pos))) {
			int y = calculatedYPos-1;
			BlockState state = world.getBlockState(CoroUtilBlock.blockPos(pos.x, y, pos.z));

			int iter = 0;
			while ((state.is(BlockTags.LOGS) || state.is(BlockTags.LEAVES) || state.is(BlockTags.CAVE_VINES) || state.is(BlockTags.REPLACEABLE_BY_TREES) || state.is(Blocks.COCOA)  || state.is(Blocks.BAMBOO) || state.isAir()) && y > world.getMinBuildHeight()) {
				y--;
				state = world.getBlockState(CoroUtilBlock.blockPos(pos.x, y, pos.z));

				iter++;
			}
			if (y > world.getMinBuildHeight()) calculatedYPos = y;

		}


		if (Weather.isLoveTropicsInstalled()) {
			while (world.getBlockState(CoroUtilBlock.blockPos(pos.x, calculatedYPos, pos.z)).getBlock() instanceof LiquidBlock && calculatedYPos < world.getMaxBuildHeight()) {
				calculatedYPos++;
			}
		}

		if (isBaby()) {
			Player player = getPlayer();
			if (player != null) {
				calculatedYPos = (int) player.position().y;
			}
		}


		if (isPet()) {
			Player entP = getPlayer();
			if (entP != null) {
				if (entP.position().distanceTo(posGround) < 10) {
					if (calculatedYPos + 4 > entP.position().y) {
						int y = (int) (entP.position().y + 2);
						while (y > entP.position().y - 3) {
							BlockPos blockPos = CoroUtilBlock.blockPos(pos.x, y, pos.z);
							BlockPos blockPosUp = CoroUtilBlock.blockPos(pos.x, y+1, pos.z);
							BlockState state = world.getBlockState(blockPos);
							BlockState stateUp = world.getBlockState(blockPosUp);
							if ((!state.isAir() && state.getCollisionShape(world, blockPosUp) != Shapes.empty()) && (stateUp.isAir() || stateUp.getCollisionShape(world, blockPosUp) == Shapes.empty())) {
								calculatedYPos = y+1;
								break;
							}
							y--;
						}
					}
				}
			}
		}
		return calculatedYPos;
	}

	public void setupTornadoAwayFromPlayersAimAtPlayers() {
		List<? extends Player> players = manager.getWorld().players();
		List<Player> playersNear = new ArrayList<>();
		double xAdd = 0;
		double yAdd = 0;
		double zAdd = 0;
		Vec3 vecAdd = new Vec3(0, 0, 0);
		for (Player player : players) {
			vecAdd = vecAdd.add(player.position());
		}
		vecAdd = new Vec3(vecAdd.x / players.size(), vecAdd.y / players.size(), vecAdd.z / players.size());
		initPositions(vecAdd.add(150, 0, 150));
		aimAtCoords(vecAdd);
	}

	public void setupPlayerControlledTornado(Entity entity) {
		this.playerControlled = true;
	}

	public Player getPlayer() {
		if (spawnerUUID.equals("")) return null;
		return manager.getWorld().getPlayerByUUID(UUID.fromString(spawnerUUID));
	}

	public boolean isPlayerControlled() {
		return playerControlled;
	}

	public void setPlayerControlled(boolean playerControlled) {
		this.playerControlled = playerControlled;
	}

	public int getPlayerControlledTimeLeft() {
		return playerControlledTimeLeft;
	}

	public void setPlayerControlledTimeLeft(int playerControlledTimeLeft) {
		this.playerControlledTimeLeft = playerControlledTimeLeft;
	}

	public boolean isBaby() {
		return baby;
	}

	public void setBaby(boolean baby) {
		this.baby = baby;
	}

	public boolean isPet() {
		return pet;
	}

	public void setPet(boolean pet) {
		this.pet = pet;
	}

	public boolean isPetGrabsItems() {
		return petGrabsItems;
	}

	public void setPetGrabsItems(boolean petGrabsItems) {
		this.petGrabsItems = petGrabsItems;
	}

	public boolean isSharknado() {
		return sharknado;
	}

	public void setSharknado(boolean sharknado) {
		this.sharknado = sharknado;
	}

	public void setAndUpdateTornado() {
		if (tornadoFunnelSimple == null) {
			setupTornado();
		}


		tornadoFunnelSimple.pos = new Vec3(posGround.x, posBaseFormationPos.y, posGround.z);
	}

	public TornadoFunnelSimple getTornadoFunnelSimple() {
		return tornadoFunnelSimple;
	}

	public void setTornadoFunnelSimple(TornadoFunnelSimple tornadoFunnelSimple) {
		this.tornadoFunnelSimple = tornadoFunnelSimple;
	}

	public int getAge() {
		return age;
	}

	public void setAge(int age) {
		this.age = age;
	}

	public int getAgeSinceTornadoTouchdown() {
		return ageSinceTornadoTouchdown;
	}

	public void setAgeSinceTornadoTouchdown(int ageSinceTornadoTouchdown) {
		this.ageSinceTornadoTouchdown = ageSinceTornadoTouchdown;
	}

	public boolean isBeingDeflectedCached() {
		return isBeingDeflectedCached;
	}

	public void setBeingDeflectedCached(boolean beingDeflectedCached) {
		isBeingDeflectedCached = beingDeflectedCached;
	}
}
