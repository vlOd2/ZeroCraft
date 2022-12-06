package net.fieme.zerocraft.console.builtin;

import net.fieme.zerocraft.ZeroCraft;
import net.fieme.zerocraft.console.ConsoleCaller;
import net.fieme.zerocraft.console.ConsoleCommand;

public class RestartCmd implements ConsoleCommand {
	@Override
	public String getName() {
		return "restart";
	}

	@Override
	public String getDescription() {
		return "Restarts the server";
	}
	
	@Override
	public String getRequiredPermission() {
		return "zerocraft.admin.restart";
	}
	
	@Override
	public String getUsage() {
		return "restart";
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
		if (ZeroCraft.instance == null || !ZeroCraft.instance.isRunning)
			return;
		ZeroCraft.instance.stop();
		ZeroCraft.instance.start();
	}
}
