package net.fieme.zerocraft.game;

import java.io.Serializable;
import java.util.ArrayList;

import net.fieme.zerocraft.InlineForloop;
import net.fieme.zerocraft.ZeroCraft;

/**
 * A in-game world
 */
public class World implements Serializable {
	private static final long serialVersionUID = 8105344403903580546L;
	private transient ZeroCraft serverInstance;
	public final String name;
	public final short width;
	public final short height;
	public final short depth;
	public short spawnX;
	public short spawnY;
	public short spawnZ;
	public byte spawnYaw;
	public byte spawnPitch;
	private byte[][][] blocks;
	private transient ArrayList<Entity> entities = new ArrayList<Entity>();
	
	/**
	 * Creates a new world using the specified size
	 * 
	 * @param width the width
	 * @param height the height
	 * @param depth the depth
	 */
	public World(ZeroCraft serverInstance, String name, 
			short width, short height, short depth) {
		this.serverInstance = serverInstance;
		this.name = name;
		this.width = width;
		this.height = height;
		this.depth = depth;
		this.blocks = new byte[width][height][depth];
	}

    /**
     * NOTE: Not intended to be used function
     */
	public void func_0000(ZeroCraft arg0) {
		this.serverInstance = arg0;
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
	 * Sets the ID of the block at the specified position
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
		
		if (this.entities.size() > 0) {
			this.serverInstance.foreachPlayer(new InlineForloop<EntityPlayer>() {
				@Override
				public void onEntry(EntityPlayer entry) {
					if (entities.contains(entry)) {
						entry.packetHandler.sendBlock(x, y, z, block);
					}
				}
			});	
		}
	}
	
	/**
	 * Sets the ID of the block at the specified position with notify
	 * 
	 * @param x block x
	 * @param y block y
	 * @param z block z
	 * @throws IllegalArgumentException if the block position is invalid
	 * @param block new block ID
	 */
	public void setBlockWithNotify(int x, int y, int z, byte block) {
		this.setBlock(x, y, z, block);
		
		if (block == WorldTiles.water.id || block == WorldTiles.lava.id ||
			block == WorldTiles.stillwater.id || block == WorldTiles.stilllava.id) {
			if (block == WorldTiles.water.id || block == WorldTiles.lava.id) {
				int x2 = x - 1;
				int z2 = z - 1;
				if (this.isValidBlockPos(x2, y, z2)) {
					this.setBlock(x2, y, z2, block);
				}
				
				x2 = x + 1;
				z2 = z + 1;
				if (this.isValidBlockPos(x2, y, z2)) {
					this.setBlock(x2, y, z2, block);
				}
				
				x2 = x + 1;
				z2 = z;
				if (this.isValidBlockPos(x2, y, z2)) {
					this.setBlock(x2, y, z2, block);
				}
				
				x2 = x;
				z2 = z + 1;
				if (this.isValidBlockPos(x2, y, z2)) {
					this.setBlock(x2, y, z2, block);
				}
				
				x2 = x - 1;
				z2 = z;
				if (this.isValidBlockPos(x2, y, z2)) {
					this.setBlock(x2, y, z2, block);
				}
				
				x2 = x;
				z2 = z - 1;
				if (this.isValidBlockPos(x2, y, z2)) {
					this.setBlock(x2, y, z2, block);
				}
				
				x2 = x + 1;
				z2 = z - 1;
				if (this.isValidBlockPos(x2, y, z2)) {
					this.setBlock(x2, y, z2, block);
				}
				
				x2 = x - 1;
				z2 = z + 1;
				if (this.isValidBlockPos(x2, y, z2)) {
					this.setBlock(x2, y, z2, block);
				}	
			}
		} else {
			if (block == WorldTiles.sand.id || block == WorldTiles.gravel.id) {
				this.setBlock(x, y, z, WorldTiles.air.id);

				for (int physicY = y; y > 0; physicY--) {
					byte blockAtPhysic = this.getBlock(x, physicY, z);
					if (blockAtPhysic != WorldTiles.air.id) {
						this.setBlock(x, physicY + 1, z, block);
						break;
					}
				}
			}
			
			if (y > 0) {
				if (block == WorldTiles.air.id && 
					this.getBlock(x, y - 1, z) == WorldTiles.dirt.id) {
					this.setBlock(x, y - 1, z, WorldTiles.grass.id);
				}
				
				if (block != WorldTiles.air.id && 
					this.getBlock(x, y - 1, z) == WorldTiles.grass.id) {
					this.setBlock(x, y - 1, z, WorldTiles.dirt.id);
				}
			}
			
			if (block == WorldTiles.dirt.id) {
				if (y + 1 < this.height) {
					if (this.getBlock(x, y + 1, z) == WorldTiles.air.id) {
						this.setBlock(x, y, z, WorldTiles.grass.id);
					}	
				} else {
					this.setBlock(x, y, z, WorldTiles.grass.id);
				}
			}	
		}
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
						x <= (this.width * -1) || 
						y <= (this.height * -1) || 
						z <= (this.depth * -1)
				) || (
						x >= (this.width) || 
						y >= (this.height) || 
						z >= (this.depth)
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
	 * Gets this world as 1D array for legacy clients
	 *
	 * @return the byte array
	 */
	public byte[] getAsArrayLegacy() {
		byte[] data = new byte[this.width * this.height * this.depth];
		
		for (int y = 0; y < this.height; y++) {
			for (int z = 0; z < this.depth; z++) {
				for (int x = 0; x < this.width; x++) {
					data[getWorldIndexInArray(x, y, z)] = (byte)WorldTiles.getFromIDLegacy(this.blocks[x][y][z]).id;
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
	
	public void onWorldLoad() {
		
	}
	
	public void onWorldUnload() {
		for (Entity entity : this.entities.toArray(new Entity[0])) {
			this.removeEntity(entity);
			if (entity instanceof EntityPlayer) {
				EntityPlayer player = (EntityPlayer) entity;
				player.kick("&cThe world you were connected to was unloaded!");
			}
		}
	}
}
