package net.fieme.zerocraft.console.builtin;

import net.fieme.zerocraft.configuration.ServerConfig;
import net.fieme.zerocraft.console.ConsoleCaller;
import net.fieme.zerocraft.console.ConsoleCommand;

public class RulesCmd implements ConsoleCommand {
	@Override
	public String getName() {
		return "rules";
	}

	@Override
	public String getDescription() {
		return "Shows the rules of this server";
	}
	
	@Override
	public String getUsage() {
		return "rules";
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
		if (ServerConfig.instance.serverRules.length > 0) {
			caller.sendMessage("&aThe rules of this server&e:");
			for (int i = 0; i < ServerConfig.instance.serverRules.length; i++) {
				caller.sendMessage("&8[&e" + i + "&8]&b " + ServerConfig.instance.serverRules[i]);
			}	
		} else {
			caller.sendMessage("&aThis server has no rules");
		}
	}
}
