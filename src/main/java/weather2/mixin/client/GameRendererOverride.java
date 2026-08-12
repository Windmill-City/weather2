package weather2.mixin.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(GameRenderer.class)
public abstract class GameRendererOverride {


    /**
     * Overrides the far render distance so weather effects render beyond the normal view distance.
     * @author Corosus
     * @reason Weather2 needs a larger depth far plane so storms and particles are visible at range.
     */
    @Overwrite
    public float getDepthFar() {

        return Minecraft.getInstance().gameRenderer.getRenderDistance() * 4F;
    }
}