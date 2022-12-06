package net.fieme.zerocraft.gui;

import javax.swing.JFrame;

import net.fieme.zerocraft.event.EventBasicObject;
import net.fieme.zerocraft.event.EventFirer;

/**
 * A graphical interface window
 */
public abstract class Window {
	public static final long EVENT_WINDOWCLOSE_ID = 334609476;
	/**
	 * The base of this window
	 */
	public JFrame frame;
	/**
	 * Basic event firer that fires when the window is closed
	 * @implSpec this should be null when the window is created or closed, 
	 * and should only be created when the window is shown
	 * @implSpec the object should be null
	 */
	public EventFirer<EventBasicObject> windowClose;
	/**
	 * Shows this window
	 */
	public abstract void show();
	/**
	 * Closes this window
	 */
	public abstract void close();
}
