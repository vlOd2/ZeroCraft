package net.fieme.zerocraft.logging;

import java.awt.Color;
import java.io.PrintStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import net.fieme.zerocraft.DiscordIntegration;
import net.fieme.zerocraft.MonitoredByteArrayOutputStream;
import net.fieme.zerocraft.ZeroCraft;
import net.fieme.zerocraft.event.EventBasicObject;
import net.fieme.zerocraft.event.EventHandler;
import net.fieme.zerocraft.event.EventListener;

public class Logging {
	public static int totalWarns;
	public static int totalErrors;
	private static PrintStream originalStdOut;
	private static PrintStream originalStdErr;
	private static MonitoredByteArrayOutputStream stdOut; 
	private static MonitoredByteArrayOutputStream stdErr;
	public static boolean didUpdateInWindow;
	
	static {
		originalStdOut = System.out;
		originalStdErr = System.err;
		stdOut = new MonitoredByteArrayOutputStream();
		stdOut.dataChanged.addListener(new EventListener() {
			@EventHandler
			public void stdOut_dataChanged(EventBasicObject e) {
				if (e.id != MonitoredByteArrayOutputStream.EVENT_DATACHANGED_ID) return;
				try {
					byte[] data = (byte[])e.object;
					String dataStr = new String(data);
					originalStdOut.write(data);
					
					if (ZeroCraft.instance != null && 
						ZeroCraft.instance.windowMain != null && 
						!dataStr.isEmpty() && 
						dataStr.trim().length() >= 1) {
						if (!didUpdateInWindow) {
							ZeroCraft.instance.windowMain.appendText(
									dataStr, Color.black);
						}
						else {
							didUpdateInWindow = false;
						}
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		});
		stdErr = new MonitoredByteArrayOutputStream();
		stdErr.dataChanged.addListener(new EventListener() {
			@EventHandler
			public void stdOut_dataChanged(EventBasicObject e) {
				if (e.id != MonitoredByteArrayOutputStream.EVENT_DATACHANGED_ID) return;
				try {
					byte[] data = (byte[])e.object;
					String dataStr = new String(data);
					originalStdErr.write(data);
					
					if (ZeroCraft.instance != null && 
						ZeroCraft.instance.windowMain != null && 
						!dataStr.isEmpty() && 
						dataStr.trim().length() >= 1) {
						if (!didUpdateInWindow) {
							ZeroCraft.instance.windowMain.appendText(
									dataStr, Color.red);
						}
						else {
							didUpdateInWindow = false;
						}
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		});
		System.setOut(new PrintStream(stdOut));
		System.setErr(new PrintStream(stdErr));
	}

	public static void log(String header, String message, boolean err, boolean outputInWindow, Color windowColor) {
		LocalDateTime localDateTime = LocalDateTime.now();
		DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
		String text = dateTimeFormatter.format(localDateTime) + " [" + header + "] " + message;

		if (outputInWindow) {
			if (ZeroCraft.instance != null && 
				ZeroCraft.instance.windowMain != null) {
				ZeroCraft.instance.windowMain.appendText(text, windowColor);
			}
		}
		
		didUpdateInWindow = true;
		if (err) {
			System.err.println(text);
		} else {
			System.out.println(text);
		}
	}
	
	public static void logLevel(LoggingLevel level, String message, boolean outputInWindow) {
		if (level == LoggingLevel.ERROR || 
			level == LoggingLevel.SEVERE || 
			level == LoggingLevel.FATAL) {
			totalErrors++;
			Logging.log(level.toString(), message, true, outputInWindow, Color.red);
		} else if (level == LoggingLevel.WARN) {
			totalWarns++;
			Logging.log(level.toString(), message, false, outputInWindow, new Color(246, 190, 0));
		} else {
			Logging.log(level.toString(), message, false, outputInWindow, Color.black);
		}
		
		if (ZeroCraft.instance != null)
			DiscordIntegration.update(ZeroCraft.instance.players.size(), totalWarns, totalErrors);
	}
	
	public static void logVerbose(String message, boolean outputInWindow) {
		Logging.logLevel(LoggingLevel.VERBOSE, message, outputInWindow);
	}
	
	public static void logInfo(String message, boolean outputInWindow) {
		Logging.logLevel(LoggingLevel.INFO, message, outputInWindow);
	}
	
	public static void logWarn(String message, boolean outputInWindow) {
		Logging.logLevel(LoggingLevel.WARN, message, outputInWindow);
	}
	
	public static void logError(String message, boolean outputInWindow) {
		Logging.logLevel(LoggingLevel.ERROR, message, outputInWindow);
	}
	
	public static void logSevere(String message, boolean outputInWindow) {
		Logging.logLevel(LoggingLevel.SEVERE, message, outputInWindow);
	}
	
	public static void logFatal(String message, boolean outputInWindow) {
		Logging.logLevel(LoggingLevel.FATAL, message, outputInWindow);
	}
	
	public static void logVerbose(String message) {
		Logging.logLevel(LoggingLevel.VERBOSE, message, true);
	}
	
	public static void logInfo(String message) {
		Logging.logLevel(LoggingLevel.INFO, message, true);
	}
	
	public static void logWarn(String message) {
		Logging.logLevel(LoggingLevel.WARN, message, true);
	}
	
	public static void logError(String message) {
		Logging.logLevel(LoggingLevel.ERROR, message, true);
	}
	
	public static void logSevere(String message) {
		Logging.logLevel(LoggingLevel.SEVERE, message, true);
	}
	
	public static void logFatal(String message) {
		Logging.logLevel(LoggingLevel.FATAL, message, true);
	}
}
