package net.fieme.zerocraft.game;

public class WorldTile {
	public final byte id;
	
	public WorldTile(byte id) {
		this.id = id;
	}
	
	public WorldTile(int id) {
		this.id = (byte)id;
	}
}
