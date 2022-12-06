import java.awt.GraphicsEnvironment;

import net.fieme.zerocraft.ZeroCraft;
import net.fieme.zerocraft.gui.WindowMain;

/**
 * Default starter for ZeroCraft
 */
public class Start {
	public static void main(String[] args) {
		ZeroCraft.instance = new ZeroCraft();
		if (System.getProperty("nogui") == null && 
			!GraphicsEnvironment.isHeadless()) {
			ZeroCraft.instance.windowMain = new WindowMain();
			ZeroCraft.instance.windowMain.show();
			ZeroCraft.instance.windowMain.windowClose.addListener(ZeroCraft.instance);
			ZeroCraft.instance.windowMain.inputSubmitted.addListener(ZeroCraft.instance);
			ZeroCraft.instance.windowMain.appendText("Welcome to the control panel of " + 
					ZeroCraft.VERSION_DISPLAYABLE_STR + "!");
			ZeroCraft.instance.windowMain.appendText("To start the server, type \"start\"");
			ZeroCraft.instance.windowMain.appendText("After you started the server,"
					+ " you can type \"help\" to see the available commands");
		} else {
			ZeroCraft.instance.start();
		}
	}
}
