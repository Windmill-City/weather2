package extendedrenderer.particle.entity;

import org.joml.Quaternionf;
import org.joml.Vector3f;

import com.corosus.coroutil.util.CoroUtilBlock;
import com.corosus.coroutil.util.CoroUtilParticle;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import weather2.ClientTickHandler;
import weather2.weathersystem.WeatherManagerClient;
import weather2.weathersystem.wind.WindManager;

public class ParticleTexExtraRender extends ParticleTexFX {

	private int severityOfRainRate = 2;

	private int extraParticlesBaseAmount = 5;

	public boolean noExtraParticles = false;

	private float extraRandomSecondaryYawRotation = 360;

	public ParticleTexExtraRender(ClientLevel worldIn, double posXIn, double posYIn,
			double posZIn, double mX, double mY, double mZ,
			TextureAtlasSprite par8Item) {
		super(worldIn, posXIn, posYIn, posZIn, mX, mY, mZ, par8Item);

	}

	public int getSeverityOfRainRate() {
		return severityOfRainRate;
	}

	public void setSeverityOfRainRate(int severityOfRainRate) {
		this.severityOfRainRate = severityOfRainRate;
	}

	public int getExtraParticlesBaseAmount() {
		return extraParticlesBaseAmount;
	}

	public void setExtraParticlesBaseAmount(int extraParticlesBaseAmount) {
		this.extraParticlesBaseAmount = extraParticlesBaseAmount;
	}

	@Override
	public void tickExtraRotations() {

		WeatherManagerClient weatherMan = ClientTickHandler.weatherManager;
		if (weatherMan == null)
			return;
		WindManager windMan = weatherMan.getWindManager();
		if (windMan == null)
			return;

		if (isSlantParticleToWind()) {
			double speed = xd * xd + zd * zd;
			rotationYaw = -(float) Math.toDegrees(Math.atan2(zd, xd)) - 90;
			rotationPitch = Math.min(45, (float) (speed * 120));
			rotationPitch += (this.getEntityId() % 10) - 5;
		}

		windMan.applyWindForceNew(this, 1F / 2F, 0.5F);
	}

	@Override
	public void render(VertexConsumer buffer, Camera renderInfo, float partialTicks) {

		Vec3 Vector3d = renderInfo.getPosition();
		Quaternionf quaternion;
		if (this.facePlayer || (this.rotationPitch == 0 && this.rotationYaw == 0)) {
			quaternion = renderInfo.rotation();
		} else {

			quaternion = new Quaternionf(0, 0, 0, 1);
			quaternion.mul(Axis.YP.rotationDegrees(this.rotationYaw));
			quaternion.mul(Axis.XP.rotationDegrees(this.rotationPitch));
			if (extraRandomSecondaryYawRotation > 0) {
				quaternion.mul(Axis.YP.rotationDegrees(getEntityId() % extraRandomSecondaryYawRotation));
			}
		}

		float posX = (float) (Mth.lerp((double) partialTicks, this.xo, this.x) - Vector3d.x());
		float posY = (float) (Mth.lerp((double) partialTicks, this.yo, this.y) - Vector3d.y());
		float posZ = (float) (Mth.lerp((double) partialTicks, this.zo, this.z) - Vector3d.z());

		float f = this.getU0();
		float f1 = this.getU1();
		float f2 = this.getV0();
		float f3 = this.getV1();

		float part = 16F / 3F;
		float offset = 0;
		float posBottom = (float) (this.y - 10D);

		float height = level
				.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, CoroUtilBlock.blockPos(this.x, this.y, this.z))
				.getY();

		if (posBottom < height) {
			float diff = height - posBottom;
			offset = diff;
			if (offset > part)
				offset = part;
		}

		int renderAmount = 0;
		if (noExtraParticles) {
			renderAmount = 1;
		} else {

			renderAmount = Math.min(1 + extraParticlesBaseAmount, CoroUtilParticle.maxRainDrops);
		}

		try {
			for (int ii = 0; ii < renderAmount; ii++) {
				double xx = 0;
				double zz = 0;
				double yy = 0;
				if (ii != 0) {
					xx = CoroUtilParticle.rainPositions[ii].x;
					zz = CoroUtilParticle.rainPositions[ii].z;

					yy = CoroUtilParticle.rainPositions[ii].y;
				}

				if (this.isDontRenderUnderTopmostBlock()) {
					int height2 = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING,
							CoroUtilBlock.blockPos(this.x + xx, this.y, this.z + zz)).getY();
					if (this.y + yy < height2)
						continue;
				}

				int i = this.getLightColor(partialTicks);
				if (i > 0) {
					setLastNonZeroBrightness(i);
				} else {
					i = getLastNonZeroBrightness();
				}

				Vector3f[] avector3f = new Vector3f[] { new Vector3f(-1.0F, -1.0F, 0.0F),
						new Vector3f(-1.0F, 1.0F, 0.0F), new Vector3f(1.0F, 1.0F, 0.0F),
						new Vector3f(1.0F, -1.0F, 0.0F) };
				float scale = this.getQuadSize(partialTicks);

				for (int v = 0; v < 4; ++v) {
					Vector3f vector3f = avector3f[v];
					vector3f.rotate(quaternion);
					vector3f.mul(scale);
					vector3f.add(posX, posY, posZ);
				}

				buffer.vertex(xx + avector3f[0].x(), yy + avector3f[0].y(), zz + avector3f[0].z()).uv(f1, f3)
						.color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(i).endVertex();
				buffer.vertex(xx + avector3f[1].x(), yy + avector3f[1].y(), zz + avector3f[1].z()).uv(f1, f2)
						.color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(i).endVertex();
				buffer.vertex(xx + avector3f[2].x(), yy + avector3f[2].y(), zz + avector3f[2].z()).uv(f, f2)
						.color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(i).endVertex();
				buffer.vertex(xx + avector3f[3].x(), yy + avector3f[3].y(), zz + avector3f[3].z()).uv(f, f3)
						.color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(i).endVertex();
			}
		} catch (Throwable ex) {
			ex.printStackTrace();
		}

	}

}
