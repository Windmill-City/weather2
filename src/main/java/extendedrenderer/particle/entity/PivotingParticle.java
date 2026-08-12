package extendedrenderer.particle.entity;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;


public class PivotingParticle extends ParticleTexFX {

    private Vec3 pivot = new Vec3(0, 0, 0);
    private Vec3 pivotPrev = new Vec3(0, 0, 0);

    private Vec3 pivotRot = new Vec3(0, 0, 0);
    private Vec3 pivotRotPrev = new Vec3(0, 0, 0);

    public PivotingParticle(ClientLevel worldIn, double posXIn, double posYIn, double posZIn, double mX, double mY, double mZ, TextureAtlasSprite par8Item) {
        super(worldIn, posXIn, posYIn, posZIn, mX, mY, mZ, par8Item);
    }

    @Override
    public void tick() {
        super.tick();
    }


    @Override
    public Vec3 getPivotedPosition(float partialTicks) {
        Vec3 pivotLerped = pivotPrev.lerp(pivot, partialTicks);
        Vec3 pivotRotLerped = pivotRotPrev.lerp(pivotRot, partialTicks);
        float x = (float) (-Math.sin(Math.toRadians(pivotRotLerped.y)) * pivotLerped.y);
        float z = (float) (Math.cos(Math.toRadians(pivotRotLerped.y)) * pivotLerped.y);
        return new Vec3(x, 0, z);
    }

    public Vec3 getPivot() {
        return pivot;
    }

    public void setPivot(Vec3 pivot) {
        this.pivot = pivot;
    }

    public Vec3 getPivotPrev() {
        return pivotPrev;
    }

    public void setPivotPrev(Vec3 pivotPrev) {
        this.pivotPrev = pivotPrev;
    }

    public Vec3 getPivotRot() {
        return pivotRot;
    }

    public void setPivotRot(Vec3 pivotRot) {
        this.pivotRot = pivotRot;
    }

    public Vec3 getPivotRotPrev() {
        return pivotRotPrev;
    }

    public void setPivotRotPrev(Vec3 pivotRotPrev) {
        this.pivotRotPrev = pivotRotPrev;
    }

    @Override
    public AABB getBoundingBoxForRender(float partialTicks) {
        return getBoundingBox().move(getPivotedPosition(partialTicks));
    }

    @Override
    public void render(VertexConsumer buffer, Camera renderInfo, float partialTicks) {
        super.render(buffer, renderInfo, partialTicks);
    }
}
