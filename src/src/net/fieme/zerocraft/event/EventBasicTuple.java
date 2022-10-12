package net.fieme.zerocraft.event;

import net.fieme.zerocraft.Tuple;

public class EventBasicTuple implements Event {
	private boolean cancelled;
	public final long id;
	public final Tuple<Object, Object> tuple;
	
	public EventBasicTuple(long id, Tuple<Object, Object> tuple) {
		this.id = id;
		this.tuple = tuple;
	}
	
	@Override
	public boolean getCancelled() throws UnsupportedOperationException {
		return this.cancelled;
	}

	@Override
	public void setCancelled(boolean value) throws UnsupportedOperationException {
		this.cancelled = value;
	}
}