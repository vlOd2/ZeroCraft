package net.fieme.zerocraft.game;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

/**
 * A in-game world
 */
public class World implements Serializable {
	private static final long serialVersionUID = 8105344403903580546L;
	public final String name;
	public final short width;
	public final short height;
	public final short depth;
	public short spawnX;
	public short spawnY;
	public short spawnZ;
	private byte[][][] blocks;
	private transient ArrayList<Entity> entities = new ArrayList<Entity>();
	
	/**
	 * Creates a new world using the specified size
	 * 
	 * @param width the width
	 * @param height the height
	 * @param depth the depth
	 */
	public World(String name, 
			short width, short height, short depth) {
		this.name = name;
		this.width = width;
		this.height = height;
		this.depth = depth;
		this.blocks = new byte[width][height][depth];
	}

    /**
     * NOTE: Not intended to be used function
     */
	public void func_0000() {
		this.entities = new ArrayList<Entity>();
	}
	
	/**
	 * Gets the block ID at the specified position
	 * 
	 * @param x block x
	 * @param y block y
	 * @param z block z
	 * @throws IllegalArgumentException if the block position is invalid
	 * @return block ID
	 */
	public byte getBlock(int x, int y, int z) {
		if (!this.isValidBlockPos(x, y, z)) throw new IllegalArgumentException();
		return this.blocks[x][y][z];
	}
	
	/**
	 * Sets the ID of the block at the specified positon
	 * 
	 * @param x block x
	 * @param y block y
	 * @param z block z
	 * @throws IllegalArgumentException if the block position is invalid
	 * @param block new block ID
	 */
	public void setBlock(int x, int y, int z, byte block) {
		if (!this.isValidBlockPos(x, y, z)) throw new IllegalArgumentException();
		this.blocks[x][y][z] = block;
	}
	
	/**
	 * Checks if the specified block position is valid 
	 * 
	 * @param x block x
	 * @param y block y
	 * @param z block z
	 * @return the check status
	 */
	public boolean isValidBlockPos(int x, int y, int z) {
		return !((
						x < (this.width * -1) || 
						y < (this.height * -1) || 
						z < (this.depth * -1)
				) || (
						x > (this.width) || 
						y > (this.height) || 
						z > (this.depth)
				));
	}

	/**
	 * Gets the index in the 1D array representation of this world
	 * 
	 * @param x block x
	 * @param y block y
	 * @param z block z
	 * @see World#getAsArray()
	 * @return the index
	 */
	public int getWorldIndexInArray(int x, int y, int z) {
		return (y * this.depth + z) * this.width + x;
	}
	
	/**
	 * Gets this world as 1D array
	 *
	 * @return the byte array
	 */
	public byte[] getAsArray() {
		byte[] data = new byte[this.width * this.height * this.depth];
		
		for (int y = 0; y < this.height; y++) {
			for (int z = 0; z < this.depth; z++) {
				for (int x = 0; x < this.width; x++) {
					data[getWorldIndexInArray(x, y, z)] = this.blocks[x][y][z];
				}
			}
		}
		
		return data;
	}
	
	/**
	 * Adds an entity to this world
	 * 
	 * @param ent the entity
	 */
	public void addEntity(Entity ent) {
		if (ent == null) return;
		ent.world = this;
		ent.id = this.entities.size() + 1;
		this.entities.add(ent);
	}
	
	/**
	 * Removes an entity from this world
	 * 
	 * @param ent the entity
	 */
	public void removeEntity(Entity ent) {
		if (ent == null) return;
		ent.world = null;
		ent.id = 0;
		this.entities.remove(ent);
	}
	
	/**
	 * Generates flat terrain into this world
	 */
	public void generateFlatTerrain() {
		int maxTerrainHeight = this.height / 2;
		
		this.spawnX = (short) (this.width / 2);
		this.spawnY = (short) (maxTerrainHeight + 5);
		this.spawnZ = (short) (this.depth / 2);
		
		for (int worldGenY = maxTerrainHeight - 1; worldGenY < maxTerrainHeight; worldGenY++) {
			for (int worldGenZ = 0; worldGenZ < this.depth; worldGenZ++) {
				for (int worldGenX = 0; worldGenX < this.width; worldGenX++) {
					this.setBlock(worldGenX, worldGenY, worldGenZ, (byte) 2);
				}
			}
		}
		
		for (int worldGenY = maxTerrainHeight - 3; worldGenY < maxTerrainHeight - 1; worldGenY++) {
			for (int worldGenZ = 0; worldGenZ < this.depth; worldGenZ++) {
				for (int worldGenX = 0; worldGenX < this.width; worldGenX++) {
					this.setBlock(worldGenX, worldGenY, worldGenZ, (byte) 3);
				}
			}
		}
		
		for (int worldGenY = 0; worldGenY < maxTerrainHeight - 3; worldGenY++) {
			for (int worldGenZ = 0; worldGenZ < this.depth; worldGenZ++) {
				for (int worldGenX = 0; worldGenX < this.width; worldGenX++) {
					this.setBlock(worldGenX, worldGenY, worldGenZ, (byte) 1);
				}
			}
		}
	}
	
	/**
	 * Generates noise terrain designed to look like the far lands
	 */
	public void generateFarLandsTerrain() {
		int maxTerrainHeight = this.height / 2;
		
		this.spawnX = (short) (this.width / 1.3);
		this.spawnY = (short) 5;
		this.spawnZ = (short) (this.depth / 1.3);
		
		OpenSimplexNoise noise = new OpenSimplexNoise(ThreadLocalRandom.current().nextLong());
		double noiseScale = 6.5;
		double dirtStartY = 2;
		double waterEndY = 4;
		
		for (int worldGenY = 0; worldGenY < maxTerrainHeight; worldGenY++) {
			for (int worldGenX = 0; worldGenX < this.width; worldGenX++) {
				double value = noise.eval(worldGenX / noiseScale, worldGenY / noiseScale);
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
				
				for (int worldGenZ = 0; worldGenZ < this.depth / 2; worldGenZ++) {
					this.setBlock(worldGenX, worldGenY, worldGenZ, blockID);
				}
			}
		}
		
		for (int worldGenY = 0; worldGenY < maxTerrainHeight; worldGenY++) {
			for (int worldGenZ = 0; worldGenZ < this.depth; worldGenZ++) {
				double value = noise.eval(0, worldGenY / noiseScale, worldGenZ / noiseScale);
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

				for (int worldGenX = 0; worldGenX < this.width / 2; worldGenX++) {
					this.setBlock(worldGenX, worldGenY, worldGenZ, blockID);
				}
			}
		}

		for (int worldGenY = 0; worldGenY < maxTerrainHeight; worldGenY++) {
			for (int worldGenZ = 0; worldGenZ < this.depth; worldGenZ++) {
				for (int worldGenX = 0; worldGenX < this.width; worldGenX++) {
					if (worldGenY >= maxTerrainHeight / dirtStartY && 
						this.getBlock(worldGenX, worldGenY, worldGenZ) == 3)
						if (this.getBlock(worldGenX, worldGenY + 1, worldGenZ) == 0) 
							this.setBlock(worldGenX, worldGenY, worldGenZ, (byte) 2);
				}
			}
		}
		
		for (int worldGenZ = this.depth / 2; worldGenZ < this.depth; worldGenZ++) {
			for (int worldGenX = this.width / 2; worldGenX < this.width; worldGenX++) {
				this.setBlock(worldGenX, 0, worldGenZ, (byte)1);
			}
		}
		for (int worldGenZ = this.depth / 2; worldGenZ < this.depth; worldGenZ++) {
			for (int worldGenX = this.width / 2; worldGenX < this.width; worldGenX++) {
				this.setBlock(worldGenX, 1, worldGenZ, (byte)3);
			}
		}
		for (int worldGenZ = this.depth / 2; worldGenZ < this.depth; worldGenZ++) {
			for (int worldGenX = this.width / 2; worldGenX < this.width; worldGenX++) {
				this.setBlock(worldGenX, 2, worldGenZ, (byte)2);
			}
		}
	}
	
	/**
	 * Generates noise terrain into this world
	 */
	public void generateNoiseTerrain() {
		int maxTerrainHeight = this.height / 2;
		
		this.spawnX = (short) (this.width / 2);
		this.spawnY = (short) (maxTerrainHeight + 5);
		this.spawnZ = (short) (this.depth / 2);
		
		OpenSimplexNoise noise = new OpenSimplexNoise(ThreadLocalRandom.current().nextLong());
		double noiseScale = 50;
		double dirtStartY = 2;
		double waterEndY = 4;

		// Generate normal terrain
		for (int worldGenY = 0; worldGenY < maxTerrainHeight; worldGenY++) {
			for (int worldGenZ = 0; worldGenZ < this.depth; worldGenZ++) {
				for (int worldGenX = 0; worldGenX < this.width; worldGenX++) {
					double value = noise.eval(worldGenX / noiseScale, worldGenY / noiseScale, worldGenZ / noiseScale);
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
					
					this.setBlock(worldGenX, worldGenY, worldGenZ, blockID);
				}
			}
		}
		
		// Generate grass blocks
		for (int worldGenY = 0; worldGenY < maxTerrainHeight; worldGenY++) {
			for (int worldGenZ = 0; worldGenZ < this.depth; worldGenZ++) {
				for (int worldGenX = 0; worldGenX < this.width; worldGenX++) {
					if (worldGenY >= maxTerrainHeight / dirtStartY && 
						this.getBlock(worldGenX, worldGenY, worldGenZ) == 3)
						if (this.getBlock(worldGenX, worldGenY + 1, worldGenZ) == 0) 
							this.setBlock(worldGenX, worldGenY, worldGenZ, (byte) 2);
				}
			}
		}
	}
}
