package net.fieme.zerocraft;

import java.io.ByteArrayOutputStream;

import net.fieme.zerocraft.event.EventBasicObject;
import net.fieme.zerocraft.event.EventFirer;

public class MonitoredByteArrayOutputStream extends ByteArrayOutputStream {
	public static final long EVENT_DATACHANGED_ID = 517069500;
	public EventFirer<EventBasicObject> dataChanged;
	
	public MonitoredByteArrayOutputStream() {
		super();
		this.dataChanged = new EventFirer<EventBasicObject>();
	}
	
	public MonitoredByteArrayOutputStream(int size) {
		super(size);
		this.dataChanged = new EventFirer<EventBasicObject>();
	}
	
	@Override
	public synchronized void write(int b) {
    	super.write(b);
    	dataChanged.fire(new EventBasicObject(EVENT_DATACHANGED_ID, new byte[] { (byte) b }));
    }

    @Override
    public synchronized void write(byte[] b) {
    	super.write(b, 0, b.length);
    	dataChanged.fire(new EventBasicObject(EVENT_DATACHANGED_ID, b));
    }
    
    @Override
    public synchronized void write(byte[] b, int off, int len) {
    	super.write(b, off, len);
    	byte[] data = new byte[len];
    	System.arraycopy(b, off, data, 0, len);
    	dataChanged.fire(new EventBasicObject(EVENT_DATACHANGED_ID, data));
    }
}
