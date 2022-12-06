package net.fieme.zerocraft.event.builtin;

import net.fieme.zerocraft.event.Event;
import net.fieme.zerocraft.game.EntityPlayer;

public class EventPlayerJoin implements Event {
	private boolean cancelled;
	public final EntityPlayer player;
	
	public EventPlayerJoin(EntityPlayer player) {
		this.player = player;
	}
	
	@Override
	public boolean getCancelled() throws UnsupportedOperationException {
		return cancelled;
	}

	@Override
	public void setCancelled(boolean value) throws UnsupportedOperationException {
		this.cancelled = value;
	}
}
