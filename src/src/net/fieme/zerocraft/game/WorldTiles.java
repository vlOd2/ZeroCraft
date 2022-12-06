package net.fieme.zerocraft.game;

import java.lang.reflect.Field;

public class WorldTiles {
	public final static WorldTile air = new WorldTile(0);
	public final static WorldTile stone = new WorldTile(1);
	public final static WorldTile grass = new WorldTile(2);
	public final static WorldTile dirt = new WorldTile(3);
	public final static WorldTile cobblestone = new WorldTile(4);
	public final static WorldTile wood = new WorldTile(5);
	public final static WorldTile sappling = new WorldTile(6);
	public final static WorldTile bedrock = new WorldTile(7);
	public final static WorldTile water = new WorldTile(8);
	public final static WorldTile stillwater = new WorldTile(9);
	public final static WorldTile lava = new WorldTile(10);
	public final static WorldTile stilllava = new WorldTile(11);
	public final static WorldTile sand = new WorldTile(12);
	public final static WorldTile gravel = new WorldTile(13);
	public final static WorldTile goldore = new WorldTile(14);
	public final static WorldTile ironore = new WorldTile(15);
	public final static WorldTile coalore = new WorldTile(16);
	public final static WorldTile log = new WorldTile(17);
	public final static WorldTile leaves = new WorldTile(18);
	public final static WorldTile sponge = new WorldTile(19);
	public final static WorldTile glass = new WorldTile(20);
	public final static WorldTile red = new WorldTile(21);
	public final static WorldTile orange = new WorldTile(22);
	public final static WorldTile yellow = new WorldTile(23);
	public final static WorldTile lime = new WorldTile(24);
	public final static WorldTile green = new WorldTile(25);
	public final static WorldTile teal = new WorldTile(26);
	public final static WorldTile aqua = new WorldTile(27);
	public final static WorldTile cyan = new WorldTile(28);
	public final static WorldTile blue = new WorldTile(29);
	public final static WorldTile indigo = new WorldTile(30);
	public final static WorldTile violet = new WorldTile(31);
	public final static WorldTile magenta = new WorldTile(32);
	public final static WorldTile pink = new WorldTile(33);
	public final static WorldTile black = new WorldTile(34);
	public final static WorldTile gray = new WorldTile(35);
	public final static WorldTile white = new WorldTile(36);
	public final static WorldTile dandelion = new WorldTile(37);
	public final static WorldTile rose = new WorldTile(38);
	public final static WorldTile brownmushroom = new WorldTile(39);
	public final static WorldTile redmushroom = new WorldTile(40);
	public final static WorldTile gold = new WorldTile(41);
	public final static WorldTile iron = new WorldTile(42);
	public final static WorldTile doubleslab = new WorldTile(43);
	public final static WorldTile slab = new WorldTile(44);
	public final static WorldTile brick = new WorldTile(45);
	public final static WorldTile tnt = new WorldTile(46);
	public final static WorldTile bookshelf = new WorldTile(47);
	public final static WorldTile mossyrocks = new WorldTile(48);
	public final static WorldTile obsidian = new WorldTile(49);
	private final static WorldTile[] tiles = new WorldTile[] {
		air, stone, grass, dirt, cobblestone, wood, sappling, bedrock,
		water, stillwater, lava, stilllava, sand, gravel, goldore, ironore, coalore, log, leaves, sponge, glass,
		red, orange, yellow, lime, green, teal, aqua, cyan, blue, indigo, violet, magenta, pink, black, gray,
		dandelion, rose, brownmushroom, redmushroom, white, gold, iron, doubleslab, 
		slab, brick, tnt, bookshelf, mossyrocks, obsidian
	};
	private final static WorldTile[] legacyTiles = new WorldTile[] {
		air, stone, grass, dirt, cobblestone, wood, sappling,
		water, stillwater, lava, stilllava, sand, gravel, gold, log, leaves, sponge, glass,
		red, orange, yellow, lime, green, teal, aqua, cyan, blue, indigo, violet, magenta, pink, black, gray,
		dandelion, rose, brownmushroom, redmushroom, white
	};

	public static WorldTile getFromID(byte id) {
	    for (WorldTile worldTile : tiles) {
	        if (worldTile.id == id)
	            return worldTile;
	    }

	    return air;
	}
	
	public static WorldTile getFromIDLegacy(byte id) {
	    for (WorldTile worldTile : legacyTiles) {
	        if (worldTile.id == id)
	            return worldTile;
	    }
	    
	    for (WorldTile worldTile : tiles) {
	        if (worldTile.id == id)
	            return stone;
	    }

	    return air;
	}
	
	public static WorldTile getFromName(String name) {
		for (Field field : WorldTiles.class.getDeclaredFields()) {
			if (field.getClass().isArray() || field.getType() != WorldTile.class) continue;
			
			if (field.getName().equalsIgnoreCase(name)) {
				try {
					return (WorldTile) field.get(null);
				} catch (Exception ex) {
					ex.printStackTrace();
					return air;
				}
			}
		}
	    return air;
	}
}
