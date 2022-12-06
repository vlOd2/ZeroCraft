package net.fieme.zerocraft.console.builtin;

import net.fieme.zerocraft.Utils;
import net.fieme.zerocraft.ZeroCraft;
import net.fieme.zerocraft.configuration.BannedConfig;
import net.fieme.zerocraft.console.ConsoleCaller;
import net.fieme.zerocraft.console.ConsoleCommand;

public class UnbanCmd implements ConsoleCommand {
	@Override
	public String getName() {
		return "unban";
	}

	@Override
	public String getDescription() {
		return "Unbans the specified player/ip";
	}

	@Override
	public String getRequiredPermission() {
		return "zerocraft.admin.unban";
	}
	
	@Override
	public String getUsage() {
		return "unban <player/ip>";
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

		caller.sendMessage("&aUnbanning &e" + name + "&b...");
		
		if (Utils.isIPv4Address(name)) {
			if (BannedConfig.instance.ipaddresses.get(name) != null) {
				BannedConfig.instance.ipaddresses.remove(name);
				foundEntry = true;
			}
			ZeroCraft.instance.saveConfigs();
		} else {
			if (BannedConfig.instance.users.get(name) != null) {
				BannedConfig.instance.users.remove(name);
				foundEntry = true;
			}
			ZeroCraft.instance.saveConfigs();
		}
		
		if (foundEntry) {
			caller.sendMessage("&aUnbanned &e" + name);
		} else {
			caller.sendMessage("&cUnable to find &e" + name + "&c in the ban list!");
		}
	}
}
