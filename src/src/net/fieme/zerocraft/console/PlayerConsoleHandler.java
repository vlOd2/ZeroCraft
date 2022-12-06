package net.fieme.zerocraft.console;

import net.fieme.zerocraft.game.EntityPlayer;

/**
 * Handler that handles player commands and variables
 */
public class PlayerConsoleHandler extends ConsoleHandler {
	/**
	 * Handler that handles player commands and variables
	 * 
	 * @param player the handled player
	 */
	public PlayerConsoleHandler(EntityPlayer player) {
		super(null);
		this.caller.isPlayer = true;
		this.caller.player = player;
		this.addDefaults();
	}
	
	/**
	 * This function always throws {@link IllegalStateException}
	 */
	@Override
	public void start() {
		throw new IllegalStateException();
	}

	/**
	 * This function clears the commands and variables registered
	 */
	@Override
	public void close() {
    	this.commands.clear();
    	this.variables.clear();
	}
}
