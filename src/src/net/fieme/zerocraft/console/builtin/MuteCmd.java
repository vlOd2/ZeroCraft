package net.fieme.zerocraft.console.builtin;

import net.fieme.zerocraft.ZeroCraft;
import net.fieme.zerocraft.configuration.MessagesConfig;
import net.fieme.zerocraft.configuration.MutedConfig;
import net.fieme.zerocraft.console.ConsoleCaller;
import net.fieme.zerocraft.console.ConsoleCommand;

public class MuteCmd implements ConsoleCommand {
	@Override
	public String getName() {
		return "mute";
	}

	@Override
	public String getDescription() {
		return "Mutes the specified player";
	}

	@Override
	public String getRequiredPermission() {
		return "zerocraft.admin.mute";
	}
	
	@Override
	public String getUsage() {
		return "mute <player> <reason>";
	}

	@Override
	public int getMinArgsCount() {
		return 2;
	}

	@Override
	public int getMaxArgsCount() {
		return 2;
	}

	@Override
	public void execute(ConsoleCaller caller, String[] args) throws Exception {
		String name = args[0];
		String reason = args[1];
		
		caller.sendMessage("&aMuting &e" + name + "&b...");
		MutedConfig.instance.users.put(name, reason);
		ZeroCraft.instance.saveConfigs();
		ZeroCraft.instance.sendGlobalChatMessage(MessagesConfig.instance.playerMute
				.replace("%player%", name));
		caller.sendMessage("&aMuted &e" + name);
	}
}
