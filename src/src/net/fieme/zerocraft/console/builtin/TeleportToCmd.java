package net.fieme.zerocraft.console.builtin;

import net.fieme.zerocraft.ZeroCraft;
import net.fieme.zerocraft.console.ConsoleCaller;
import net.fieme.zerocraft.console.ConsoleCommand;
import net.fieme.zerocraft.game.EntityPlayer;

public class TeleportToCmd implements ConsoleCommand {
	@Override
	public String getName() {
		return "teleportto";
	}

	@Override
	public String[] getAliases() {
		return new String[] { "tpto", "moveto" };
	}
	
	@Override
	public String getDescription() {
		return "Teleports you to the specified player";
	}
	
	@Override
	public String getUsage() {
		return "teleportto <playername>";
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
		
		String targetPlayerName = args[0];
		EntityPlayer player = caller.player;
		EntityPlayer targetPlayer = ZeroCraft.instance.getPlayerByName(targetPlayerName);

		if (targetPlayer == null) {
			caller.sendMessage("&cUnable to find the player &e" + targetPlayerName + "&c!");
		} else {
			if (targetPlayer.world != player.world) {
				player.switchWorld(targetPlayer.world);
			}
			player.teleportRaw(targetPlayer.posX, targetPlayer.posY, targetPlayer.posZ, 
					targetPlayer.yaw, targetPlayer.pitch);
			caller.sendMessage("&aTeleported you to &e" + targetPlayerName);
		}
	}
}
