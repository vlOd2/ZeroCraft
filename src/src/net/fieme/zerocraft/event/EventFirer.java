package net.fieme.zerocraft.event;

import java.lang.reflect.Method;
import java.util.ArrayList;

import net.fieme.zerocraft.Utils;
import net.fieme.zerocraft.logging.Logging;

/**
 * A class that handles fierering of a specific event
 * 
 * @param <EVENT> the event to handle
 */
public class EventFirer<EVENT> {
	private ArrayList<EventListener> listeners = new ArrayList<EventListener>();
	
	/**
	 * Adds a listener to this firer
	 * 
	 * @param listener the listener
	 * @throws IllegalArgumentException if the listener has already been added
	 */
	public void addListener(EventListener listener) {
		if (this.listeners.contains(listener))
			throw new IllegalArgumentException();
		this.listeners.add(listener);
	}
	
	/**
	 * Removes a listener to this firer
	 * 
	 * @param listener the listener
	 * @throws IllegalArgumentException if the listener has not been added
	 */
	public void removeListener(EventListener listener) {
		if (!this.listeners.contains(listener))
			throw new IllegalArgumentException();
		this.listeners.remove(listener);
	}

	/**
	 * Fires the specified event instance to every listener
	 * 
	 * @param eventInstance the instance to fire
	 */
	public void fire(EVENT eventInstance) {
		try {
			for (EventListener listener : this.listeners.toArray(new EventListener[0])) {
				for (Method listenerMethod : listener.getClass().getDeclaredMethods()) {
					if (listenerMethod.getAnnotation(EventHandler.class) != null && 
							listenerMethod.getParameterCount() == 1 && 
							listenerMethod.getParameterTypes()[0] == eventInstance.getClass()) {
						listenerMethod.setAccessible(true);
						listenerMethod.invoke(listener, eventInstance);
					}
				}
			}	
		} catch (Exception ex) {
			Logging.logSevere(Utils.getExceptionStackTraceAsStr(ex));
		}
	}
}
