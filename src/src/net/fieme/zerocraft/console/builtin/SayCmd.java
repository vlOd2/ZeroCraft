package net.fieme.zerocraft.console.builtin;

import net.fieme.zerocraft.ColorUtil;
import net.fieme.zerocraft.ZeroCraft;
import net.fieme.zerocraft.configuration.MessagesConfig;
import net.fieme.zerocraft.console.ConsoleCaller;
import net.fieme.zerocraft.console.ConsoleCommand;
import net.fieme.zerocraft.game.EntityPlayer;

public class SayCmd implements ConsoleCommand {
	@Override
	public String getName() {
		return "say";
	}

	@Override
	public String getDescription() {
		return "Says something in the chat";
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
	public void execute(ConsoleCaller caller, String[] args) {
		if (!caller.isPlayer) {
			ZeroCraft.instance.sendGlobalChatMessage(MessagesConfig.instance.chatFormat
					.replace("%prefix%", "")
					.replace("%author%", "Console")
					.replace("%suffix%", "")
					.replace("%message%", ColorUtil.cleanColorCodes(args[0].replace("%", "&"), '&')));
		} else {
			EntityPlayer player = caller.player;
			player.packetHandler.handleChat(args[0]);
		}
	}
}
