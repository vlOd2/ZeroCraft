package net.fieme.zerocraft.game.generator;

import net.fieme.zerocraft.game.World;

public class FlatWorldGenerator implements WorldGenerator {
	@Override
	public void generateWorld(World world) throws Exception {
		int maxTerrainHeight = world.height / 2;
		
		world.spawnX = (short) (world.width / 2);
		world.spawnY = (short) (maxTerrainHeight + 5);
		world.spawnZ = (short) (world.depth / 2);
		
		if (world.height < 6) {
			throw new Exception("Minimum size for this generator is 1x6x1!");
		}
		
		for (int worldGenY = maxTerrainHeight - 1; worldGenY < maxTerrainHeight; worldGenY++) {
			for (int worldGenZ = 0; worldGenZ < world.depth; worldGenZ++) {
				for (int worldGenX = 0; worldGenX < world.width; worldGenX++) {
					world.setBlock(worldGenX, worldGenY, worldGenZ, (byte) 2);
				}
			}
		}
		
		for (int worldGenY = maxTerrainHeight - 3; worldGenY < maxTerrainHeight - 1; worldGenY++) {
			for (int worldGenZ = 0; worldGenZ < world.depth; worldGenZ++) {
				for (int worldGenX = 0; worldGenX < world.width; worldGenX++) {
					world.setBlock(worldGenX, worldGenY, worldGenZ, (byte) 3);
				}
			}
		}
		
		for (int worldGenY = 0; worldGenY < maxTerrainHeight - 3; worldGenY++) {
			for (int worldGenZ = 0; worldGenZ < world.depth; worldGenZ++) {
				for (int worldGenX = 0; worldGenX < world.width; worldGenX++) {
					world.setBlock(worldGenX, worldGenY, worldGenZ, (byte) 1);
				}
			}
		}
	}
}
