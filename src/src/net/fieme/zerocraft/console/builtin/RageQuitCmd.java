package net.fieme.zerocraft.console.builtin;

import net.fieme.zerocraft.ZeroCraft;
import net.fieme.zerocraft.console.ConsoleCaller;
import net.fieme.zerocraft.console.ConsoleCommand;

public class RageQuitCmd implements ConsoleCommand {
	@Override
	public String getName() {
		return "ragequit";
	}

	@Override
	public String[] getAliases() {
		return new String[] { "rq", "angryquit", "aq" };
	}
	
	@Override
	public String getDescription() {
		return "Makes you ragequit";
	}

	@Override
	public String getUsage() {
		return "ragequit";
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
		if (!caller.isPlayer) {
			caller.sendMessage("&cOnly players are allowed to use this command!");
			return;
		}
		
		ZeroCraft.instance.sendGlobalChatMessage("&e" + caller.player.name + " &chas rage quited!");
		caller.player.kick("&cRAGE QUIT!");
	}
}
