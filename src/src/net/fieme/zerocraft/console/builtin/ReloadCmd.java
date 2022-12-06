package net.fieme.zerocraft.console.builtin;

import net.fieme.zerocraft.ZeroCraft;
import net.fieme.zerocraft.console.ConsoleCaller;
import net.fieme.zerocraft.console.ConsoleCommand;

public class ReloadCmd implements ConsoleCommand {
	@Override
	public String getName() {
		return "reload";
	}

	@Override
	public String getDescription() {
		return "Reloads the server configs (plugins aren't reloaded)";
	}
	
	@Override
	public String getRequiredPermission() {
		return "zerocraft.admin.reload";
	}
	
	@Override
	public String getUsage() {
		return "reload";
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
		caller.sendMessage("&aReloading the &eserver&a...");
		ZeroCraft.instance.reloadConfigs();
		caller.sendMessage("&aReloaded the &eserver");
	}
}
