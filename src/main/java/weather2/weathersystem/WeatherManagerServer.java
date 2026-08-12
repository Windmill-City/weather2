package weather2.weathersystem;

import com.corosus.coroutil.util.*;
import com.google.common.collect.Lists;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.PacketDistributor;
import weather2.*;
import weather2.config.*;
import weather2.datatypes.StormState;
import weather2.util.CachedNBTTagCompound;
import weather2.util.WeatherUtilBlock;
import weather2.weathersystem.storm.StormObject;
import weather2.weathersystem.storm.WeatherObject;
import weather2.weathersystem.storm.WeatherObjectParticleStorm;
import weather2.weathersystem.wind.WindManager;

import javax.annotation.Nullable;
import java.util.*;

public class WeatherManagerServer extends WeatherManager {
	private final ServerLevel world;

	public WeatherManagerServer(ServerLevel world) {
		super(world.dimension());
		this.world = world;
	}

	@Override
	public Level getWorld() {
		return world;
	}

	@Override
	public void tick() {
		super.tick();

		StormState snowstorm = ServerWeatherProxy.getSnowstormForEverywhere(world);
		if (snowstorm != null) {
			tickStormBlockBuildup(snowstorm, Blocks.SNOW);
		}
		StormState sandstorm = ServerWeatherProxy.getSandstormForEverywhere(world);
		if (sandstorm != null) {
			tickStormBlockBuildup(sandstorm, WeatherBlocks.BLOCK_SAND_LAYER.get());
		}


		tickWeatherCoverage();

		if (world != null) {
			WindManager windMan = getWindManager();

			getStormObjects().stream()
					.filter(wo -> world.getGameTime() % wo.getUpdateRateForNetwork() == 0)
					.forEach(this::syncStormUpdate);


			if (world.getGameTime() % 60 == 0) {
				syncWindUpdate(windMan);
			}


			int rate = 20;
			if (world.getGameTime() % rate == 0) {


				for (int i = 0; i < getStormObjects().size(); i++) {
					WeatherObject so = getStormObjects().get(i);
					Player closestPlayer = world.getNearestPlayer(so.posGround.x, so.posGround.y, so.posGround.z, ConfigMisc.Misc_simBoxRadiusCutoff, EntitySelector.ENTITY_STILL_ALIVE);

					if (so instanceof StormObject && ((StormObject) so).isPet()) continue;

					if (ConfigMisc.Winter_Wonderland && so instanceof WeatherObjectParticleStorm && ((WeatherObjectParticleStorm) so).getType() == WeatherObjectParticleStorm.StormType.SNOWSTORM) continue;


					if (closestPlayer == null || ConfigMisc.Aesthetic_Only_Mode) {
						so.ticksSinceNoNearPlayer += rate;

						if (so.ticksSinceNoNearPlayer > 20 * 30 || ConfigMisc.Aesthetic_Only_Mode) {
							if (world.getPlayers(LivingEntity::isAlive).size() == 0) {
								Weather.dbg("removing distant storm: " + so.ID + ", running without players");
							} else {
								Weather.dbg("removing distant storm: " + so.ID);
							}

							removeStormObject(so.ID);
							syncStormRemove(so);
						}
					} else {
						so.ticksSinceNoNearPlayer = 0;
					}
				}


				Random rand = new Random();


				boolean spawnClouds = true;
				if (spawnClouds && !Weather.isLoveTropicsInstalled() && !ConfigMisc.Aesthetic_Only_Mode && WeatherUtilConfig.shouldTickClouds(world.dimension().location().toString())) {
					for (int i = 0; i < world.players().size(); i++) {
						Player entP = world.players().get(i);


						if (getStormObjects().size() < ConfigStorm.Storm_MaxPerPlayerPerLayer * world.players().size()) {
							if (rand.nextInt(5) == 0) {

								trySpawnStormCloudNearPlayerForLayer(entP, 0);

							}
						}


					}
				}
			}


			if (!Weather.isLoveTropicsInstalled() && WeatherUtilConfig.listDimensionsStorms.contains(world.dimension().location().toString()) && world.getGameTime() % 200 == 0 && windMan.isHighWindEventActive()) {
				if (!ConfigSand.Storm_NoSandstorms) {
					tryParticleStorm(world, WeatherObjectParticleStorm.StormType.SANDSTORM);
				}
				if (!ConfigSnow.Storm_NoSnowstorms && (!ConfigMisc.Aesthetic_Only_Mode || ConfigMisc.Winter_Wonderland)) {
					tryParticleStorm(world, WeatherObjectParticleStorm.StormType.SNOWSTORM);
				}
			}
		}
	}

	public Optional<BlockPos> findWeatherDeflector(BlockPos pos, int range) {
		double closestDist = Float.MAX_VALUE;
		BlockPos closestPos = null;
		for (Map.Entry<Long, BlockPos> entrySet : getLookupWeatherBlockDamageDeflector().entrySet()) {
			double dist = pos.distSqr(entrySet.getValue());
			if (dist < range * range) {
				if (dist < closestDist) {
					closestDist = dist;
					closestPos = entrySet.getValue();
				}
			}
		}
		if (closestPos != null) {
			return Optional.of(closestPos);
		} else {
			return Optional.empty();
		}

	}

	public void tickStormBlockBuildup(StormState stormState, Block block) {
		Level world = getWorld();
		WindManager windMan = getWindManager();
		Random rand = CoroUtilMisc.random();

		float angle = windMan.getWindAngle(null);

		int rate = 1;
		int maxStack;
		if (stormState != null) {
			rate = stormState.getBuildupTickRate();
			maxStack = stormState.getMaxStackable();
		} else {
			maxStack = 4;
		}
		if (world.getGameTime() % rate == 0) {
			List<ChunkHolder> list = Lists.newArrayList(((ServerLevel)world).getChunkSource().chunkMap.getChunks());
			Collections.shuffle(list);
			list.forEach((p_241099_7_) -> {
				Optional<LevelChunk> optional = p_241099_7_.getTickingChunkFuture().getNow(ChunkHolder.UNLOADED_LEVEL_CHUNK).left();
				if (optional.isPresent()) {
					for (int i = 0; i < 10; i++) {
						BlockPos blockPos = new BlockPos((optional.get().getPos().x * 16) + rand.nextInt(16), 0, (optional.get().getPos().z * 16) + rand.nextInt(16));
						int y = WeatherUtilBlock.getPrecipitationHeightSafe(world, blockPos).getY();
						Vec3 pos = new Vec3(blockPos.getX(), y, blockPos.getZ());
						WeatherUtilBlock.fillAgainstWallSmoothly(world, pos, angle, 15, 2, block, maxStack);
					}
				}
			});
		}
	}

	public void syncStormRemove(WeatherObject parStorm) {

		CompoundTag data = new CompoundTag();
		data.putString("packetCommand", "WeatherData");
		data.putString("command", "syncStormRemove");
		parStorm.nbtSyncForClient();
		data.put("data", parStorm.getNbtCache().getNewNBT());


		data.getCompound("data").putBoolean("removed", true);

		WeatherNetworking.HANDLER.send(PacketDistributor.DIMENSION.with(() -> getWorld().dimension()), new PacketNBTFromServer(data));
	}

	public void syncWindUpdate(WindManager parManager) {

		CompoundTag data = new CompoundTag();
		data.putString("packetCommand", "WeatherData");
		data.putString("command", "syncWindUpdate");
		data.put("data", parManager.nbtSyncForClient());
		WeatherNetworking.HANDLER.send(PacketDistributor.DIMENSION.with(() -> getWorld().dimension()), new PacketNBTFromServer(data));
	}

	public void tickWeatherCoverage() {
		ServerLevel world = (ServerLevel) this.getWorld();
		if (world != null) {
			if (!ConfigMisc.overcastMode) {
				if (ConfigMisc.lockServerWeatherMode != -1) {
					world.serverLevelData.setRaining(ConfigMisc.lockServerWeatherMode == 1);
					world.serverLevelData.setThundering(ConfigMisc.lockServerWeatherMode == 1);
				}
			}

			if (ConfigStorm.preventServerThunderstorms && !ConfigMisc.Aesthetic_Only_Mode) {
				world.serverLevelData.setThundering(false);
			}


			if (world.getGameTime() % 40 == 0) {
				isVanillaRainActiveOnServer = world.isRaining();
				float minRain = 0;
				float maxRain = 0;
				if (world.isThundering()) {
					minRain = 0.3F;
					maxRain = 1F;
				} else if (world.isRaining()) {
					minRain = 0.1F;
					maxRain = 0.7F;
				}
				vanillaRainAmountOnServer = Math.max(minRain, Math.min(maxRain, vanillaRainAmountOnServer + (CoroUtilMisc.random().nextFloat() - CoroUtilMisc.random().nextFloat()) * 0.02F));


				syncWeatherVanilla();
			}


			if (world.getGameTime() % 400 == 0) {

			}


			if (world.getGameTime() % 200 == 0) {
				Random rand = new Random();
				cloudIntensity += (float)((rand.nextDouble() * ConfigMisc.Cloud_Coverage_Random_Change_Amount) - (rand.nextDouble() * ConfigMisc.Cloud_Coverage_Random_Change_Amount));
				if (ConfigMisc.overcastMode && world.isRaining()) {
					cloudIntensity = 1;
				} else {
					if (cloudIntensity < ConfigMisc.Cloud_Coverage_Min_Percent / 100F) {
						cloudIntensity = (float) ConfigMisc.Cloud_Coverage_Min_Percent / 100F;
					} else if (cloudIntensity > ConfigMisc.Cloud_Coverage_Max_Percent / 100F) {
						cloudIntensity = (float) ConfigMisc.Cloud_Coverage_Max_Percent / 100F;
					}
				}
				if (world.getGameTime() % 2000 == 0) {

				}


			}


		}
	}

	public void tryParticleStorm(Level level, WeatherObjectParticleStorm.StormType type) {

		int stormOdds = ConfigSand.Sandstorm_OddsTo1;
		int timeBetweenTicks = ConfigSand.Sandstorm_TimeBetweenInTicks;
		long lastStormTime = this.lastSandstormFormed;
		boolean useGlobalServerRate = ConfigSand.Sandstorm_UseGlobalServerRate;
		String stormString;

		if (type == WeatherObjectParticleStorm.StormType.SNOWSTORM) {
			stormOdds = ConfigSnow.Snowstorm_OddsTo1;
			stormString = "lastSnowstormTime";
			useGlobalServerRate = ConfigSnow.Snowstorm_UseGlobalServerRate;
			lastStormTime = this.lastSnowstormFormed;
		} else {
			stormString = "lastSandstormTime";
		}

		if (stormOdds <= 0 || CoroUtilMisc.random().nextInt(stormOdds) == 0) {
			if (useGlobalServerRate) {
				if (lastStormTime == 0 || lastStormTime + timeBetweenTicks < level.getGameTime()) {
					if (world.players().size() > 0) {
						Player entP = world.players().get(CoroUtilMisc.random().nextInt(world.players().size()));
						boolean stormMade = trySpawnParticleStormNearPos(level, entP.position(), type);
						if (stormMade) {
							if (type == WeatherObjectParticleStorm.StormType.SANDSTORM) {
								lastSandstormFormed = world.getGameTime();
							} else if (type == WeatherObjectParticleStorm.StormType.SNOWSTORM) {
								lastSnowstormFormed = world.getGameTime();
							}
						}
					}
				}
			} else {
				world.players().stream().forEach(player -> {
					CompoundTag playerNBT = player.getPersistentData();
					long lastStormTimePlayer = playerNBT.getLong(stormString);
					if (lastStormTimePlayer == 0 || lastStormTimePlayer + timeBetweenTicks < level.getGameTime()) {
						boolean stormMade = trySpawnParticleStormNearPos(player.level(), player.position(), type);
						if (stormMade) {
							playerNBT.putLong(stormString, world.getGameTime());
						}
					}
				});
			}
		}
	}

	public boolean trySpawnParticleStormNearPos(Level world, Vec3 posIn, WeatherObjectParticleStorm.StormType type) {
		return trySpawnParticleStormNearPos(world, posIn, type, false);
	}

	public boolean trySpawnParticleStormNearPos(Level world, Vec3 posIn, WeatherObjectParticleStorm.StormType type, boolean force) {


		int searchRadius = 64;

		double angle = getWindManager().getWindAngleForClouds();

		double dirX = -Math.sin(Math.toRadians(angle));
		double dirZ = Math.cos(Math.toRadians(angle));
		double vecX = dirX * searchRadius/2 * -1;
		double vecZ = dirZ * searchRadius/2 * -1;

		Random rand = new Random();

		BlockPos foundPos = null;

		int findTriesMax = 30;
		for (int i = 0; i < findTriesMax; i++) {

			int x = Mth.floor(posIn.x + vecX + rand.nextInt(searchRadius * 2) - searchRadius);
			int z = Mth.floor(posIn.z + vecZ + rand.nextInt(searchRadius * 2) - searchRadius);

			BlockPos pos = WeatherUtilBlock.getPrecipitationHeightSafe(world, new BlockPos(x, 0, z));

			if (!world.isLoaded(pos)) continue;

			Biome biomeIn = world.getBiome(pos).get();

			if (force || WeatherObjectParticleStorm.canSpawnHere(world, pos, type, true)) {

				foundPos = pos;


				double dirXLeft = -Math.sin(Math.toRadians(angle-90));
				double dirZLeft = Math.cos(Math.toRadians(angle-90));
				double dirXRight = -Math.sin(Math.toRadians(angle+90));
				double dirZRight = Math.cos(Math.toRadians(angle+90));

				double distLeftRight = 20;
				BlockPos posLeft = WeatherUtilBlock.getPrecipitationHeightSafe(world, CoroUtilBlock.blockPos(foundPos.getX() + (dirXLeft * distLeftRight), 0, foundPos.getZ() + (dirZLeft * distLeftRight)));
				if (!world.isLoaded(posLeft)) continue;

				if (!WeatherObjectParticleStorm.canSpawnHere(world, posLeft, type, false)) continue;


				BlockPos posRight = WeatherUtilBlock.getPrecipitationHeightSafe(world, CoroUtilBlock.blockPos(foundPos.getX() + (dirXRight * distLeftRight), 0, foundPos.getZ() + (dirZRight * distLeftRight)));
				if (!world.isLoaded(posRight)) continue;

				if (!WeatherObjectParticleStorm.canSpawnHere(world, posRight, type, false)) continue;


				BlockPos posFind = new BlockPos(foundPos);
				BlockPos posFindLastGoodUpwind = new BlockPos(foundPos);
				BlockPos posFindLastGoodDownwind = new BlockPos(foundPos);
				double tickDist = 10;


				while (world.isLoaded(posFind) && WeatherObjectParticleStorm.canSpawnHere(world, posFind, type, true)) {

					posFindLastGoodUpwind = new BlockPos(posFind);


					int xx = Mth.floor(posFind.getX() + (dirX * -1D * tickDist));
					int zz = Mth.floor(posFind.getZ() + (dirZ * -1D * tickDist));

					posFind = WeatherUtilBlock.getPrecipitationHeightSafe(world, new BlockPos(xx, 0, zz));
				}


				posFind = new BlockPos(foundPos);


				while (world.isLoaded(posFind) && WeatherObjectParticleStorm.canSpawnHere(world, posFind, type, true)) {

					posFindLastGoodDownwind = new BlockPos(posFind);


					int xx = Mth.floor(posFind.getX() + (dirX * 1D * tickDist));
					int zz = Mth.floor(posFind.getZ() + (dirZ * 1D * tickDist));

					posFind = WeatherUtilBlock.getPrecipitationHeightSafe(world, new BlockPos(xx, 0, zz));
				}

				int minDistanceOfDesertStretchNeeded = 20;
				double dist = posFindLastGoodUpwind.distSqr(posFindLastGoodDownwind);

				if (force || dist >= minDistanceOfDesertStretchNeeded * minDistanceOfDesertStretchNeeded) {

					if (ConfigMisc.Winter_Wonderland && type == WeatherObjectParticleStorm.StormType.SNOWSTORM) {

						spawnParticleStorm(CoroUtilBlock.blockPos(posIn), type);
					} else {
						spawnParticleStorm(posFindLastGoodUpwind, type);
					}


					Weather.dbg("found decent spot and stretch for particle storm, stretch: " + dist + ", type: " + type);
					return true;
				}
			}
		}

		Weather.dbg("couldnt spawn particle storm");
		return false;
	}

	public void spawnParticleStorm(BlockPos pos, WeatherObjectParticleStorm.StormType type) {
		WeatherObjectParticleStorm storm = new WeatherObjectParticleStorm(this);

		storm.setType(type);
		storm.initFirstTime();
		BlockPos posSpawn = new BlockPos(WeatherUtilBlock.getPrecipitationHeightSafe(world, pos)).above();
		storm.initStormSpawn(new Vec3(posSpawn.getX(), posSpawn.getY(), posSpawn.getZ()));
		addStormObject(storm);
		syncStormNew(storm);
	}

	public void trySpawnStormCloudNearPlayerForLayer(Player entP, int layer) {


		Random rand = new Random();

		int tryCountMax = 10;
		int tryCountCur = 0;
		int spawnX = -1;
		int spawnZ = -1;
		Vec3 tryPos = null;
		StormObject soClose = null;
		Player playerClose = null;

		int closestToPlayer = 128;


		float windOffsetDist = Math.min(256, ConfigMisc.Misc_simBoxRadiusCutoff / 4 * 3);
		double angle = getWindManager().getWindAngleForClouds();
		double vecX = -Math.sin(Math.toRadians(angle)) * windOffsetDist;
		double vecZ = Math.cos(Math.toRadians(angle)) * windOffsetDist;

		while (tryCountCur++ == 0 || (tryCountCur < tryCountMax && (soClose != null || playerClose != null))) {
			spawnX = (int) (entP.getX() - vecX + rand.nextInt(ConfigMisc.Misc_simBoxRadiusSpawn) - rand.nextInt(ConfigMisc.Misc_simBoxRadiusSpawn));
			spawnZ = (int) (entP.getZ() - vecZ + rand.nextInt(ConfigMisc.Misc_simBoxRadiusSpawn) - rand.nextInt(ConfigMisc.Misc_simBoxRadiusSpawn));
			tryPos = new Vec3(spawnX, StormObject.layers.get(layer), spawnZ);
			soClose = getClosestStormAny(tryPos, ConfigMisc.Cloud_Formation_MinDistBetweenSpawned);
			playerClose = entP.level().getNearestPlayer(spawnX, 50, spawnZ, closestToPlayer, false);
		}

		if (soClose == null) {


			StormObject so = new StormObject(this);
			so.pos = tryPos;
			so.layer = layer;
			so.initFirstTime();

			if (layer != 0) {
				so.canBeDeadly = false;
			}
			so.spawnerUUID = entP.getStringUUID();
			if (rand.nextFloat() >= cloudIntensity) {
				so.setCloudlessStorm(true);
			}
			addStormObject(so);
			syncStormNew(so);
		} else {

		}
	}

	public void playerJoinedWorldSyncFull(ServerPlayer entP) {
		Weather.dbg("Weather2: playerJoinedWorldSyncFull for dim: " + dimension);
		Level world = getWorld();
		if (world != null) {
			Weather.dbg("Weather2: playerJoinedWorldSyncFull, sending " + getStormObjects().size() + " weather objects to: " + entP.getName() + ", dim: " + dimension);

			for (int i = 0; i < getStormObjects().size(); i++) {
				syncStormNew(getStormObjects().get(i), entP);
			}
		}
	}


	public void syncBlockParticleNew(BlockPos pos, BlockState state, WeatherObject owner) {
		CompoundTag data = new CompoundTag();
		data.putString("packetCommand", "WeatherData");
		data.putString("command", "syncBlockParticleNew");
		CompoundTag nbt = new CompoundTag();
		nbt.putInt("posX", pos.getX());
		nbt.putInt("posY", pos.getY());
		nbt.putInt("posZ", pos.getZ());
		nbt.put("blockstate", NbtUtils.writeBlockState(state));
		nbt.putLong("ownerID", owner.ID);
		data.put("data", nbt);

		WeatherNetworking.HANDLER.send(PacketDistributor.DIMENSION.with(() -> getWorld().dimension()), new PacketNBTFromServer(data));
	}

	public void syncStormNew(WeatherObject parStorm) {
		syncStormNew(parStorm, null);
	}

	public void syncStormNew(WeatherObject parStorm, @Nullable ServerPlayer entP) {
		CompoundTag data = new CompoundTag();
		data.putString("packetCommand", "WeatherData");
		data.putString("command", "syncStormNew");

		CachedNBTTagCompound cache = parStorm.getNbtCache();
		cache.setUpdateForced(true);
		parStorm.nbtSyncForClient();
		cache.setUpdateForced(false);
		data.put("data", cache.getNewNBT());

		if (entP == null) {
			WeatherNetworking.HANDLER.send(PacketDistributor.DIMENSION.with(() -> getWorld().dimension()), new PacketNBTFromServer(data));
		} else {
			WeatherNetworking.HANDLER.sendTo(new PacketNBTFromServer(data), entP.connection.connection, NetworkDirection.PLAY_TO_CLIENT);
		}
	}

	public void syncStormUpdate(WeatherObject parStorm) {

		CompoundTag data = new CompoundTag();
		data.putString("packetCommand", "WeatherData");
		data.putString("command", "syncStormUpdate");
		parStorm.getNbtCache().setNewNBT(new CompoundTag());
		parStorm.nbtSyncForClient();
		data.put("data", parStorm.getNbtCache().getNewNBT());
		boolean testNetworkData = false;
		if (testNetworkData) {
			System.out.println("sending to client: " + parStorm.getNbtCache().getNewNBT().getAllKeys().size());
			if (parStorm instanceof StormObject) {
				System.out.println("Real: " + ((StormObject) parStorm).levelCurIntensityStage);
				if (parStorm.getNbtCache().getNewNBT().contains("levelCurIntensityStage")) {
					System.out.println(" vs " + parStorm.getNbtCache().getNewNBT().getInt("levelCurIntensityStage"));
				} else {
					System.out.println("no key!");
				}
			}

			Iterator iterator = parStorm.getNbtCache().getNewNBT().getAllKeys().iterator();
			String keys = "";
			while (iterator.hasNext()) {
				keys = keys.concat((String) iterator.next() + "; ");
			}
			System.out.println("sending    " + keys);
		}
		WeatherNetworking.HANDLER.send(PacketDistributor.DIMENSION.with(() -> getWorld().dimension()), new PacketNBTFromServer(data));
	}

	public void syncWeatherVanilla() {

		CompoundTag data = new CompoundTag();
		data.putString("packetCommand", "WeatherData");
		data.putString("command", "syncWeatherUpdate");
		data.putBoolean("isVanillaRainActiveOnServer", isVanillaRainActiveOnServer);
		data.putFloat("vanillaRainAmountOnServer", vanillaRainAmountOnServer);
		WeatherNetworking.HANDLER.send(PacketDistributor.DIMENSION.with(() -> getWorld().dimension()), new PacketNBTFromServer(data));
	}

	public void removeWeatherObjectAndSync(WeatherObject parStorm) {

		if (parStorm == null) {
			return;
		}
		if (getWorld().players().size() == 0) {
			Weather.dbg("removing distant storm: " + parStorm.ID + ", running without players");
		} else {
			Weather.dbg("removing distant storm: " + parStorm.ID);
		}
		removeStormObject(parStorm.ID);
		syncStormRemove(parStorm);
	}

	public void clearAllStorms() {
		Iterator<WeatherObject> it = getStormObjects().iterator();
		while (it.hasNext()) {
			WeatherObject so = it.next();

			so.remove();
			syncStormRemove(so);

		}
		getStormObjects().clear();
		lookupStormObjectsByID.clear();
	}


	public float getBiomeBasedStormSpawnChanceInArea(BlockPos posCenter) {

		int scanResolution = 64;
		float samples = 0;
		float allTemperaturesAdded = 0;


		for (int x = -ConfigMisc.Misc_simBoxRadiusSpawn; x <= ConfigMisc.Misc_simBoxRadiusSpawn; x += scanResolution) {
			for (int z = -ConfigMisc.Misc_simBoxRadiusSpawn; z <= ConfigMisc.Misc_simBoxRadiusSpawn; z += scanResolution) {
				BlockPos pos = new BlockPos(posCenter.getX() + x, posCenter.getY(), posCenter.getZ() + z);
				if (getWorld().isLoaded(pos)) {
					pos = WeatherUtilBlock.getPrecipitationHeightSafe(getWorld(), pos);
					Biome bgb = getWorld().getBiome(pos).get();
					allTemperaturesAdded += StormObject.getTemperatureMCToWeatherSys(CoroUtilCompatibility.getAdjustedTemperature(getWorld(), bgb, pos));
					samples++;
				}
			}
		}

		CULog.dbg("samples: " + samples);
		CULog.dbg("allTemperaturesAdded: " + allTemperaturesAdded);

		float chance = 1 - (Math.abs(allTemperaturesAdded) / samples);
		return chance;
	}
}
