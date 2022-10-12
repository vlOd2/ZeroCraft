package net.fieme.zerocraft.console.builtin;

import net.fieme.zerocraft.ZeroCraft;
import net.fieme.zerocraft.configuration.MessagesConfig;
import net.fieme.zerocraft.console.ConsoleCommand;
import net.fieme.zerocraft.game.Player;
import net.fieme.zerocraft.game.World;
import net.fieme.zerocraft.logging.Logging;

public class WorldCmd implements ConsoleCommand {
	private Object caller;
	
	@Override
	public String getName() {
		return "world";
	}

	@Override
	public String getDescription() {
		return "Switches the world you are currently on";
	}

	@Override
	public void setCaller(Object caller) {
		this.caller = caller;
	}
	
	@Override
	public String getUsage() {
		return "world <name>";
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
		if (caller == null) {
			Logging.logWarn("You cannot use this command!");
		} else if (caller instanceof Player) {
			Player player = (Player) caller;
			String worldName = args[0];
			
			player.packetHandler.sendChatMessage(MessagesConfig.feedbackSwitchingWorld
					.replace("%world%", worldName));
			World world = ZeroCraft.instance.getWorldByName(worldName);
			
			if (world != null) {
				player.switchWorld(world);
				player.packetHandler.sendChatMessage(MessagesConfig.feedbackSwitchedWorld
						.replace("%world%", worldName));
			} else {
				player.packetHandler.sendChatMessage(MessagesConfig.feedbackWorldNotFound);
			}
		}
	}
}
