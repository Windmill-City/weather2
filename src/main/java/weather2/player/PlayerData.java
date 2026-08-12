package weather2.player;

import java.util.HashMap;

import net.minecraft.nbt.CompoundTag;

public class PlayerData {

	public static HashMap<String, CompoundTag> playerNBT = new HashMap<>();

	public static CompoundTag getPlayerNBT(String username) {
		if (!playerNBT.containsKey(username)) {


			playerNBT.put(username, new CompoundTag());
		}
		return playerNBT.get(username);
	}


}
