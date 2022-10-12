package net.fieme.zerocraft.event;

public class EventBasicObject implements Event {
	private boolean cancelled;
	public final long id;
	public final Object object;
	
	public EventBasicObject(long id, Object object) {
		this.id = id;
		this.object = object;
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