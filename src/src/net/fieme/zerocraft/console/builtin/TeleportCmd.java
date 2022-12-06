package net.fieme.zerocraft.console.builtin;

import net.fieme.zerocraft.configuration.MessagesConfig;
import net.fieme.zerocraft.console.ConsoleCaller;
import net.fieme.zerocraft.console.ConsoleCommand;
import net.fieme.zerocraft.game.EntityPlayer;

public class TeleportCmd implements ConsoleCommand {
	@Override
	public String getName() {
		return "teleport";
	}

	@Override
	public String[] getAliases() {
		return new String[] { "tp", "move" };
	}
	
	@Override
	public String getDescription() {
		return "Teleports you to the specified position";
	}
	
	@Override
	public String getUsage() {
		return "teleport <x> <y> <z> [precise (true to use)]";
	}
	
	@Override
	public int getMinArgsCount() {
		return 3;
	}

	@Override
	public int getMaxArgsCount() {
		return 4;
	}

	@Override
	public void execute(ConsoleCaller caller, String[] args) {
		if (!caller.isPlayer) {
			caller.sendMessage("&cOnly players are allowed to use this command!");
			return;
		}
		
		EntityPlayer player = caller.player;
		
		String xPosRaw = args[0];
		String yPosRaw = args[1];
		String zPosRaw = args[2];
		short xPos = 0;
		short yPos = 0;
		short zPos = 0;
		
		try {
			xPos = Short.valueOf(xPosRaw);
			yPos = Short.valueOf(yPosRaw);
			zPos = Short.valueOf(zPosRaw);	
		} catch (Exception ex) {
			caller.sendMessage(MessagesConfig.instance.feedbackInvalidArguments);
			return;
		}

		if (args.length > 3 && args[3].equalsIgnoreCase("true"))
			player.teleportRaw(xPos, yPos, zPos, player.yaw, player.pitch);
		else
			player.teleport(xPos, (short)(yPos + 1), zPos, player.yaw, player.pitch);
		
		caller.sendMessage("&aTeleported you to &eX:" + xPos + " Y:" + yPos + " Z:" + zPos);
	}
}
