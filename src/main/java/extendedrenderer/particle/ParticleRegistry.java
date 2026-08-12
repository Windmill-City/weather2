package extendedrenderer.particle;

import extendedrenderer.ExtendedRenderer;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.atlas.sources.SingleFile;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.common.data.SpriteSourceProvider;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import weather2.Weather;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ParticleRegistry extends SpriteSourceProvider {

	public static TextureAtlasSprite squareGrey;
	public static TextureAtlasSprite smoke;

	public static TextureAtlasSprite cloud;
	public static TextureAtlasSprite cloud256;
	public static TextureAtlasSprite cloud256_fire;
	public static TextureAtlasSprite cloud256_test;

	public static TextureAtlasSprite groundSplash;

	public static TextureAtlasSprite downfall3;


	public static TextureAtlasSprite chicken;
	public static TextureAtlasSprite potato;
	public static TextureAtlasSprite leaf;
	public static TextureAtlasSprite rain;
	public static TextureAtlasSprite rain_white;


	public static TextureAtlasSprite snow;
	public static TextureAtlasSprite snow2;


	public static TextureAtlasSprite tumbleweed;
	public static TextureAtlasSprite debris_1;
	public static TextureAtlasSprite debris_2;
	public static TextureAtlasSprite debris_3;
	public static TextureAtlasSprite test_texture;
	public static TextureAtlasSprite white_square;
	public static List<TextureAtlasSprite> listFish = new ArrayList<>();

	public static TextureAtlasSprite grass;
	public static TextureAtlasSprite hail;
	public static TextureAtlasSprite cloudNew;
	public static TextureAtlasSprite cloud_square;
	public static TextureAtlasSprite square16;
	public static TextureAtlasSprite square64;

	public ParticleRegistry(PackOutput output, ExistingFileHelper fileHelper)
	{
		super(output, fileHelper, ExtendedRenderer.modid);
	}

	@Override
	protected void addSources()
	{


		addSprite(new ResourceLocation(ExtendedRenderer.modid + ":particles/white"));


		addSprite(new ResourceLocation(ExtendedRenderer.modid + ":particles/cloud256"));
		addSprite(new ResourceLocation(ExtendedRenderer.modid + ":particles/cloud256_fire"));


		addSprite(new ResourceLocation(ExtendedRenderer.modid + ":particles/cloud256_6"));


		addSprite(new ResourceLocation(ExtendedRenderer.modid + ":particles/downfall3"));

		if (!Weather.isLoveTropicsInstalled()) {
			addSprite(new ResourceLocation(ExtendedRenderer.modid + ":particles/chicken"));
			addSprite(new ResourceLocation(ExtendedRenderer.modid + ":particles/potato"));
		}
		addSprite(new ResourceLocation(ExtendedRenderer.modid + ":particles/leaf"));


		addSprite(new ResourceLocation(ExtendedRenderer.modid + ":particles/rain_white"));


		addSprite(new ResourceLocation(ExtendedRenderer.modid + ":particles/snow"));
		addSprite(new ResourceLocation(ExtendedRenderer.modid + ":particles/snow2"));


		addSprite(new ResourceLocation(ExtendedRenderer.modid + ":particles/tumbleweed"));
		addSprite(new ResourceLocation(ExtendedRenderer.modid + ":particles/debris_1"));
		addSprite(new ResourceLocation(ExtendedRenderer.modid + ":particles/debris_2"));
		addSprite(new ResourceLocation(ExtendedRenderer.modid + ":particles/debris_3"));


		addSprite(new ResourceLocation(ExtendedRenderer.modid + ":particles/hail"));


	}

	public void addSprite(ResourceLocation res) {
		atlas(SpriteSourceProvider.PARTICLES_ATLAS).addSource(new SingleFile(res, Optional.empty()));
	}

	@SubscribeEvent
	@SuppressWarnings("deprecation")
	public static void getRegisteredParticles(TextureStitchEvent.Post event) {

		if (!event.getAtlas().location().equals(TextureAtlas.LOCATION_PARTICLES)) {
			return;
		}

		squareGrey = event.getAtlas().getSprite(new ResourceLocation(ExtendedRenderer.modid + ":particles/white"));


		cloud256 = event.getAtlas().getSprite(new ResourceLocation(ExtendedRenderer.modid + ":particles/cloud256"));
		cloud256_fire = event.getAtlas().getSprite(new ResourceLocation(ExtendedRenderer.modid + ":particles/cloud256_fire"));


		groundSplash = event.getAtlas().getSprite(new ResourceLocation(ExtendedRenderer.modid + ":particles/cloud256_6"));


		downfall3 = event.getAtlas().getSprite(new ResourceLocation(ExtendedRenderer.modid + ":particles/downfall3"));

		if (!Weather.isLoveTropicsInstalled()) {
			chicken = event.getAtlas().getSprite(new ResourceLocation(ExtendedRenderer.modid + ":particles/chicken"));
			potato = event.getAtlas().getSprite(new ResourceLocation(ExtendedRenderer.modid + ":particles/potato"));
		}
		leaf = event.getAtlas().getSprite(new ResourceLocation(ExtendedRenderer.modid + ":particles/leaf"));


		rain_white = event.getAtlas().getSprite(new ResourceLocation(ExtendedRenderer.modid + ":particles/rain_white"));


		snow = event.getAtlas().getSprite(new ResourceLocation(ExtendedRenderer.modid + ":particles/snow"));
		snow2 = event.getAtlas().getSprite(new ResourceLocation(ExtendedRenderer.modid + ":particles/snow2"));


		tumbleweed = event.getAtlas().getSprite(new ResourceLocation(ExtendedRenderer.modid + ":particles/tumbleweed"));
		debris_1 = event.getAtlas().getSprite(new ResourceLocation(ExtendedRenderer.modid + ":particles/debris_1"));
		debris_2 = event.getAtlas().getSprite(new ResourceLocation(ExtendedRenderer.modid + ":particles/debris_2"));
		debris_3 = event.getAtlas().getSprite(new ResourceLocation(ExtendedRenderer.modid + ":particles/debris_3"));


		hail = event.getAtlas().getSprite(new ResourceLocation(ExtendedRenderer.modid + ":particles/hail"));


	}


}
