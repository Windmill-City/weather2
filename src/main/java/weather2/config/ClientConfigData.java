package weather2.config;

import net.minecraft.nbt.CompoundTag;


public class ClientConfigData {

    public boolean overcastMode = false;
    public boolean Storm_Tornado_grabPlayer = true;
    public boolean Storm_Tornado_grabPlayersOnly = false;
    public boolean Storm_Tornado_grabMobs = true;
    public boolean Storm_Tornado_grabAnimals = true;
    public boolean Storm_Tornado_grabItems = false;
    public boolean Storm_Tornado_grabVillagers = true;
    public boolean Aesthetic_Only_Mode = false;


    public void readNBT(CompoundTag nbt) {
        overcastMode = nbt.getBoolean("overcastMode");
        Storm_Tornado_grabPlayer = nbt.getBoolean("Storm_Tornado_grabPlayer");
        Storm_Tornado_grabPlayersOnly = nbt.getBoolean("Storm_Tornado_grabPlayersOnly");
        Storm_Tornado_grabMobs = nbt.getBoolean("Storm_Tornado_grabMobs");
        Storm_Tornado_grabAnimals = nbt.getBoolean("Storm_Tornado_grabAnimals");
        Storm_Tornado_grabVillagers = nbt.getBoolean("Storm_Tornado_grabVillagers");
        Storm_Tornado_grabItems = nbt.getBoolean("Storm_Tornado_grabItems");
        Aesthetic_Only_Mode = nbt.getBoolean("Aesthetic_Only_Mode");
    }


    public static void writeNBT(CompoundTag data) {

        data.putBoolean("overcastMode", ConfigMisc.overcastMode);
        data.putBoolean("Storm_Tornado_grabPlayer", ConfigTornado.Storm_Tornado_grabPlayer);
        data.putBoolean("Storm_Tornado_grabPlayersOnly", ConfigTornado.Storm_Tornado_grabPlayersOnly);
        data.putBoolean("Storm_Tornado_grabMobs", ConfigTornado.Storm_Tornado_grabMobs);
        data.putBoolean("Storm_Tornado_grabAnimals", ConfigTornado.Storm_Tornado_grabAnimals);
        data.putBoolean("Storm_Tornado_grabVillagers", ConfigTornado.Storm_Tornado_grabVillagers);
        data.putBoolean("Storm_Tornado_grabItems", ConfigTornado.Storm_Tornado_grabItems);
        data.putBoolean("Aesthetic_Only_Mode", ConfigMisc.Aesthetic_Only_Mode);


    }

}
