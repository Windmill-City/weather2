package weather2;

import com.corosus.coroutil.util.CULog;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import weather2.config.ConfigMisc;
import weather2.config.ConfigStorm;
import weather2.weathersystem.storm.StormObject;


public final class ClientWeatherHelper {
	private static ClientWeatherHelper instance;

	private float curPrecipStr = 0F;
	private float curPrecipStrTarget = 0F;

	private float curOvercastStr = 0F;
	private float curOvercastStrTarget = 0F;

	private ClientWeatherHelper() {
	}

	public static ClientWeatherHelper get() {
		if (instance == null) {
			instance = new ClientWeatherHelper();
		}
		return instance;
	}

	public void reset() {
		instance.curPrecipStr = 0F;
		instance.curPrecipStrTarget = 0F;

		instance.curOvercastStr = 0F;
		instance.curOvercastStrTarget = 0F;
	}

	public void tick() {
		tickRainRates();
	}

	public float getPrecipitationStrength(Player entP) {
		return getPrecipitationStrength(entP, false);
	}


	public float getPrecipitationStrength(Player entP, boolean forOvercast) {

		if (entP == null) return 0;
		double maxStormDist = 512 / 4 * 3;
		Vec3 plPos = new Vec3(entP.getX(), StormObject.static_YPos_layer0, entP.getZ());
		StormObject storm;

		ClientTickHandler.getClientWeather();

		storm = ClientTickHandler.weatherManager.getClosestStorm(plPos, maxStormDist, StormObject.STATE_FORMING, -1, true);

		boolean closeEnough = false;
		double stormDist = 9999;
		float tempAdj = 1F;

		float sizeToUse = 0;

		float overcastModeMinPrecip = 0.23F;


		overcastModeMinPrecip = ClientTickHandler.weatherManager.vanillaRainAmountOnServer;


		if (storm != null) {

			sizeToUse = storm.size;

			if (forOvercast) {
				sizeToUse *= 1F;
			}

			stormDist = storm.pos.distanceTo(plPos);

			if (sizeToUse > stormDist) {
				closeEnough = true;
			}
		}

		if (closeEnough) {

			double stormIntensity = (sizeToUse - stormDist) / sizeToUse;


			tempAdj = 1F;


			if (storm.levelCurIntensityStage == StormObject.STATE_NORMAL) {
				if (stormIntensity > 0.3) stormIntensity = 0.3;
			}

			if (ConfigStorm.Storm_NoRainVisual) {
				stormIntensity = 0;
			}


			if (forOvercast) {
				if (stormIntensity < overcastModeMinPrecip) {
					stormIntensity = overcastModeMinPrecip;
				}
			}
			if (forOvercast) {
				curOvercastStrTarget = (float) stormIntensity;
			} else {
				curPrecipStrTarget = (float) stormIntensity;
			}
		} else {
			if (!ClientTickHandler.clientConfigData.overcastMode) {
				if (forOvercast) {
					curOvercastStrTarget = 0;
				} else {
					curPrecipStrTarget = 0;
				}
			} else {
				if (ClientTickHandler.weatherManager.isVanillaRainActiveOnServer) {
					if (forOvercast) {
						curOvercastStrTarget = overcastModeMinPrecip;
					} else {
						curPrecipStrTarget = overcastModeMinPrecip;
					}
				} else {
					if (forOvercast) {
						curOvercastStrTarget = 0;
					} else {
						curPrecipStrTarget = 0;
					}
				}
			}
		}

		if (forOvercast) {
			if (curOvercastStr < 0.002 && curOvercastStr > -0.002F) {
				return 0;
			} else {
				return curOvercastStr * tempAdj;
			}
		} else {
			if (curPrecipStr < 0.002 && curPrecipStr > -0.002F) {
				return 0;
			} else {
				return curPrecipStr * tempAdj;
			}
		}
	}

	public void controlVisuals(boolean precipitating) {
		Minecraft mc = Minecraft.getInstance();
		ClientTickHandler.getClientWeather();
		ClientWeatherProxy weather = ClientWeatherProxy.get();
		float rainAmount = weather.getVanillaRainAmount();
		float visualDarknessAmplifier = 0.5F;

		visualDarknessAmplifier = 1F;

		if (!ConfigMisc.Aesthetic_Only_Mode) {
			if (precipitating) {
				mc.level.getLevelData().setRaining(rainAmount > 0);
				mc.level.setRainLevel(rainAmount * visualDarknessAmplifier);
				mc.level.setThunderLevel(rainAmount * visualDarknessAmplifier);

			} else {

				if (!ClientTickHandler.clientConfigData.overcastMode) {
					mc.level.getLevelData().setRaining(false);
					mc.level.setRainLevel(0);
					mc.level.setThunderLevel(0);
				} else {
					if (ClientTickHandler.weatherManager.isVanillaRainActiveOnServer) {
						mc.level.getLevelData().setRaining(true);
						mc.level.setRainLevel(rainAmount * visualDarknessAmplifier);
						mc.level.setThunderLevel(rainAmount * visualDarknessAmplifier);
					} else {

					}
				}
			}
		}


	}

	public void tickRainRates() {

		float rateChange = 0.0015F;

		if (curOvercastStr > curOvercastStrTarget) {
			curOvercastStr -= rateChange;
		} else if (curOvercastStr < curOvercastStrTarget) {
			curOvercastStr += rateChange;
		}

		if (curPrecipStr > curPrecipStrTarget) {
			curPrecipStr -= rateChange;
		} else if (curPrecipStr < curPrecipStrTarget) {
			curPrecipStr += rateChange;
		}
	}
}
