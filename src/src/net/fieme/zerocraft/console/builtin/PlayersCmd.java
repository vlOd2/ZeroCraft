package net.fieme.zerocraft.console.builtin;

import net.fieme.zerocraft.ZeroCraft;
import net.fieme.zerocraft.console.ConsoleCaller;
import net.fieme.zerocraft.console.ConsoleCommand;
import net.fieme.zerocraft.game.EntityPlayer;

public class PlayersCmd implements ConsoleCommand {
	@Override
	public String getName() {
		return "players";
	}

	@Override
	public String[] getAliases() {
		return new String[] { "list", "online", "people", "playerlist", "listplayers" };
	}
	
	@Override
	public String getDescription() {
		return "Lists the connected players";
	}

	@Override
	public String getUsage() {
		return "players";
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
	public void execute(ConsoleCaller caller, String[] args) {
		EntityPlayer[] players = ZeroCraft.instance.players.toArray(new EntityPlayer[0]);
		caller.sendMessage("&aThere are &b" + players.length + "&a players online:");
		
		if (players.length > 0) {
			String playersStr = "";
			for (EntityPlayer player : players) {
				playersStr += " " + player.name;
			}
			playersStr = playersStr.trim();
			
			caller.sendMessage(playersStr);	
		}
	}
}
