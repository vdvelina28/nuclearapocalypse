package net.elileo.nuclearapocalypse.effect;

import net.elileo.nuclearapocalypse.command.SkipTimerCommand;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class NuclearExplosionEffect {

    public static void createExplosion(ServerLevel world, BlockPos pos, Entity player) {
        if (world == null || pos == null) {
            System.out.println("Explosion cannot happen: world or position is null!");
            return;
        }
        System.out.println("World LevelData is " + (world.getLevelData() != null ? "initialized" : "null"));



        float explosionPower = 50.0F; // Initial explosion power
        Explosion explosion = world.explode(
                player,
                pos.getX(), pos.getY(), pos.getZ(),
                explosionPower,
                true, // Causes fire
                Level.ExplosionInteraction.BLOCK
        );

        if (explosion != null) {
            System.out.println("Explosion occurred successfully.");

            // Destroy blocks in the crater radius to create the round crater
            destroyBlocksInRadius(world, pos, (int) explosionPower);

            // Other explosion effects
            spawnMushroomCloud(world, pos); // Mushroom cloud effect
            spawnFireInCrater(world, pos);   // Spawn fire in the crater
            spawnContinuousSmoke(world, pos); // Continuous smoke in the crater

            // Apply shockwave effect to the world
            ShockwaveEffect.applyShockwaveToWorldAboveGround(world, pos, (int) explosionPower);
            ShockwaveEffect.applyShockwave(world, pos); // Use pos as explosion center

            applyRadiationToNearbyPlayers(world, pos);

        } else {
            System.out.println("Explosion failed to occur.");
        }
    }



    // Spawn mushroom cloud particles
    // Spawn mushroom cloud particles
    private static void spawnMushroomCloud(ServerLevel world, BlockPos pos) {

        if (world == null || pos == null) {
            System.out.println("World or Position is null in spawnMushroomCloud.");
            return;
        }
        System.out.println("World LevelData is " + (world.getLevelData() != null ? "initialized" : "null"));


        double baseRadius = 50.0;  // Adjusted size for the cloud
        double cloudHeight = 25.0;  // Decreased height of the cloud cap for closeness
        double stemHeight = 20.0;    // Adjusted height of the stem
        int initialParticleCount = 1000;  // Increased particle count for density
        int decayTime = 180;  // Total time for the effect in seconds

        Timer timer = new Timer();
        TimerTask task = new TimerTask() {
            private int ticksRemaining = decayTime * 20;  // Convert seconds to ticks

            @Override
            public void run() {
                if (ticksRemaining > 0) {
                    // Generate mushroom cloud cap with both smoke and fire particles
                    for (int i = 0; i < initialParticleCount; i++) {
                        double angle = Math.random() * 2 * Math.PI;
                        double radius = baseRadius * Math.random();
                        double heightFactor = Math.pow(Math.random(), 3);  // More dense height distribution

                        double offsetX = radius * Math.cos(angle);
                        double offsetZ = radius * Math.sin(angle);
                        double offsetY = cloudHeight * heightFactor;

                        // Adjust Y to bring the cloud closer to the crater
                        world.sendParticles(
                                ParticleTypes.CAMPFIRE_COSY_SMOKE,
                                pos.getX() + offsetX,
                                pos.getY() + offsetY + 2,  // Lowered to bring it closer to the crater
                                pos.getZ() + offsetZ,
                                2, 0, 0, 0, 0.05  // Increase count and speed for density
                        );

                        // Fire particles
                        if (Math.random() < 0.15) {  // Increased chance to spawn fire particles
                            world.sendParticles(
                                    ParticleTypes.FLAME,
                                    pos.getX() + offsetX,
                                    pos.getY() + offsetY + 2,  // Lowered to bring it closer to the crater
                                    pos.getZ() + offsetZ,
                                    2, 0, 0, 0, 0.05  // Increase count and speed for density
                            );
                        }
                    }

                    // Generate mushroom stem with smoke particles
                    for (int i = 0; i < initialParticleCount / 5; i++) {  // Increased stem density
                        double offsetY = stemHeight * Math.random();
                        double offsetX = (Math.random() - 0.5) * 5;  // Reduced width for denser stem
                        double offsetZ = (Math.random() - 0.5) * 5;  // Reduced width for denser stem

                        world.sendParticles(
                                ParticleTypes.LARGE_SMOKE,
                                pos.getX() + offsetX,
                                pos.getY() + offsetY + 2,  // Lowered to bring the stem closer to the crater
                                pos.getZ() + offsetZ,
                                2, 0, 0, 0, 0.05  // Increase count and speed for density
                        );
                    }

                    ticksRemaining--;
                } else {
                    timer.cancel();
                }
            }
        };

        timer.scheduleAtFixedRate(task, 0, 100);  // Schedule the task every 100 ms
    }

    private static void spawnFireInCrater(ServerLevel world, BlockPos pos) {
        if (world == null) {
            System.out.println("World is null!");
            return;
        }
        int craterRadius = 50;  // Crater fire radius
        int fireSpreadRadius = 125;  // Expanded fire spread radius outside the crater

        // Spawn fire in the crater
        for (int x = -craterRadius; x <= craterRadius; x++) {
            for (int z = -craterRadius; z <= craterRadius; z++) {
                for (int y = -10; y <= 10; y++) {  // Adjust to account for crater depth
                    BlockPos firePos = pos.offset(x, y, z);
                    double distance = firePos.distSqr(pos);  // Calculate squared distance from the center

                    // Ensure fire is only placed in the crater area on solid surfaces
                    if (distance <= craterRadius * craterRadius) {
                        BlockPos belowPos = firePos.below();
                        BlockState belowBlock = world.getBlockState(belowPos);

                        // Check if the position below is solid and current position is air
                        if (belowBlock.isSolid() && world.getBlockState(firePos).isAir()) {
                            world.setBlock(firePos, Blocks.FIRE.defaultBlockState(), 3);  // Place fire block
                        }
                    }
                }
            }
        }

        // Spawn fire outside the crater (increased fire spread radius)
        for (int x = -fireSpreadRadius; x <= fireSpreadRadius; x++) {
            for (int z = -fireSpreadRadius; z <= fireSpreadRadius; z++) {
                BlockPos firePos = pos.offset(x, 0, z);
                double distance = firePos.distSqr(pos);  // Calculate squared distance from the center

                // Ensure fire is only placed outside the crater but within the spread radius
                if (distance > craterRadius * craterRadius && distance <= fireSpreadRadius * fireSpreadRadius) {
                    BlockPos belowPos = firePos.below();
                    BlockState belowBlock = world.getBlockState(belowPos);

                    // Check if the position below is solid and current position is air
                    if (belowBlock.isSolid() && world.getBlockState(firePos).isAir()) {
                        world.setBlock(firePos, Blocks.FIRE.defaultBlockState(), 3);  // Place fire block
                    }
                }
            }
        }
        System.out.println("Fire spawned in the crater and surrounding area.");
    }


    private static void applyRadiationToNearbyPlayers(ServerLevel world, BlockPos explosionPos) {
        if (world == null || explosionPos == null) {
            System.out.println("World or explosion position is null in applyRadiationToNearbyPlayers.");
            return;
        }

        List<ServerPlayer> players = world.getPlayers(player ->
                player.distanceToSqr(explosionPos.getX(), explosionPos.getY(), explosionPos.getZ()) <= 200 * 200
        );

        for (ServerPlayer nearbyPlayer : players) {
            RadiationEffect.applyRadiation(nearbyPlayer);
            System.out.println("Applied radiation to player: " + nearbyPlayer.getName().getString());
        }
    }



    // Spawn continuous smoke in the crater
    // Spawn continuous smoke in the crater
    private static void spawnContinuousSmoke(ServerLevel world, BlockPos pos) {
        if (world == null || pos == null) {
            System.out.println("World or position is null in spawnContinuousSmoke.");
            return;
        }
        int radius = 50;  // Radius for continuous smoke
        int smokeCount = 400;  // Increased number of smoke particles for density
        int duration = 120;  // Duration in seconds
        int craterDepth = 20;  // Smoke will extend 20 blocks deep into the crater

        Timer timer = new Timer();
        TimerTask task = new TimerTask() {
            private int ticksRemaining = duration * 20;  // Convert seconds to ticks

            @Override
            public void run() {
                if (ticksRemaining > 0) {
                    // Generate smoke particles around the crater and inside it
                    for (int i = 0; i < smokeCount; i++) {
                        double offsetX = (Math.random() - 0.5) * radius;
                        double offsetZ = (Math.random() - 0.5) * radius;
                        double offsetY = (Math.random() * 30) - craterDepth;  // Smoke goes up to 30 blocks above and 20 blocks into the crater

                        // Send smoke particles to the world
                        world.sendParticles(
                                ParticleTypes.CAMPFIRE_COSY_SMOKE,
                                pos.getX() + offsetX,
                                pos.getY() + offsetY,
                                pos.getZ() + offsetZ,
                                5, 0, 0, 0, 0.1  // Increased particle count and speed for higher density
                        );
                    }
                    ticksRemaining--;
                } else {
                    timer.cancel();
                }
            }
        };

        timer.scheduleAtFixedRate(task, 0, 100);  // Schedule the task every 100 ms
        System.out.println("Continuous smoke spawned in the crater with increased density.");
    }

    private static void destroyBlocksInRadius(ServerLevel world, BlockPos center, int radius) {
        if (world == null || center == null) {
            System.out.println("World or center position is null in destroyBlocksInRadius.");
            return;
        }
        // Ensure the crater is circular
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                double distanceSquared = x * x + z * z; // Use distance squared for circular check

                // Ensure only blocks within the spherical radius are destroyed
                if (distanceSquared <= radius * radius) {
                    // Calculate the height based on the distance from the center for depth
                    int heightAdjustment = (int) (Math.sqrt(radius * radius - distanceSquared) / 2); // Adjust depth

                    for (int y = -heightAdjustment; y <= 0; y++) { // Loop to create depth
                        BlockPos blockPos = center.offset(x, y, z);
                        BlockState blockState = world.getBlockState(blockPos);

                        // Avoid destroying air blocks and bedrock
                        if (!blockState.isAir() && !blockState.is(Blocks.BEDROCK)) {
                            world.setBlock(blockPos, Blocks.AIR.defaultBlockState(), 3);  // Clear the block
                        }
                    }
                }
            }
        }
        System.out.println("Blocks destroyed within circular radius: " + radius);
    }
}

