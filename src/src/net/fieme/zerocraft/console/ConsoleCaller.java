package net.fieme.zerocraft.console;

import net.fieme.zerocraft.game.EntityPlayer;
import net.fieme.zerocraft.logging.Logging;

public class ConsoleCaller {
	public boolean isPlayer;
	public EntityPlayer player;
	
	/**
	 * Sends a message to the caller
	 * 
	 * @implSpec if the caller is not a player 
	 * color codes should still be parsed
	 * @param message the message
	 */
	public void sendMessage(String message) {
		if (!this.isPlayer) {
			Logging.logInfo(message, true, true);
		} else {
			this.player.packetHandler.sendChatMessage(message);
		}
	}
	
	/**
	 * Checks if the caller has the specified permission
	 * 
	 * @param name the name of the permission
	 * @implSpec if the caller is not a player 
	 * this should always return true
	 * @return true if the permission is allowed, false if otherwise
	 */
	public boolean hasPermission(String name) {
		if (!this.isPlayer || name.equalsIgnoreCase("none")) {
			return true;
		} else {
			return this.player.permissionUser.hasPermission(name, false);
		}
	}
}
