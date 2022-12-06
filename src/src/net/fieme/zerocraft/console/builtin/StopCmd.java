package net.fieme.zerocraft.console.builtin;

import net.fieme.zerocraft.ZeroCraft;
import net.fieme.zerocraft.console.ConsoleCaller;
import net.fieme.zerocraft.console.ConsoleCommand;

public class StopCmd implements ConsoleCommand {
	@Override
	public String getName() {
		return "stop";
	}

	@Override
	public String getDescription() {
		return "Stops the server";
	}

	@Override
	public String getRequiredPermission() {
		return "zerocraft.admin.stop";
	}
	
	@Override
	public String getUsage() {
		return "stop";
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
	}
}
