package net.fieme.zerocraft.game;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

import net.fieme.zerocraft.Utils;
import net.fieme.zerocraft.ZeroCraft;
import net.fieme.zerocraft.game.generator.FlatWorldGenerator;
import net.fieme.zerocraft.game.generator.WorldGenerator;
import net.fieme.zerocraft.logging.Logging;

public class WorldManager {
	private ZeroCraft serverInstance;
	public final ArrayList<World> worlds = new ArrayList<World>();
	
	public WorldManager(ZeroCraft serverInstance) {
		this.serverInstance = serverInstance;
	}

	/**
	 * Generates a specified level using the specified options
	 * 
	 * @param name the name
	 * @param width the width
	 * @param height the height
	 * @param depth the depth
	 * @param generator the world generator
	 * @return the level
	 * @throws Exception when an error has occured during generation
	 */
	public World generateWorld(String name, short width, 
			short height, short depth, WorldGenerator generator) throws Exception {
		Logging.logInfo("Generating world \"" + name + "\"...");
		World world = new World(this.serverInstance, name, width, height, depth);
		generator.generateWorld(world);
		Logging.logInfo("Generated world \"" + name + "\"");
		
		return world;
	}
	
	/**
	 * Gets a world by it's name
	 *
	 * @param name the name
	 * @return the world or null
	 */
	public World getWorldByName(String name) {
		for (World world : this.worlds.toArray(new World[0])) {
			if (world.name.equalsIgnoreCase(name))
				return world;
		}
		
		return null;
	}
	
	/**
	 * Adds an world
	 * 
	 * @param world the world
	 */
	public void addWorld(World world) {
		if (world == null) return;
		if (this.getWorldByName(world.name) != null) return;
		this.worlds.add(world);
		world.onWorldLoad();
	}
	
	/**
	 * Removes an world
	 * 
	 * @param name the name of the world
	 */
	public void removeWorld(World world) {
		if (world == null) return;
		this.worlds.remove(world);
		world.onWorldUnload();
	}
	
	/**
	 * Saves the specified world
	 * 
	 * @param name the name of the world
	 */
	public void saveWorld(String name) {
		long startTime = System.currentTimeMillis();
		World world = this.getWorldByName(name);
		if (world == null) return;
		
		Logging.logInfo("Saving world " + name + "...");
		this.saveWorldToFile(world);
		Logging.logInfo("Saved world " + name + " in " + (System.currentTimeMillis() - startTime) + "ms");
	}
	
	/**
	 * Loads the specified world<br>
	 * If the world does not exist, it is generated<br>
	 * If the world is already loaded, it will be unloaded
	 * 
	 * @param name the name of the world
	 */
	public boolean loadWorld(String name, boolean doNotGenerate) {
		long startTime = System.currentTimeMillis();
		Logging.logInfo("Loading world " + name + "...");
		
		World world = this.getWorldByName(name);
		if (world != null) {
			this.unloadWorld(name);
		}
		
		world = this.loadWorldFromFile(name);
		if (world == null) {
			Logging.logWarn("Unable to find level " + name + "!");
			if (!doNotGenerate) {
				try {
					world = this.generateWorld(name, (short)128, (short)256, (short)128, 
							new FlatWorldGenerator());		
				} catch (Exception ex) {
					Logging.logError("Unable to generate the level " + name + ": " + ex.getMessage());
					return false;
				}
			} else {
				return false;
			}
		}
		
		this.addWorld(world);
		this.saveWorld(name);
		Logging.logInfo("Loaded world " + name + " in " + (System.currentTimeMillis() - startTime) + "ms");
		
		return true;
	}

	/**
	 * Unloads the specified world
	 * 
	 * @param name the name of the world
	 */
	public void unloadWorld(String name) {
		long startTime = System.currentTimeMillis();
		World world = this.getWorldByName(name);
		if (world == null) return;

		Logging.logInfo("Unloading world " + name + "...");
		this.saveWorld(name);
		this.removeWorld(world);
		Logging.logInfo("Unloaded world " + name + " in " + (System.currentTimeMillis() - startTime) + "ms");
	}
	
	/**
	 * Deletes the specified world
	 * 
	 * @param name the name of the world
	 */
	public void deleteWorld(String name) {
		long startTime = System.currentTimeMillis();
		World world = this.getWorldByName(name);
		if (world == null) return;
		
		Logging.logInfo("Deleting world " + name + "...");
		this.unloadWorld(name);
		try {
			File file = new File(name.toLowerCase() + ".world");
			if (file.exists()) {
				file.delete();
			} else {
				Logging.logWarn("Unable to find file for world " + name + "!");
			}
		} catch (Exception ex) {
			Logging.logError("Unable to delete world " + name + ": " + 
					Utils.getExceptionStackTraceAsStr(ex));
		}
		Logging.logInfo("Deleted world " + name + " in " + (System.currentTimeMillis() - startTime) + "ms");
	}
	
	/**
	 * Unloads all loaded worlds
	 */
	public void unloadWorlds() {
		for (World world : this.worlds.toArray(new World[0])) {
			this.unloadWorld(world.name);
		}
	}
	
	private World loadWorldFromFile(String name) {
		try {
			File file = new File(name.toLowerCase() + ".world");
			
			if (!file.exists()) {
				return null;
			}
			
			FileInputStream fileInputStream = new FileInputStream(file);
			ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
			World world = (World) objectInputStream.readObject();
			world.func_0000(this.serverInstance);
			
			objectInputStream.close();
			fileInputStream.close();
			
			return world;	
		} catch (Exception ex) {
			return null;
		}
	}

	private void saveWorldToFile(World world) {
		try {
			File file = new File(world.name.toLowerCase() + ".world");
			if (!file.exists()) file.createNewFile();
			
			FileOutputStream fileOutputStream = new FileOutputStream(file);
			ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
			
			objectOutputStream.writeObject(world);
			objectOutputStream.flush();	
			
			objectOutputStream.close();
			fileOutputStream.close();	
		} catch (Exception ex) {
		}
	}
}
