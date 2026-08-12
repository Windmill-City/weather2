package extendedrenderer.particle.entity;

import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.ArrayUtils;

import com.corosus.coroutil.util.CoroUtilColor;
import com.corosus.coroutil.util.CoroUtilMisc;

import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class ParticleTexLeafColor extends ParticleTexFX {

	private static BlockColors colors;

	private static ConcurrentHashMap<BlockState, int[]> colorCache = new ConcurrentHashMap<>();

	public float rotationYawMomentum;
	public float rotationPitchMomentum;

	public ParticleTexLeafColor(ClientLevel worldIn, double posXIn, double posYIn,
			double posZIn, double mX, double mY, double mZ,
			TextureAtlasSprite par8Item) {
		super(worldIn, posXIn, posYIn, posZIn, mX, mY, mZ, par8Item);

		if (colors == null) {
			colors = Minecraft.getInstance().getBlockColors();

		}

		BlockPos pos = new BlockPos((int) Math.floor(posXIn), (int) Math.floor(posYIn), (int) Math.floor(posZIn));
		BlockState state = worldIn.getBlockState(pos);

		int multiplier = ParticleTexLeafColor.colors.getColor(state, this.level, pos, 0);

		int[] colors = colorCache.get(state);
		if (colors == null) {

			colors = CoroUtilColor.getColors(state);

			if (colors.length == 0) {

				if (!hasColor(state) || (multiplier & 0xFFFFFF) == 0xFFFFFF) {
					multiplier = 5811761;
				}

				colors = new int[] { 0x00FF00 };
			}

			if (colors.length > 1) {
				while (colors[colors.length - 1] == colors[colors.length - 2]) {
					colors = ArrayUtils.remove(colors, colors.length - 1);
				}
			}
			colorCache.put(state, colors);
		}

		int randMax = 1 << (colors.length - 1);
		int choice = 32 - Integer.numberOfLeadingZeros(CoroUtilMisc.random.nextInt(randMax));
		int color = colors[choice];

		float mr = ((multiplier >>> 16) & 0xFF) / 255f;
		float mg = ((multiplier >>> 8) & 0xFF) / 255f;
		float mb = (multiplier & 0xFF) / 255f;

		this.rCol *= (float) (color >> 16 & 255) / 255.0F * mr;
		this.gCol *= (float) (color >> 8 & 255) / 255.0F * mg;
		this.bCol *= (float) (color & 255) / 255.0F * mb;
	}

	@Override
	public void tick() {
		super.tick();

		if (isCollidedVerticallyDownwards && random.nextInt(10) == 0) {
			double speed = Math.sqrt(this.xd * this.xd + this.zd * this.zd);
			if (speed > 0.07) {
				this.yd = 0.02D + random.nextDouble() * 0.03D;
				this.xd *= 0.6D;
				this.zd *= 0.6D;

				rotationYawMomentum = 30;
				rotationPitchMomentum = 30;
			}
		}

		if (rotationYawMomentum > 0) {

			this.rotationYaw += rotationYawMomentum;

			rotationYawMomentum -= 1.5F;

			if (rotationYawMomentum < 0) {
				rotationYawMomentum = 0;
			}
		} else {
			rotationYawMomentum += random.nextDouble() * 30;
		}

		if (rotationPitchMomentum > 0) {

			this.rotationPitch += rotationPitchMomentum;

			rotationPitchMomentum -= 1.5F;

			if (rotationPitchMomentum < 0) {
				rotationPitchMomentum = 0;
			}
		} else {
			rotationPitchMomentum += random.nextDouble() * 30;
		}
	}

	private final boolean hasColor(BlockState state) {
		return false;
	}

}
