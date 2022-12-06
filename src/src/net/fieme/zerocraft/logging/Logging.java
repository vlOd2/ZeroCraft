package net.fieme.zerocraft.logging;

import java.awt.Color;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import net.fieme.zerocraft.ColorUtil;
import net.fieme.zerocraft.ZeroCraft;
import net.fieme.zerocraft.event.EventBasicObject;
import net.fieme.zerocraft.event.EventHandler;
import net.fieme.zerocraft.event.EventListener;

public class Logging {
	public static int totalWarns;
	public static int totalErrors;
	private static InputStream originalStdIn;
	private static PrintStream originalStdOut;
	private static PrintStream originalStdErr;
	private static InputStream stdIn; 
	private static MonitoredByteArrayOutputStream stdOut; 
	private static MonitoredByteArrayOutputStream stdErr;
	public static boolean didUpdateInWindow;
	
	static {
		originalStdIn = System.in;
		originalStdOut = System.out;
		originalStdErr = System.err;

		stdIn = new InputStream() {
			@Override
			public int available() throws IOException {
				return originalStdIn.available();
			}
			
			@Override
            public int read() throws IOException {
                int chr = originalStdIn.read();
                stdOut.write(chr);
                return chr;
            }
			
            @Override
            public int read(byte[] buffer) throws IOException {
            	return this.read(buffer, 0, buffer.length);
            }
			
            @Override
            public int read(byte[] buffer, int off, int len) throws IOException {
            	int readResult = originalStdIn.read(buffer, off, len);
                stdOut.write(buffer, off, readResult);
                return readResult;
            }
        };
        
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
		
		System.setIn(stdIn);
		System.setOut(new PrintStream(stdOut));
		System.setErr(new PrintStream(stdErr));
	}
	
	public static void log(String header, String message, boolean err, 
			boolean outputInWindow, Color windowColor, boolean enableColorCodes) {
		LocalDateTime localDateTime = LocalDateTime.now();
		DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
		String text = dateTimeFormatter.format(localDateTime) + " [" + header + "] " + message;

		if (outputInWindow) {
			if (ZeroCraft.instance != null && 
				ZeroCraft.instance.windowMain != null) {
				ZeroCraft.instance.windowMain.appendText(text, windowColor, enableColorCodes);
			}
		}
		
		didUpdateInWindow = true;
		text = ColorUtil.stripColorCodes(text, '&');
		
		if (err) {
			System.err.println(text);
		} else {
			System.out.println(text);
		}
	}
	
	public static void logLevel(LoggingLevel level, String message, boolean outputInWindow, 
			boolean enableColorCodes) {
		if (level == LoggingLevel.ERROR || 
			level == LoggingLevel.SEVERE || 
			level == LoggingLevel.FATAL) {
			totalErrors++;
			Logging.log(level.toString(), message, true, outputInWindow, 
					Color.red, enableColorCodes);
		} else if (level == LoggingLevel.WARN) {
			totalWarns++;
			Logging.log(level.toString(), message, false, outputInWindow, 
					new Color(246, 190, 0), enableColorCodes);
		} else {
			Logging.log(level.toString(), message, false, outputInWindow, 
					Color.black, enableColorCodes);
		}
		
		if (ZeroCraft.instance != null)
			ZeroCraft.instance.updatePlayerCount(false);
	}

	public static void logVerbose(String message, boolean outputInWindow, boolean enableColorCodes) {
		Logging.logLevel(LoggingLevel.VERBOSE, message, outputInWindow, enableColorCodes);
	}
	
	public static void logInfo(String message, boolean outputInWindow, boolean enableColorCodes) {
		Logging.logLevel(LoggingLevel.INFO, message, outputInWindow, enableColorCodes);
	}
	
	public static void logWarn(String message, boolean outputInWindow, boolean enableColorCodes) {
		Logging.logLevel(LoggingLevel.WARN, message, outputInWindow, enableColorCodes);
	}
	
	public static void logError(String message, boolean outputInWindow, boolean enableColorCodes) {
		Logging.logLevel(LoggingLevel.ERROR, message, outputInWindow, enableColorCodes);
	}
	
	public static void logSevere(String message, boolean outputInWindow, boolean enableColorCodes) {
		Logging.logLevel(LoggingLevel.SEVERE, message, outputInWindow, enableColorCodes);
	}
	
	public static void logFatal(String message, boolean outputInWindow, boolean enableColorCodes) {
		Logging.logLevel(LoggingLevel.FATAL, message, outputInWindow, enableColorCodes);
	}
	
	public static void logVerbose(String message, boolean outputInWindow) {
		Logging.logLevel(LoggingLevel.VERBOSE, message, outputInWindow, false);
	}
	
	public static void logInfo(String message, boolean outputInWindow) {
		Logging.logLevel(LoggingLevel.INFO, message, outputInWindow, false);
	}
	
	public static void logWarn(String message, boolean outputInWindow) {
		Logging.logLevel(LoggingLevel.WARN, message, outputInWindow, false);
	}
	
	public static void logError(String message, boolean outputInWindow) {
		Logging.logLevel(LoggingLevel.ERROR, message, outputInWindow, false);
	}
	
	public static void logSevere(String message, boolean outputInWindow) {
		Logging.logLevel(LoggingLevel.SEVERE, message, outputInWindow, false);
	}
	
	public static void logFatal(String message, boolean outputInWindow) {
		Logging.logLevel(LoggingLevel.FATAL, message, outputInWindow, false);
	}
	
	public static void logVerbose(String message) {
		Logging.logLevel(LoggingLevel.VERBOSE, message, true, false);
	}
	
	public static void logInfo(String message) {
		Logging.logLevel(LoggingLevel.INFO, message, true, false);
	}
	
	public static void logWarn(String message) {
		Logging.logLevel(LoggingLevel.WARN, message, true, false);
	}
	
	public static void logError(String message) {
		Logging.logLevel(LoggingLevel.ERROR, message, true, false);
	}
	
	public static void logSevere(String message) {
		Logging.logLevel(LoggingLevel.SEVERE, message, true, false);
	}
	
	public static void logFatal(String message) {
		Logging.logLevel(LoggingLevel.FATAL, message, true, false);
	}
}
