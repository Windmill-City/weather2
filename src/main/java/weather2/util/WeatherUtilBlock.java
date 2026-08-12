package weather2.util;

import com.corosus.coroutil.util.CoroUtilBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import weather2.WeatherBlocks;
import weather2.block.SandLayerBlock;


public class WeatherUtilBlock {


	public static int layerableHeightPropMax = 8;

	public static void fillAgainstWallSmoothly(Level world, Vec3 posSource, float directionYaw, float scanDistance, float fillRadius, Block blockLayerable, int maxBlockStackingAllowed) {
		fillAgainstWallSmoothly(world, posSource, directionYaw, scanDistance, fillRadius, blockLayerable, 4, maxBlockStackingAllowed);
	}

	public static void fillAgainstWallSmoothly(Level world, Vec3 posSource, float directionYaw, float scanDistance, float fillRadius, Block blockLayerable, int heightDiff, int maxBlockStackingAllowed) {


		BlockState stateTest = world.getBlockState(CoroUtilBlock.blockPos(posSource));
		if (stateTest.getBlock() == blockLayerable) {
			int heightTest = getHeightForAnyBlock(stateTest);
			if (heightTest < 8) {

			}
		}

		BlockPos posSourcei = CoroUtilBlock.blockPos(posSource);

		int y = posSourcei.getY();
		float tickStep = 0.75F;


		Vec3 posLastNonWall = new Vec3(posSource.x, posSource.y, posSource.z);
		Vec3 posWall = null;

		BlockPos lastScannedPosXZ = null;


		int previousBlockHeight = 0;


		for (float i = 0; i < scanDistance; i += tickStep) {
			double vecX = (-Math.sin(Math.toRadians(directionYaw)) * (i));
			double vecZ = (Math.cos(Math.toRadians(directionYaw)) * (i));

			int x = Mth.floor(posSource.x + vecX);
			int z = Mth.floor(posSource.z + vecZ);

			BlockPos pos = new BlockPos(x, y, z);
			BlockPos posXZ = new BlockPos(x, 0, z);
			BlockState state = world.getBlockState(pos);

			if (lastScannedPosXZ == null || !posXZ.equals(lastScannedPosXZ)) {

				lastScannedPosXZ = new BlockPos(posXZ);

				AABB aabbCompare = new AABB(pos);
				VoxelShape voxelshape = Shapes.create(aabbCompare);

				boolean collided = Shapes.joinIsNotEmpty(state.getCollisionShape(world, pos).move(pos.getX(), pos.getY(), pos.getZ()), voxelshape, BooleanOp.AND);


				if (!state.isAir() && state.getBlock().defaultMapColor() != MapColor.PLANT &&
						collided) {
					BlockPos posUp = new BlockPos(x, y + 1, z);
					BlockState stateUp = world.getBlockState(posUp);

					if (stateUp.isAir()) {
						int height = getHeightForAnyBlock(state);


						if (height - previousBlockHeight <= heightDiff) {

							if (height == 8) {
								previousBlockHeight = 0;
								y++;
							} else {
								previousBlockHeight = height;
							}

							posLastNonWall = new Vec3(posSource.x + vecX, y, posSource.z + vecZ);


							continue;

						} else {
							posWall = new Vec3(posSource.x + vecX, y, posSource.z + vecZ);
							break;
						}

					} else {
						posWall = new Vec3(posSource.x + vecX, y, posSource.z + vecZ);
						break;
					}


				} else {
					posLastNonWall = new Vec3(posSource.x + vecX, y, posSource.z + vecZ);
				}

			} else {
				continue;
			}
		}

		if (posWall != null) {
			int amountWeHave = 1;
			int amountToAddPerXZ = 1;

			BlockState state = world.getBlockState(CoroUtilBlock.blockPos(posWall));
			BlockState state1 = world.getBlockState(CoroUtilBlock.blockPos(posLastNonWall).offset(1, 0, 0));
			BlockState state22 = world.getBlockState(CoroUtilBlock.blockPos(posLastNonWall).offset(-1, 0, 0));
			BlockState state3 = world.getBlockState(CoroUtilBlock.blockPos(posLastNonWall).offset(0, 0, 1));
			BlockState state4 = world.getBlockState(CoroUtilBlock.blockPos(posLastNonWall).offset(0, 0, -1));


			if (state.getBlock() == Blocks.CACTUS || state1.getBlock() == Blocks.CACTUS ||
					state22.getBlock() == Blocks.CACTUS || state3.getBlock() == Blocks.CACTUS || state4.getBlock() == Blocks.CACTUS) {
				return;
			}

			BlockPos pos2 = CoroUtilBlock.blockPos(posLastNonWall.x, posLastNonWall.y, posLastNonWall.z);
			BlockState state2 = world.getBlockState(pos2);
			if (state2.getBlock().defaultMapColor() == MapColor.WATER || state2.getBlock().defaultMapColor() == MapColor.FIRE) {
				return;
			}

			amountWeHave = trySpreadOnPos2(world, CoroUtilBlock.blockPos(posLastNonWall.x, posLastNonWall.y, posLastNonWall.z), amountWeHave, amountToAddPerXZ, 10, blockLayerable, maxBlockStackingAllowed);
		} else {

		}
	}

	public static int trySpreadOnPos2(Level world, BlockPos posSpreadTo, int amount, int amountAllowedToAdd, int maxDropAllowed, Block blockLayerable, int maxBlockStackingAllowed) {

		if (amount <= 0) return amount;


		if (!world.getBlockState(posSpreadTo.offset(0, 1, 0)).isAir()) {
			return amount;
		}

		BlockPos posCheckNonAir = new BlockPos(posSpreadTo);
		BlockState stateCheckNonAir = world.getBlockState(posCheckNonAir);

		int depth = 0;


		while (stateCheckNonAir.isAir()) {
			posCheckNonAir = posCheckNonAir.offset(0, -1, 0);
			stateCheckNonAir = world.getBlockState(posCheckNonAir);
			depth++;

			if (depth > maxDropAllowed) {
				return amount;
			}
		}

		BlockPos posCheckPlaceable = new BlockPos(posCheckNonAir);
		BlockState stateCheckPlaceable = world.getBlockState(posCheckPlaceable);


		if (maxBlockStackingAllowed > 0) {
			boolean sandMode = false;
			if (blockLayerable == Blocks.SNOW) {
				sandMode = false;
			} else if (blockLayerable == WeatherBlocks.BLOCK_SAND_LAYER.get()) {
				sandMode = true;
			}
			int foundBlocks = 0;
			BlockPos posCheckDownForStacks = new BlockPos(posCheckPlaceable);
			BlockState stateCheckDownForStacks = world.getBlockState(posCheckPlaceable);
			if ((!sandMode && stateCheckPlaceable.getBlock() == Blocks.SNOW_BLOCK) || (sandMode && stateCheckPlaceable.getBlock() == Blocks.SAND)) {
				while ((!sandMode && stateCheckDownForStacks.getBlock() == Blocks.SNOW_BLOCK) || (sandMode && stateCheckDownForStacks.getBlock() == Blocks.SAND)) {
					foundBlocks++;
					if (foundBlocks >= maxBlockStackingAllowed) {

						return amount;
					}
					posCheckDownForStacks = posCheckDownForStacks.offset(0, -1, 0);
					stateCheckDownForStacks = world.getBlockState(posCheckDownForStacks);
				}
			}
		}


		int distForPlaceableBlocks = 0;

		while (true && distForPlaceableBlocks < 10) {

			AABB aabbCompare = new AABB(posCheckPlaceable);

			VoxelShape voxelshape = Shapes.create(aabbCompare);
			boolean collided = Shapes.joinIsNotEmpty(stateCheckPlaceable.getCollisionShape(world, posCheckPlaceable).move(posCheckPlaceable.getX(), posCheckPlaceable.getY(), posCheckPlaceable.getZ()), voxelshape, BooleanOp.AND);


			if (stateCheckPlaceable.getBlock() != blockLayerable && !collided && stateCheckPlaceable.getFluidState().isEmpty()) {
				posCheckPlaceable = posCheckPlaceable.offset(0, -1, 0);
				stateCheckPlaceable = world.getBlockState(posCheckPlaceable);
				distForPlaceableBlocks++;
				continue;

			} else if (stateCheckPlaceable.isFaceSturdy(world, posCheckPlaceable, Direction.UP) ||
					stateCheckPlaceable.getBlock() == blockLayerable) {
				break;

			} else {

				return amount;
			}
		}


		if (distForPlaceableBlocks >= 10) {
			return amount;
		}


		if (!stateCheckPlaceable.isFaceSturdy(world, posCheckPlaceable, Direction.UP) &&
				stateCheckPlaceable.getBlock() != blockLayerable) {
			System.out.println("shouldnt be, failed a check somewhere!");
			return amount;
		}


		for (int i = 0; i < distForPlaceableBlocks; i++) {


			world.setBlockAndUpdate(posCheckNonAir.offset(0, -i, 0), Blocks.AIR.defaultBlockState());
		}

		BlockPos posPlaceLayerable = new BlockPos(posCheckPlaceable);
		BlockState statePlaceLayerable = world.getBlockState(posPlaceLayerable);

		int amountToAdd = amountAllowedToAdd;


		while (amountAllowedToAdd > 0 && world.getBlockState(posPlaceLayerable.offset(0, 1, 0)).isAir()) {

			if (amountAllowedToAdd <= 0) {
				break;
			}

			if (statePlaceLayerable.getBlock() == blockLayerable && getHeightForLayeredBlock(statePlaceLayerable) < layerableHeightPropMax) {
				int height = getHeightForLayeredBlock(statePlaceLayerable);


				height += amountAllowedToAdd;
				if (height > layerableHeightPropMax) {
					amountAllowedToAdd = height - layerableHeightPropMax;
					height = layerableHeightPropMax;

				} else {
					amountAllowedToAdd = 0;
				}
				try {

					world.setBlockAndUpdate(posPlaceLayerable, setBlockWithLayerState(blockLayerable, height));
				} catch (Exception e) {
					e.printStackTrace();
				}


				if (height == layerableHeightPropMax) {
					posPlaceLayerable = posPlaceLayerable.offset(0, 1, 0);
					statePlaceLayerable = world.getBlockState(posPlaceLayerable);
				}


			} else if (statePlaceLayerable.isFaceSturdy(world, posPlaceLayerable, Direction.UP)) {
				posPlaceLayerable = posPlaceLayerable.offset(0, 1, 0);
				statePlaceLayerable = world.getBlockState(posPlaceLayerable);

			} else if (statePlaceLayerable.isAir()) {

				int height = amountAllowedToAdd;
				if (height > layerableHeightPropMax) {
					amountAllowedToAdd = height - layerableHeightPropMax;
					height = layerableHeightPropMax;

				} else {
					amountAllowedToAdd = 0;
				}
				try {


					world.setBlockAndUpdate(posPlaceLayerable, setBlockWithLayerState(blockLayerable, height));
				} catch (Exception e) {
					e.printStackTrace();
				}


				if (height == layerableHeightPropMax) {
					posPlaceLayerable = posPlaceLayerable.offset(0, 1, 0);
					statePlaceLayerable = world.getBlockState(posPlaceLayerable);
				}
			} else {

			}
		}

		if (amountAllowedToAdd < 0) {

		}
		int amountAdded = amountToAdd - amountAllowedToAdd;
		amount -= amountAdded;
		return amount;

	}

	public static int getHeightForAnyBlock(BlockState state) {
		Block block = state.getBlock();
		if (block == Blocks.SNOW) {
			return state.getValue(SnowLayerBlock.LAYERS).intValue();
		} else if (block == WeatherBlocks.BLOCK_SAND_LAYER.get()) {
			return state.getValue(SandLayerBlock.LAYERS).intValue();
		} else if (block == Blocks.SAND || block == Blocks.SNOW_BLOCK) {
			return 8;
		} else if (block instanceof SlabBlock) {
			return 4;
		} else if (block == Blocks.AIR) {
			return 0;
		} else {
			return 8;
		}
	}

	public static int getHeightForLayeredBlock(BlockState state) {
		if (state.getBlock() == Blocks.SNOW) {
			return (state.getValue(SnowLayerBlock.LAYERS)).intValue();
		} else if (state.getBlock() == WeatherBlocks.BLOCK_SAND_LAYER.get()) {
			return state.getValue(SandLayerBlock.LAYERS).intValue();
		} else if (state.getBlock() == Blocks.SAND || state.getBlock() == Blocks.SNOW_BLOCK) {
			return 8;
		} else {

			return 0;
		}
	}

	public static BlockState setBlockWithLayerState(Block block, int height) {
		boolean solidBlockUnderMode = true;
		if (block == Blocks.SNOW) {
			if (height == layerableHeightPropMax && solidBlockUnderMode) {
				return Blocks.SNOW_BLOCK.defaultBlockState();
			} else {
				return block.defaultBlockState().setValue(SnowLayerBlock.LAYERS, height);
			}
		} else if (block == WeatherBlocks.BLOCK_SAND_LAYER.get()) {
			if (height == layerableHeightPropMax && solidBlockUnderMode) {
				return Blocks.SAND.defaultBlockState();
			} else {
				return block.defaultBlockState().setValue(SandLayerBlock.LAYERS, height);
			}
		} else {

			return null;
		}
	}

	public static BlockPos getPrecipitationHeightSafe(Level world, BlockPos pos) {
		return getPrecipitationHeightSafe(world, pos, Heightmap.Types.MOTION_BLOCKING);
	}


	public static BlockPos getPrecipitationHeightSafe(Level world, BlockPos pos, Heightmap.Types heightmapType) {
		if (world.isLoaded(pos)) {
			return world.getHeightmapPos(heightmapType, pos);
		} else {
			return new BlockPos(pos.getX(), -255, pos.getZ());
		}
	}
}
