package net.fieme.zerocraft.console.builtin;

import net.fieme.zerocraft.InlineForloop;
import net.fieme.zerocraft.Utils;
import net.fieme.zerocraft.ZeroCraft;
import net.fieme.zerocraft.console.ConsoleCaller;
import net.fieme.zerocraft.console.ConsoleCommand;
import net.fieme.zerocraft.game.EntityPlayer;

public class KickCmd implements ConsoleCommand {
	@Override
	public String getName() {
		return "kick";
	}

	@Override
	public String getDescription() {
		return "Kicks the specified player";
	}

	@Override
	public String getRequiredPermission() {
		return "zerocraft.admin.kick";
	}
	
	@Override
	public String getUsage() {
		return "kick <player> [reason]";
	}

	@Override
	public int getMinArgsCount() {
		return 1;
	}

	@Override
	public int getMaxArgsCount() {
		return 2;
	}

	@Override
	public void execute(ConsoleCaller caller, String[] args) throws Exception {
		String name = args[0];
		String reason = args.length > 1 ? args[1] : null;
		
		caller.sendMessage("&aKicking &e" + name + "&b...");

		if (Utils.isIPv4Address(name)) {
			ZeroCraft.instance.foreachPlayer(new InlineForloop<EntityPlayer>() {
				@Override
				public void onEntry(EntityPlayer entry) {
					if (entry.client.identifier.networkAddress.equals(name)) {
						entry.kick(reason);
					}
				}
			});
		} else {
			EntityPlayer player = ZeroCraft.instance.getPlayerByName(name);
			if (player == null) {
				caller.sendMessage("&cUnable to find the player &e" + name + "&c!");
				return;
			} else {
				player.kick(reason);
			}
		}
		
		caller.sendMessage("&aKicked &e" + name);
	}
}
