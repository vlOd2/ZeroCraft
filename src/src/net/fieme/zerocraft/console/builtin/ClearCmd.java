package net.fieme.zerocraft.console.builtin;

import net.fieme.zerocraft.ZeroCraft;
import net.fieme.zerocraft.console.ConsoleCommand;
import net.fieme.zerocraft.game.Player;

public class ClearCmd implements ConsoleCommand {
	private Object caller;
	
	@Override
	public String getName() {
		return "clear";
	}

	@Override
	public String getDescription() {
		return "Clears the chat (local)";
	}

	@Override
	public void setCaller(Object caller) {
		this.caller = caller;
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
	public void execute(String[] args) {
		if (caller == null) {
			for (int i = 0; i < 100; i++)
			    System.out.print("\n\r");
			if (ZeroCraft.instance.windowMain != null)
				ZeroCraft.instance.windowMain.clear();
		} else if (caller instanceof Player) {
			Player player = (Player) caller;
			for (int i = 0; i < 100; i++)
			    player.packetHandler.sendChatMessage(" ");
		}
	}
}
