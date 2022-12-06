package net.fieme.zerocraft.event.builtin;

import net.fieme.zerocraft.event.Event;
import net.fieme.zerocraft.game.EntityPlayer;

public class EventPlayerBlock implements Event {
	private boolean cancelled;
	public final EntityPlayer player;
	public final short x;
	public final short y;
	public final short z;
	public final byte id;
	
	public EventPlayerBlock(EntityPlayer player, short x, 
			short y, short z, byte id) {
		this.player = player;
		this.x = x;
		this.y = y;
		this.z = z;
		this.id = id;
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
