package net.fieme.zerocraft.event.builtin;

import net.fieme.zerocraft.event.Event;
import net.fieme.zerocraft.game.EntityPlayer;

public class EventPlayerLeave implements Event {
	public final EntityPlayer player;
	
	public EventPlayerLeave(EntityPlayer player) {
		this.player = player;
	}
	
	@Override
	public boolean getCancelled() throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setCancelled(boolean value) throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}
}
