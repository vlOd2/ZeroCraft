package net.fieme.zerocraft.console.builtin;

import net.fieme.zerocraft.ZeroCraft;
import net.fieme.zerocraft.console.ConsoleCaller;
import net.fieme.zerocraft.console.ConsoleCommand;

public class ClearCmd implements ConsoleCommand {
	@Override
	public String getName() {
		return "clear";
	}

	@Override
	public String getDescription() {
		return "Clears the chat (local)";
	}
	
	@Override
	public String getUsage() {
		return "clear";
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
		for (int i = 0; i < 100; i++) {
			caller.sendMessage(" ");
		}
		    
		if (!caller.isPlayer && ZeroCraft.instance.windowMain != null) {
			ZeroCraft.instance.windowMain.clear();
		}
	}
}
