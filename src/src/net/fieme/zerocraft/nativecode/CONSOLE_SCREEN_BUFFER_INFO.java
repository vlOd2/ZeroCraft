package net.fieme.zerocraft.nativecode;

import java.util.ArrayList;
import java.util.List;

import com.sun.jna.Structure;

public class CONSOLE_SCREEN_BUFFER_INFO extends Structure {
	public COORD dwSize;
	public COORD dwCursorPosition;
	public short wAttributes;
	public SMALL_RECT srWindow;
	public COORD dwMaximumWindowSize;
	
	@Override
	protected List<String> getFieldOrder() {
		ArrayList<String> fieldOrder = new ArrayList<String>();
		fieldOrder.add("dwSize");
		fieldOrder.add("dwCursorPosition");
		fieldOrder.add("wAttributes");
		fieldOrder.add("srWindow");
		fieldOrder.add("dwMaximumWindowSize");
		return fieldOrder;
	}
}
