package net.fieme.zerocraft.nativecode;

import java.util.ArrayList;
import java.util.List;

import com.sun.jna.Structure;

public class COORD extends Structure {
	public short X;
	public short Y;
	
	@Override
	protected List<String> getFieldOrder() {
		ArrayList<String> fieldOrder = new ArrayList<String>();
		fieldOrder.add("X");
		fieldOrder.add("Y");
		return fieldOrder;
	}
}
