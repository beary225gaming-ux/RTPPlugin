package com.rtpplugin.rtp.utils;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.util.Random;
import java.util.Set;

public class SafeLocationFinder {

    private static final Random RANDOM = new Random();

    /**
     * Blocks that are unsafe to stand ON (the block directly below the player).
     * A player standing on these would take damage or be in an undesirable state.
     */
    private static final Set<Material> UNSAFE_FLOOR = Set.of(
            Material.LAVA,
            Material.WATER,
            Material.CACTUS,
            Material.FIRE,
            Material.SOUL_FIRE,
            Material.MAGMA_BLOCK,
            Material.WITHER_ROSE,
            Material.SWEET_BERRY_BUSH,
            Material.CAMPFIRE,
            Material.SOUL_CAMPFIRE,
            Material.POINTED_DRIPSTONE,
            Material.POWDER_SNOW,
            Material.COBWEB
    );

    /**
     * Blocks that are unsafe to have the player's body inside.
     * The player occupies two blocks (feet and head level).
     */
    private static final Set<Material> UNSAFE_BODY = Set.of(
            Material.LAVA,
            Material.WATER,
            Material.FIRE,
            Material.SOUL_FIRE,
            Material.CACTUS,
            Material.SWEET_BERRY_BUSH,
            Material.WITHER_ROSE,
            Material.POWDER_SNOW,
            Material.COBWEB
    );

    /**
     * Attempts to find a safe teleport location in the given world.
     *
     * @param world      The world to search in.
     * @param minDist    Minimum horizontal distance from 0,0.
     * @param maxDist    Maximum horizontal distance from 0,0.
     * @param maxAttempts Maximum number of random attempts before giving up.
     * @return A safe Location, or null if none was found.
     */
    public static Location find(World world, int minDist, int maxDist, int maxAttempts) {
        for (int attempt = 0; attempt < maxAttempts; attempt++) {
            int x = randomCoord(minDist, maxDist);
            int z = randomCoord(minDist, maxDist);

            Location candidate = getSafeY(world, x, z);
            if (candidate != null) {
                return candidate;
            }
        }
        return null;
    }

    /**
     * Generates a random coordinate that is between minDist and maxDist from 0,
     * randomly on either the positive or negative side.
     */
    private static int randomCoord(int minDist, int maxDist) {
        int distance = minDist + RANDOM.nextInt(maxDist - minDist + 1);
        return RANDOM.nextBoolean() ? distance : -distance;
    }

    /**
     * Scans downward from the world's highest block at (x, z) to find
     * the first safe standing position.
     */
    private static Location getSafeY(World world, int x, int z) {
        // For the nether, scan from y=120 downward (avoid bedrock ceiling)
        int startY;
        int minY;

        switch (world.getEnvironment()) {
            case NETHER -> {
                startY = 120;
                minY = 5;
            }
            case THE_END -> {
                startY = world.getHighestBlockYAt(x, z);
                minY = 0;
            }
            default -> { // NORMAL (overworld)
                startY = world.getHighestBlockYAt(x, z);
                minY = world.getMinHeight() + 1;
            }
        }

        for (int y = startY; y > minY; y--) {
            Block floor = world.getBlockAt(x, y, z);
            Block feet  = world.getBlockAt(x, y + 1, z);
            Block head  = world.getBlockAt(x, y + 2, z);

            // Floor must be solid and safe to stand on
            if (!floor.getType().isSolid()) continue;
            if (UNSAFE_FLOOR.contains(floor.getType())) continue;

            // The two blocks the player occupies must be passable and safe
            if (!feet.getType().isAir() && UNSAFE_BODY.contains(feet.getType())) continue;
            if (!head.getType().isAir() && UNSAFE_BODY.contains(head.getType())) continue;

            // Both body blocks must be passable (air, tall grass, etc.)
            if (feet.getType().isSolid()) continue;
            if (head.getType().isSolid()) continue;

            // Make sure we're not landing in void or out-of-bounds
            if (y + 1 < world.getMinHeight() || y + 2 > world.getMaxHeight()) continue;

            // Center the player on the block
            return new Location(world, x + 0.5, y + 1, z + 0.5);
        }

        return null;
    }
}
