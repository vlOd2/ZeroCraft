package net.fieme.zerocraft.console.builtin;

import net.fieme.zerocraft.Utils;
import net.fieme.zerocraft.console.ConsoleCaller;
import net.fieme.zerocraft.console.ConsoleCommand;
import net.fieme.zerocraft.game.WorldTile;
import net.fieme.zerocraft.game.WorldTiles;

public class PlaceCmd implements ConsoleCommand {
	@Override
	public String getName() {
		return "place";
	}

	@Override
	public String getDescription() {
		return "Makes you only place the specified block (to disable, use block 0)";
	}
	
	@Override
	public String getUsage() {
		return "place <name/id>";
	}
	
	@Override
	public int getMinArgsCount() {
		return 1;
	}

	@Override
	public int getMaxArgsCount() {
		return 1;
	}
	
	@Override
	public void execute(ConsoleCaller caller, String[] args) {
		if (!caller.isPlayer) {
			caller.sendMessage("&cOnly players are allowed to use this command!");
			return;
		}
		
		String blockName = args[0];
		WorldTile block = null;
		
		if (!Utils.isNumeric(blockName, false) || 
			Integer.valueOf(blockName) < Byte.MIN_VALUE || 
			Integer.valueOf(blockName) > Byte.MAX_VALUE) {
			block = WorldTiles.getFromName(blockName);
		} else {
			block = WorldTiles.getFromID(Byte.valueOf(blockName));
		}
	
		if (block == WorldTiles.air) {
			block = null;
		}
		
		caller.player.forcePlaceBlock = block;
		if (caller.player.forcePlaceBlock == null) {
			caller.sendMessage("&aYou are no longer placing only a specific block!");
		} else {
			caller.sendMessage("&aYou are now placing only block &e" + blockName + "&a!");
		}
	}
}
