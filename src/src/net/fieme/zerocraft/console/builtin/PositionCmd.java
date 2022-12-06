package net.fieme.zerocraft.console.builtin;

import net.fieme.zerocraft.console.ConsoleCaller;
import net.fieme.zerocraft.console.ConsoleCommand;
import net.fieme.zerocraft.game.EntityPlayer;

public class PositionCmd implements ConsoleCommand {
	@Override
	public String getName() {
		return "position";
	}

	@Override
	public String[] getAliases() {
		return new String[] { "pos", "whereami", "location", "loc" };
	}
	
	@Override
	public String getDescription() {
		return "Shows your current position";
	}
	
	@Override
	public String getUsage() {
		return "position";
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
		if (!caller.isPlayer) {
			caller.sendMessage("&cOnly players are allowed to use this command!");
			return;
		}
		
		EntityPlayer player = caller.player;
		caller.sendMessage("&aYour raw position &eX:" + player.posX + " Y:" + player.posY + " Z:" + player.posZ);
		caller.sendMessage("&aYour position &eX:" + (player.posX >> 5) + " Y:" + (player.posY >> 5) + " Z:" + (player.posZ >> 5));
	}
}
