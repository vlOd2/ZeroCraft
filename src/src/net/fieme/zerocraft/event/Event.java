package net.fieme.zerocraft.event;

/**
 * The base of an event
 */
public interface Event {
	/**
	 * Gets the value of the event's cancel state
	 * 
	 * @return the cancel state
	 * @throws UnsupportedOperationException if cancel state is n/a
	 */
	public boolean getCancelled() throws UnsupportedOperationException;
	
	/**
	 * Sets the value of the event's cancel state
	 * 
	 * @param canceledValue the new cancel state
	 * @throws UnsupportedOperationException if cancel state is n/a
	 */
	public void setCancelled(boolean value) throws UnsupportedOperationException;
}
