package net.fieme.zerocraft.game.generator;

import java.util.concurrent.ThreadLocalRandom;

import net.fieme.zerocraft.game.OpenSimplexNoise;
import net.fieme.zerocraft.game.World;

public class SimplexWorldGenerator implements WorldGenerator {
	@Override
	public void generateWorld(World world) {
		int maxTerrainHeight = world.height / 2;
		
		world.spawnX = (short) (world.width / 2);
		world.spawnY = (short) (maxTerrainHeight + 5);
		world.spawnZ = (short) (world.depth / 2);
		
		OpenSimplexNoise noise = new OpenSimplexNoise(ThreadLocalRandom.current().nextLong());
		double noiseScale = 32;
		double dirtStartY = 2;
		double waterEndY = 4;
		
		// Generate normal terrain
		for (int worldGenY = 0; worldGenY < maxTerrainHeight; worldGenY++) {
			for (int worldGenZ = 0; worldGenZ < world.depth; worldGenZ++) {
				for (int worldGenX = 0; worldGenX < world.width; worldGenX++) {
					double value = noise.eval(worldGenX / noiseScale, 
							worldGenY / noiseScale, 
							worldGenZ / noiseScale);
					byte blockID = 0;
					
					if (value > 0) {
						if (worldGenY >= maxTerrainHeight / dirtStartY) blockID = 3;
						else {
							// Gold
							if (ThreadLocalRandom.current().nextInt(0, 100) == 1) blockID = 14;
							// Iron
							else if (ThreadLocalRandom.current().nextInt(0, 100) == 2) blockID = 15;
							// Coal
							else if (ThreadLocalRandom.current().nextInt(0, 100) == 4) blockID = 16;
							else blockID = 1;
						}
					} else
						if (worldGenY <= maxTerrainHeight / waterEndY) blockID = 9;
					
					world.setBlock(worldGenX, worldGenY, worldGenZ, blockID);
				}
			}
		}
		
		// Generate grass blocks
		for (int worldGenY = 0; worldGenY < maxTerrainHeight; worldGenY++) {
			for (int worldGenZ = 0; worldGenZ < world.depth; worldGenZ++) {
				for (int worldGenX = 0; worldGenX < world.width; worldGenX++) {
					if (worldGenY >= maxTerrainHeight / dirtStartY && 
						world.getBlock(worldGenX, worldGenY, worldGenZ) == 3)
						if (world.getBlock(worldGenX, worldGenY + 1, worldGenZ) == 0) 
							world.setBlock(worldGenX, worldGenY, worldGenZ, (byte) 2);
				}
			}
		}
	}
}
