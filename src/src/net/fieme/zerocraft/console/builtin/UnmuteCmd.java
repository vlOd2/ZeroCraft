package net.fieme.zerocraft.console.builtin;

import net.fieme.zerocraft.ZeroCraft;
import net.fieme.zerocraft.configuration.MessagesConfig;
import net.fieme.zerocraft.configuration.MutedConfig;
import net.fieme.zerocraft.console.ConsoleCaller;
import net.fieme.zerocraft.console.ConsoleCommand;

public class UnmuteCmd implements ConsoleCommand {
	@Override
	public String getName() {
		return "unmute";
	}

	@Override
	public String getDescription() {
		return "Unmutes the specified player";
	}

	@Override
	public String getRequiredPermission() {
		return "zerocraft.admin.unmute";
	}
	
	@Override
	public String getUsage() {
		return "unmute <player>";
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
	public void execute(ConsoleCaller caller, String[] args) throws Exception {
		String name = args[0];
		boolean foundEntry = false;
		
		caller.sendMessage("&aUnmuting &e" + name + "&b...");
		
		if (MutedConfig.instance.users.get(name) != null) {
			MutedConfig.instance.users.remove(name);
			foundEntry = true;
		}
		ZeroCraft.instance.saveConfigs();
		
		if (foundEntry) {
			ZeroCraft.instance.sendGlobalChatMessage(MessagesConfig.instance.playerUnmute
					.replace("%player%", name));
			caller.sendMessage("&aUnmuted &e" + name);
		} else {
			caller.sendMessage("&cUnable to find &e" + name + "&c in the muted list!");
		}
	}
}
