package net.elileo.nuclearapocalypse.effect;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;



import java.util.ArrayList;
import java.util.List;

public class ShockwaveEffect {

    private static final int EFFECT_RADIUS = 200;  // Radius of the effect
    private static final int BATCH_SIZE = 5000;    // Number of blocks to process per batch

    public static void applyShockwave(ServerLevel world, BlockPos explosionCenter) {
        if (world == null) {
            System.out.println("World is not initialized yet.");
            return;
        }
        System.out.println("World LevelData is " + (world.getLevelData() != null ? "initialized" : "null"));

        System.out.println("Applying shockwave effect at " + explosionCenter);
        applyShockwaveEffect(world, explosionCenter, EFFECT_RADIUS);
        setEntitiesOnFire(world, explosionCenter, 200);
    }

    private static void applyShockwaveEffect(ServerLevel world, BlockPos explosionCenter, int effectRadius) {
        if (world == null) {
            System.out.println("World is null!");
            return;
        }
        // Apply the shockwave in chunks
        applyShockwaveToWorldAboveGround(world, explosionCenter, effectRadius);
    }

    private static boolean isValidBlock(ServerLevel world, BlockPos pos) {

        BlockState state = world.getBlockState(pos);
        return !state.isAir() && state.isSolid();
    }

    public static void applyShockwaveToWorldAboveGround(ServerLevel world, BlockPos center, int radius) {
        if (world == null ) {
            System.out.println("World or position is null in applyShockwaveToWorldAboveGround.");
            return;
        }
        System.out.println("World LevelData is " + (world.getLevelData() != null ? "initialized" : "null"));

        int seaLevel = world.getSeaLevel();
        int minY = seaLevel - 20;// Start applying the effect above sea level
        int maxY = seaLevel + 100;  // Cover 100 blocks above sea level for terrain effects

        List<BlockPos> blocksToTransform = new ArrayList<>();

        // Collect blocks in a circular area
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                double distance = Math.sqrt(x * x + z * z);
                if (distance <= radius) {
                    for (int y = minY; y <= maxY; y++) {
                        BlockPos pos = new BlockPos(center.getX() + x, y, center.getZ() + z);
                        if (isValidBlock(world, pos)) {
                            blocksToTransform.add(pos);
                        }
                    }
                }
            }
        }

        System.out.println("Total blocks to transform: " + blocksToTransform.size());

        // Process the blocks synchronously for smooth transformation
        for (int i = 0; i < blocksToTransform.size(); i += BATCH_SIZE) {
            List<BlockPos> batch = blocksToTransform.subList(i, Math.min(i + BATCH_SIZE, blocksToTransform.size()));
            for (BlockPos pos : batch) {
                transformBlock(world, pos);
            }
        }

        System.out.println("Shockwave effect processing complete.");
    }

    private static void transformBlock(ServerLevel world, BlockPos pos) {
        if (world == null) {
            System.out.println("World is null!");
            return;
        }
        BlockState state = world.getBlockState(pos);

        if (state.isAir()) {
            return;
        }

        if (state.is(Blocks.OAK_LOG) || state.is(Blocks.BIRCH_LOG) || state.is(Blocks.SPRUCE_LOG) ||
                state.is(Blocks.JUNGLE_LOG) || state.is(Blocks.ACACIA_LOG) || state.is(Blocks.CHERRY_LOG) ||
                state.is(Blocks.MANGROVE_LOG) || state.is(Blocks.DARK_OAK_LOG)) {
            removeLeavesFromTree(world, pos);
        } else if (state.is(Blocks.SNOW)) {
            world.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
        } else if (state.is(Blocks.SNOW_BLOCK)) {
            world.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
        } else if (state.is(Blocks.POWDER_SNOW)) {
            world.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
        } else if (state.is(Blocks.WATER)) {
            world.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
        } else if (isGrassSurfaceBlock(state)) {
            world.setBlock(pos, Blocks.DEEPSLATE.defaultBlockState(), 3);
        } else if (isPlantBlock(state)) {
            world.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
        } else if (state.is(Blocks.SAND)) {
            world.setBlock(pos, Blocks.GRAVEL.defaultBlockState(), 3);
        } else if (state.is(Blocks.SANDSTONE)) {
            world.setBlock(pos, Blocks.DEEPSLATE.defaultBlockState(), 3);
        } else if (state.is(Blocks.OAK_PLANKS)) {
            world.setBlock(pos, Blocks.TUFF_BRICKS.defaultBlockState(), 3);
        } else if (state.is(Blocks.OAK_STAIRS)) {
            world.setBlock(pos, Blocks.MOSSY_STONE_BRICK_STAIRS.defaultBlockState(), 3);
        } else if (state.is(Blocks.OAK_TRAPDOOR)) {
            world.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
        } else if (state.is(Blocks.OAK_FENCE)) {
            world.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
        } else if (state.is(Blocks.OAK_FENCE_GATE)) {
            world.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
        } else if (state.is(Blocks.GLASS)) {
            world.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
        } else if (state.is(Blocks.STONE) && isInJungleTemple(pos)) {
            world.setBlock(pos, Blocks.DEEPSLATE.defaultBlockState(), 3);
        } else if (state.is(Blocks.MUD)) {
            world.setBlock(pos, Blocks.CLAY.defaultBlockState(), 3);
        } else if (state.is(Blocks.RED_SAND)) {
            world.setBlock(pos, Blocks.GRAVEL.defaultBlockState(), 3);
        }
    }

    private static void removeLeavesFromTree(ServerLevel world, BlockPos logPos) {
        if (world == null) {
            System.out.println("World is null!");
            return;
        }
        // Increase the range of leaf removal
        for (int x = -5; x <= 5; x++) {
            for (int y = -5; y <= 5; y++) {
                for (int z = -5; z <= 5; z++) {
                    BlockPos leafPos = logPos.offset(x, y, z);
                    BlockState leafState = world.getBlockState(leafPos);

                    if (leafState.is(Blocks.OAK_LEAVES) || leafState.is(Blocks.BIRCH_LEAVES) ||
                            leafState.is(Blocks.SPRUCE_LEAVES) || leafState.is(Blocks.JUNGLE_LEAVES) ||
                            leafState.is(Blocks.ACACIA_LEAVES) || leafState.is(Blocks.CHERRY_LEAVES) ||
                            leafState.is(Blocks.MANGROVE_LEAVES) || leafState.is(Blocks.FLOWERING_AZALEA_LEAVES) ||
                            leafState.is(Blocks.AZALEA_LEAVES) || leafState.is(Blocks.DARK_OAK_LEAVES)) {
                        world.setBlock(leafPos, Blocks.AIR.defaultBlockState(), 3);
                    }
                }
            }
        }
    }

    private static boolean isGrassSurfaceBlock(BlockState state) {

        return state.is(Blocks.DIRT) || state.is(Blocks.GRASS_BLOCK) || state.is(Blocks.PODZOL) ||
                state.is(Blocks.DIRT_PATH) || state.is(Blocks.COARSE_DIRT) || state.is(Blocks.ROOTED_DIRT);
    }

    private static boolean isPlantBlock(BlockState state) {

        return state.is(Blocks.SHORT_GRASS) || state.is(Blocks.TALL_GRASS) || state.is(Blocks.FERN) ||
                state.is(Blocks.LARGE_FERN) || state.is(Blocks.RED_TULIP) || state.is(Blocks.POPPY) ||
                state.is(Blocks.DANDELION) || state.is(Blocks.BLUE_ORCHID) || state.is(Blocks.ALLIUM) ||
                state.is(Blocks.WHITE_TULIP) || state.is(Blocks.PINK_TULIP) || state.is(Blocks.OXEYE_DAISY) ||
                state.is(Blocks.CORNFLOWER) || state.is(Blocks.LILY_OF_THE_VALLEY) || state.is(Blocks.LILY_PAD) ||
                state.is(Blocks.WITHER_ROSE) || state.is(Blocks.SUNFLOWER) || state.is(Blocks.LILAC) ||
                state.is(Blocks.ROSE_BUSH) || state.is(Blocks.PEONY) || state.is(Blocks.SWEET_BERRY_BUSH) ||
                state.is(Blocks.AZURE_BLUET) || state.is(Blocks.SUGAR_CANE) || state.is(Blocks.BAMBOO) ||
                state.is(Blocks.VINE) || state.is(Blocks.HAY_BLOCK) || state.is(Blocks.MELON) ||
                state.is(Blocks.PUMPKIN) || state.is(Blocks.ORANGE_TULIP) || state.is(Blocks.BROWN_MUSHROOM_BLOCK) ||
                state.is(Blocks.RED_MUSHROOM_BLOCK) || state.is(Blocks.MUSHROOM_STEM) ||
                state.is(Blocks.RED_MUSHROOM) || state.is(Blocks.BROWN_MUSHROOM) || state.is(Blocks.CACTUS);
    }

    private static boolean isInJungleTemple(BlockPos pos) {

        int templeWidth = 22;
        int minX = pos.getX() % templeWidth;
        int minZ = pos.getZ() % templeWidth;
        return minX >= 0 && minX < templeWidth && minZ >= 0 && minZ < templeWidth;
    }

    private static boolean isSpecialStructure(BlockPos pos, ServerLevel world) {

        return isInVillage(pos, world) || isInWoodlandMansion(pos, world) ||
                isInPillagerOutpost(pos, world) || isInSwampHut(pos, world) || isInShipwreck(pos, world);
    }

    private static boolean isInVillage(BlockPos pos, ServerLevel world) {
        int villageWidth = 32;
        int minX = pos.getX() % villageWidth;
        int minZ = pos.getZ() % villageWidth;
        return minX >= 0 && minX < villageWidth && minZ >= 0 && minZ < villageWidth;
    }

    private static boolean isInWoodlandMansion(BlockPos pos, ServerLevel world) {
        int mansionWidth = 22;
        int minX = pos.getX() % mansionWidth;
        int minZ = pos.getZ() % mansionWidth;
        return minX >= 0 && minX < mansionWidth && minZ >= 0 && minZ < mansionWidth;
    }

    private static boolean isInPillagerOutpost(BlockPos pos, ServerLevel world) {
        int outpostWidth = 16;
        int minX = pos.getX() % outpostWidth;
        int minZ = pos.getZ() % outpostWidth;
        return minX >= 0 && minX < outpostWidth && minZ >= 0 && minZ < outpostWidth;
    }

    private static boolean isInSwampHut(BlockPos pos, ServerLevel world) {
        int hutWidth = 7;
        int minX = pos.getX() % hutWidth;
        int minZ = pos.getZ() % hutWidth;
        return minX >= 0 && minX < hutWidth && minZ >= 0 && minZ < hutWidth;
    }

    private static boolean isInShipwreck(BlockPos pos, ServerLevel world) {
        int wreckWidth = 16;
        int minX = pos.getX() % wreckWidth;
        int minZ = pos.getZ() % wreckWidth;
        return minX >= 0 && minX < wreckWidth && minZ >= 0 && minZ < wreckWidth;
    }
    private static void setEntitiesOnFire(ServerLevel world, BlockPos center, int radius) {
        List<Entity> entities = world.getEntitiesOfClass(Entity.class,
                new net.minecraft.world.phys.AABB(center).inflate(radius)); // Get entities in a sphere

        for (Entity entity : entities) {
            if (entity instanceof LivingEntity livingEntity) { // Ensure the entity is a LivingEntity
                // Apply the fire resistance effect if not invulnerable
                livingEntity.addEffect(new MobEffectInstance(MobEffects.HARM, 100, 0)); // 100 ticks = 5 seconds
            }
        }

        System.out.println("Entities set on fire: " + entities.size());
    }
}