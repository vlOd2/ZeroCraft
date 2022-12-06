package net.fieme.zerocraft.console.builtin;

import java.util.HashMap;

import net.fieme.zerocraft.Utils;
import net.fieme.zerocraft.ZeroCraft;
import net.fieme.zerocraft.configuration.MessagesConfig;
import net.fieme.zerocraft.configuration.ServerConfig;
import net.fieme.zerocraft.console.ConsoleCaller;
import net.fieme.zerocraft.console.ConsoleCommand;
import net.fieme.zerocraft.game.World;
import net.fieme.zerocraft.game.generator.EmptyWorldGenerator;
import net.fieme.zerocraft.game.generator.FarLandsWorldGenerator;
import net.fieme.zerocraft.game.generator.FlatWorldGenerator;
import net.fieme.zerocraft.game.generator.AlphaWorldGenerator;
import net.fieme.zerocraft.game.generator.SimplexWorldGenerator;
import net.fieme.zerocraft.game.generator.WorldGenerator;

public class ManageWorldCmd implements ConsoleCommand {
	private static final HashMap<String, WorldGenerator> worldGeneratorMap =
			new HashMap<String, WorldGenerator>();
	
	static {
		worldGeneratorMap.put("flat", new FlatWorldGenerator());
		worldGeneratorMap.put("simplex", new SimplexWorldGenerator());
		worldGeneratorMap.put("farlands", new FarLandsWorldGenerator());
		worldGeneratorMap.put("empty", new EmptyWorldGenerator());
		worldGeneratorMap.put("alpha", new AlphaWorldGenerator());
	}
	
	@Override
	public String getName() {
		return "manageworld";
	}

	@Override
	public String[] getAliases() {
		return new String[] { "mw", "worldmanager", "wm" };
	}
	
	@Override
	public String getDescription() {
		return "Manages the specified world";
	}

	@Override
	public String getRequiredPermission() {
		return "zerocraft.admin.manageworld";
	}
	
	@Override
	public String getUsage() {
		return "manageworld <load/unload/create/delete/setspawn> <name> [width/x] [height/y] [depth/z] [flat/simplex/farlands/empty/perlin]";
	}

	@Override
	public int getMinArgsCount() {
		return 2;
	}

	@Override
	public int getMaxArgsCount() {
		return 6;
	}

	// TODO: Multithreading support (to prevent server lock ups)
	@Override
	public void execute(ConsoleCaller caller, String[] args) throws Exception {
		String action = args[0];
		String worldName = args[1];
		World world = ZeroCraft.instance.worldManager.getWorldByName(worldName);

		if (worldName.equalsIgnoreCase(ServerConfig.instance.mainWorld) && 
			!action.equalsIgnoreCase("setspawn")) {
			caller.sendMessage("&cYou may not perform actions on the main world!");
			return;
		}
		if (!action.equalsIgnoreCase("create") && 
			!action.equalsIgnoreCase("delete") && 
			!action.equalsIgnoreCase("load") && 
			!action.equalsIgnoreCase("unload") &&
			!action.equalsIgnoreCase("setspawn")) {
			caller.sendMessage(MessagesConfig.instance.feedbackInvalidArguments);
			return;
		}
		if (world == null && (action.equalsIgnoreCase("delete") || 
				action.equalsIgnoreCase("unload") || 
				action.equalsIgnoreCase("setspawn"))) {
			caller.sendMessage("&cThe specified action requires a loaded world!");
			return;
		}
		if (world != null && (action.equalsIgnoreCase("create") || action.equalsIgnoreCase("load"))) {
			caller.sendMessage("&cThe specified world already exists and is loaded!");
			return;
		}
		
		if (action.equalsIgnoreCase("create")) {
			short worldWidth = 128;
			short worldHeight = 256;
			short worldDepth = 128;
			WorldGenerator worldGenerator = new FlatWorldGenerator();
			
			try {
				if (args.length > 2) {
					if (args.length > 2) {
						String worldWidthRaw = args[2];
						if (!Utils.isNumeric(worldWidthRaw, false)) throw new Exception();
						worldWidth = Short.valueOf(worldWidthRaw);
					}
					
					if (args.length > 3) {
						String worldHeightRaw = args[3];
						if (!Utils.isNumeric(worldHeightRaw, false)) throw new Exception();
						worldHeight = Short.valueOf(worldHeightRaw);
					}
					
					if (args.length > 4) {
						String worldDepthRaw = args[4];
						if (!Utils.isNumeric(worldDepthRaw, false)) throw new Exception();
						worldDepth = Short.valueOf(worldDepthRaw);
					}
					
					if (args.length > 5) {
						String worldGeneratorRaw = args[5].toLowerCase();
						if (!worldGeneratorMap.containsKey(worldGeneratorRaw)) throw new Exception();
						worldGenerator = worldGeneratorMap.get(worldGeneratorRaw);
					}
				}	
			} catch (Exception ex) {
				caller.sendMessage(MessagesConfig.instance.feedbackInvalidArguments);
				return;
			}
			
			caller.sendMessage("&aCreating the world &e" + worldName + "&a...");
			try {
				world = ZeroCraft.instance.worldManager.generateWorld(worldName, 
						worldWidth, worldHeight, worldDepth, worldGenerator);	
			} catch (Exception ex) {
				caller.sendMessage("&cUnable to generate the world: " + ex.getMessage());
				return;
			}
			ZeroCraft.instance.worldManager.addWorld(world);
			ZeroCraft.instance.worldManager.saveWorld(world.name);
			caller.sendMessage("&aCreated the world &e" + worldName);
		} else if (action.equalsIgnoreCase("delete")) {
			caller.sendMessage("&aDeleting the world &e" + worldName + "&a...");
			ZeroCraft.instance.worldManager.deleteWorld(worldName);
			caller.sendMessage("&aDeleted the world &e" + worldName);
		} else if (action.equalsIgnoreCase("load")) {
			caller.sendMessage("&aLoading the world &e" + worldName + "&a...");
			boolean result = ZeroCraft.instance.worldManager.loadWorld(worldName, true);
			
			if (result) {
				caller.sendMessage("&aLoaded the world &e" + worldName);
			} else {
				caller.sendMessage("&cUnable to load the world &e" + worldName + "&c!");
			}
		} else if (action.equalsIgnoreCase("unload")) {
			caller.sendMessage("&aUnloading the world &e" + worldName + "&a...");
			ZeroCraft.instance.worldManager.unloadWorld(worldName);
			caller.sendMessage("&aUnloaded the world &e" + worldName + "&a...");
		} else if (action.equalsIgnoreCase("setspawn")) {
			short spawnX = 0;
			short spawnY = 0;
			short spawnZ = 0;
			byte spawnYaw = 0;
			byte spawnPitch = 0;
			
			if (!caller.isPlayer && args.length < 5) {
				caller.sendMessage("&cYou may only set the spawnpoint using a full position!");
				return;
			}
			
			if (args.length >= 5) {
				try {
					String spawnXRaw = args[2];
					String spawnYRaw = args[3];
					String spawnZRaw = args[4];
					spawnX = Short.valueOf(spawnXRaw);
					spawnY = Short.valueOf(spawnYRaw);
					spawnZ = Short.valueOf(spawnZRaw);
				} catch (Exception ex) {
					caller.sendMessage(MessagesConfig.instance.feedbackInvalidArguments);
					return;
				}
			} else {
				spawnX = (short) (caller.player.posX >> 5);
				spawnY = (short) (caller.player.posY >> 5);
				spawnZ = (short) (caller.player.posZ >> 5);
				spawnYaw = caller.player.yaw;
				spawnPitch = caller.player.pitch;
			}
			
			world.spawnX = spawnX;
			world.spawnY = spawnY;
			world.spawnZ = spawnZ;
			world.spawnYaw = spawnYaw;
			world.spawnPitch = spawnPitch;
			
			caller.sendMessage("&aSet the world spawn to &eX:" + spawnX + " Y:" + spawnY + " Z:" + spawnZ);
			ZeroCraft.instance.worldManager.saveWorld(worldName);
		}
	}
}
