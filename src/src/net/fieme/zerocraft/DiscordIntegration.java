package net.fieme.zerocraft;

import club.minnced.discord.rpc.DiscordEventHandlers;
import club.minnced.discord.rpc.DiscordRPC;
import club.minnced.discord.rpc.DiscordRichPresence;

public class DiscordIntegration {
	private static final String applicationID = "1021818381541838899";
	private static DiscordRPC discordRPC;
	private static Thread callbackThread;
	private static DiscordEventHandlers discordEventHandlers;
	private static DiscordRichPresence discordRichPresence;
	
	/**
	 * Initializes the Discord integration
	 */
	public static void init() {
		try {
			discordRPC = DiscordRPC.INSTANCE;
			discordEventHandlers = new DiscordEventHandlers();
			discordRichPresence = new DiscordRichPresence();
			discordRPC.Discord_Initialize(applicationID, discordEventHandlers, true, null);
			callbackThread = new Thread() {
				@Override
				public void run() {
		            while (!Thread.currentThread().isInterrupted()) {
		    			try {
		                	discordRPC.Discord_RunCallbacks();
		                    Thread.sleep(2000);
		    			} catch (Exception ex) {
		    				break;
		    			}
		            }	
				}	
	        };
			callbackThread.start();
		} catch (Exception ex) {
		}
	}
	
	/**
	 * Destroyes the Discord integration
	 */
	public static void destroy() {
		try {
			callbackThread.interrupt();
			discordRPC.Discord_Shutdown();
			
			discordRichPresence = null;
			discordEventHandlers = null;
			callbackThread = null;
			discordRPC = null;	
		} catch (Exception ex) {
		}
	}
	
	/**
	 * Updates the current status
	 * 
	 * @param numOfPlayers the number of online players
	 * @param numOfWarns the number of logged warnings
	 * @param numOfErrors the number of logged errors
	 */
	public static void update(int numOfPlayers, int numOfWarns, int numOfErrors) {
		try {
			if (discordRichPresence == null) return;
			discordRichPresence.details =
					"Players: " + numOfPlayers + " | " +
					"Warnings: " + numOfWarns + " | " +
					"Errors: " + numOfErrors;
			discordRichPresence.state = "Hosting a classic server";
			discordRichPresence.largeImageKey = "zerocraft_logo";
			discordRichPresence.largeImageText = "ZeroCraft";
			discordRPC.Discord_UpdatePresence(discordRichPresence);	
		} catch (Exception ex) {
		}
	}
}
