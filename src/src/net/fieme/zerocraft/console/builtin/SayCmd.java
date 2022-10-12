package net.fieme.zerocraft.console.builtin;

import net.fieme.zerocraft.ZeroCraft;
import net.fieme.zerocraft.configuration.MessagesConfig;
import net.fieme.zerocraft.console.ConsoleCommand;
import net.fieme.zerocraft.game.Player;

public class SayCmd implements ConsoleCommand {
	private Object caller;
	
	@Override
	public String getName() {
		return "say";
	}

	@Override
	public String getDescription() {
		return "Says something in the chat";
	}

	@Override
	public void setCaller(Object caller) {
		this.caller = caller;
	}

	@Override
	public String getUsage() {
		return "say <message>";
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
	public void execute(String[] args) {
		if (this.caller == null) {
			ZeroCraft.instance.sendGlobalChatMessage(MessagesConfig.chatFormat
					.replace("%prefix%", "")
					.replace("%author%", "Console")
					.replace("%suffix%", "")
					.replace("%message%", args[0]));
		} else if (this.caller instanceof Player) {
			Player player = (Player) this.caller;
			player.packetHandler.handleChat(args[0]);
		}
	}
}
