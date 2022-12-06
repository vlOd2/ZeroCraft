package net.fieme.zerocraft.nativecode;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.win32.StdCallLibrary;

public interface Kernel32 extends StdCallLibrary {
	public Kernel32 INSTANCE = (Kernel32) Native.loadLibrary("kernel32", Kernel32.class);
	
	public Pointer GetStdHandle(int nStdHandle);
	public boolean GetConsoleScreenBufferInfo(Pointer hConsoleOutput,
			CONSOLE_SCREEN_BUFFER_INFO lpConsoleScreenBufferInfo);
}