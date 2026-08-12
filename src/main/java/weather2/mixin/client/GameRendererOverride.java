package weather2.mixin.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(GameRenderer.class)
public abstract class GameRendererOverride {


    @Overwrite
    public float getDepthFar() {

        return Minecraft.getInstance().gameRenderer.getRenderDistance() * 4F;
    }
}