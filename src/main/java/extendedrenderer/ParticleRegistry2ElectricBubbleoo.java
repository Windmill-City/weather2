package extendedrenderer;

import net.minecraft.client.particle.WaterDropParticle;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.RegistryObject;
import weather2.Weather;


@Mod.EventBusSubscriber(modid = Weather.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ParticleRegistry2ElectricBubbleoo {


    public static final RegistryObject<SimpleParticleType> ACIDRAIN_SPLASH = Weather.R.particle("acidrain_splash", () -> new SimpleParticleType(false));


    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public static void factories(RegisterParticleProvidersEvent event) {

        event.registerSpriteSet(new SimpleParticleType(false), WaterDropParticle.Provider::new);

    }

    public static void bootstrap() {}
}
