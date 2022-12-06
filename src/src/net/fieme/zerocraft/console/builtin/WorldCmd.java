package net.fieme.zerocraft.console.builtin;

import net.fieme.zerocraft.ZeroCraft;
import net.fieme.zerocraft.console.ConsoleCaller;
import net.fieme.zerocraft.console.ConsoleCommand;
import net.fieme.zerocraft.game.EntityPlayer;

public class WorldCmd implements ConsoleCommand {
	@Override
	public String getName() {
		return "world";
	}

	@Override
	public String getDescription() {
		return "Switches the world you are currently on";
	}


	@Override
	public String getUsage() {
		return "world <name>";
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
		
		EntityPlayer player = caller.player;
		String worldName = args[0];
		
		caller.sendMessage("&aSwitching world to &e" + worldName + "&a...");
		boolean result = ZeroCraft.instance.worldManager.getWorldByName(worldName) != null || 
				ZeroCraft.instance.worldManager.loadWorld(worldName, true);
		
		if (result) {
			player.switchWorld(ZeroCraft.instance.worldManager.getWorldByName(worldName));
			caller.sendMessage("&aSwitched world to &e" + worldName);
		} else {
			caller.sendMessage("&cUnable to find the world &e" + worldName + "&c!");
		}
	}
}
