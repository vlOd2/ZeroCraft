package net.fieme.zerocraft.game.generator;

import java.util.concurrent.ThreadLocalRandom;

import net.fieme.zerocraft.game.World;
import net.fieme.zerocraft.game.generator.alphagen.ChunkBasedOctaveGenerator;

/**
 * Uses code from Alpha 1.0.6_03<br>
 * Code mapped with RetroMCP 1.0<br>
 * <br>
 * NOTE: This might be removed in a future build
 */
public class AlphaWorldGenerator implements WorldGenerator {
	private byte getBlockInArray(byte[] array, int x, int y, int z) {
		return array[x << 11 | z << 7 | y];
	}
	
	@Override
	public void generateWorld(World world) {
		world.spawnX = (short) (world.width / 2);
		world.spawnY = (short) (world.height);
		world.spawnZ = (short) (world.depth / 2);
		
		ChunkBasedOctaveGenerator generator = new ChunkBasedOctaveGenerator(
				ThreadLocalRandom.current().nextLong());
		
		for (int chunkZ = 0; chunkZ < world.depth / 16; chunkZ++) {
			for (int chunkX = 0; chunkX < world.width / 16; chunkX++) {
				byte[] chunk = generator.generateChunk(chunkX, chunkZ);

				for (int blockY = 0; blockY < 127; blockY++) {
					for (int blockZ = 0; blockZ < 16; blockZ++) {
						for (int blockX = 0; blockX < 16; blockX++) {
							int globalBlockX = chunkX * 16 + blockX;
							int globalBlockZ = chunkZ * 16 + blockZ;
							byte block = this.getBlockInArray(chunk, blockX, blockY, blockZ);
							world.setBlock(globalBlockX, blockY, globalBlockZ, block);
						}
					}	
				}
			}	
		}
	}
}
