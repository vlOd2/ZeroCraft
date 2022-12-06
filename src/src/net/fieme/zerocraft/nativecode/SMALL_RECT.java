package net.fieme.zerocraft.nativecode;

import java.util.ArrayList;
import java.util.List;

import com.sun.jna.Structure;

public class SMALL_RECT extends Structure {
	public short Left;
	public short Top;
	public short Right;
	public short Bottom;
	
	@Override
	protected List<String> getFieldOrder() {
		ArrayList<String> fieldOrder = new ArrayList<String>();
		fieldOrder.add("Left");
		fieldOrder.add("Top");
		fieldOrder.add("Right");
		fieldOrder.add("Bottom");
		return fieldOrder;
	}
}
