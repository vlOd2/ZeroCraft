package net.fieme.zerocraft.console.builtin;

import net.fieme.zerocraft.ZeroCraft;
import net.fieme.zerocraft.console.ConsoleCaller;
import net.fieme.zerocraft.console.ConsoleCommand;
import net.fieme.zerocraft.game.EntityPlayer;
import net.fieme.zerocraft.game.World;

public class WorldsCmd implements ConsoleCommand {
	@Override
	public String getName() {
		return "worlds";
	}

	@Override
	public String getDescription() {
		return "Lists the available worlds and their players";
	}

	@Override
	public String getUsage() {
		return "worlds";
	}

	@Override
	public int getMinArgsCount() {
		return 0;
	}

	@Override
	public int getMaxArgsCount() {
		return 0;
	}

	@Override
	public void execute(ConsoleCaller caller, String[] args) throws Exception {
		EntityPlayer[] players = ZeroCraft.instance.players.toArray(new EntityPlayer[0]);
		
		String playersStr = "";
		for (World world : ZeroCraft.instance.worldManager.worlds.toArray(new World[0])) {
			if (players.length > 0) {
				playersStr = "";
				for (EntityPlayer player : players) {
					if (player.world != world) continue;
					playersStr += " " + player.name;
				}
				playersStr = playersStr.trim();
			}
			caller.sendMessage("&b" + world.name + "&a:&f " + playersStr);
		}
	}
}
