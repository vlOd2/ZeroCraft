package net.fieme.zerocraft;

import java.io.File;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Scanner;

import javax.swing.JOptionPane;

import net.fieme.zerocraft.configuration.BannedConfig;
import net.fieme.zerocraft.configuration.ConfigLoaderSaver;
import net.fieme.zerocraft.configuration.MessagesConfig;
import net.fieme.zerocraft.configuration.MutedConfig;
import net.fieme.zerocraft.configuration.PermissionsConfig;
import net.fieme.zerocraft.configuration.ServerConfig;
import net.fieme.zerocraft.console.ConsoleHandler;
import net.fieme.zerocraft.event.EventBasicObject;
import net.fieme.zerocraft.event.EventFirer;
import net.fieme.zerocraft.event.EventHandler;
import net.fieme.zerocraft.event.EventListener;
import net.fieme.zerocraft.event.builtin.EventPlayerBlock;
import net.fieme.zerocraft.event.builtin.EventPlayerChat;
import net.fieme.zerocraft.event.builtin.EventPlayerJoin;
import net.fieme.zerocraft.event.builtin.EventPlayerLeave;
import net.fieme.zerocraft.event.builtin.EventPlayerLogin;
import net.fieme.zerocraft.event.builtin.EventPlayerMove;
import net.fieme.zerocraft.game.EntityPlayer;
import net.fieme.zerocraft.game.WorldManager;
import net.fieme.zerocraft.gui.Window;
import net.fieme.zerocraft.gui.WindowMain;
import net.fieme.zerocraft.logging.Logging;
import net.fieme.zerocraft.networking.Server;
import net.fieme.zerocraft.plugin.PluginManager;

/**
 * Do you really need an explanation for this?
 */
public class ZeroCraft implements EventListener {
	/**
	 * The current ZeroCraft instance
	 */
	public static ZeroCraft instance;
	public static final double VERSION = 1.0;
	public static final String VERSION_STR = "ZeroCraft/" + VERSION;
	public static final String VERSION_DISPLAYABLE_STR = "ZeroCraft v" + VERSION;
	public boolean isRunning = false;
	public Server server;
	public ServerListingManager serverListingManager;
	public ConsoleHandler consoleHandler;
	public PluginManager pluginManager;
	public WindowMain windowMain;
	public WorldManager worldManager;
	public final ArrayList<EntityPlayer> players = new ArrayList<EntityPlayer>();
	public EventFirer<EventPlayerJoin> playerJoinEventFirer;
	public EventFirer<EventPlayerLogin> playerLoginEventFirer;
	public EventFirer<EventPlayerMove> playerMoveEventFirer;
	public EventFirer<EventPlayerBlock> playerBlockEventFirer;
	public EventFirer<EventPlayerChat> playerChatEventFirer;
	public EventFirer<EventPlayerLeave> playerLeaveEventFirer;
	
	/**
	 * Starts the server
	 * 
	 * @throws IllegalStateException if the server is already running
	 */
	public void start() {
		if (this.isRunning) throw new IllegalStateException(); 
		
		try {
			long startTime = System.currentTimeMillis();
			
			this.isRunning = true;
			this.worldManager = new WorldManager(this);
			this.consoleHandler = new ConsoleHandler("> ");	
			this.playerJoinEventFirer = new EventFirer<EventPlayerJoin>();
			this.playerLoginEventFirer = new EventFirer<EventPlayerLogin>();
			this.playerMoveEventFirer = new EventFirer<EventPlayerMove>();
			this.playerBlockEventFirer = new EventFirer<EventPlayerBlock>();
			this.playerChatEventFirer = new EventFirer<EventPlayerChat>();
			this.playerLeaveEventFirer = new EventFirer<EventPlayerLeave>();
			
			Logging.logInfo("Welcome to " + VERSION_DISPLAYABLE_STR + "!");
			InputStream logoStream = this.getClass().getResourceAsStream("/Logo.txt");
			Scanner logoScanner = new Scanner(logoStream);
			while (logoScanner.hasNextLine()) {
				String logoLine = logoScanner.nextLine().trim();
				Logging.logInfo(logoLine, true);
			}
			logoScanner.close();
			Logging.logInfo("Starting " + VERSION_DISPLAYABLE_STR + "...");

			Logging.logInfo("Loading server configuration...");
			if (this.reloadConfigs()) {
				Logging.logInfo("Loaded server configuration");
				
				if (ServerConfig.instance.experimental_enablePluginSystem) {
					Logging.logInfo("Loading plugins...");
					this.pluginManager = new PluginManager();
					this.pluginManager.loadPluginsInDir();
					Logging.logInfo("Loaded plugins");
				}
				
				Logging.logInfo("Starting listener on " + ServerConfig.instance.listenIP + ":" + 
						ServerConfig.instance.listenPort + "...");
				this.server = new Server(this, ServerConfig.instance.listenIP, ServerConfig.instance.listenPort);
				this.server.start();
				if (ServerConfig.instance.serverURL != null && 
					!ServerConfig.instance.serverURL.isEmpty()) {
					this.serverListingManager = this.setupServerListingManager(ServerConfig.instance.serverURL);
				} else {
					ServerConfig.instance.performNameVerification = false;
				}
				Logging.logInfo("Started listener on " + ServerConfig.instance.listenIP + ":" + 
						ServerConfig.instance.listenPort);
				
				Logging.logInfo("Starting Discord integration...");
				DiscordIntegration.init();
				DiscordIntegration.update(0, Logging.totalWarns, Logging.totalErrors);
				Logging.logInfo("Started Discord integration");
				
				Logging.logInfo("Server started! Took " + 
						(System.currentTimeMillis() - startTime) + "ms");

				this.consoleHandler.start();
				while (this.isRunning) {
					if (Thread.interrupted()) {
						break;
					}
				}
			} else {
				throw new Exception("Configuration load error");
			}
		} catch (Exception ex) {
			Logging.logFatal("Unable to start: " + Utils.getExceptionStackTraceAsStr(ex));
			this.stop();
		}
	}

	/**
	 * Stops the server
	 */
	public void stop() {
		try {
			Logging.logInfo("Kicking players...");
			try {
				for (EntityPlayer player : this.players.toArray(new EntityPlayer[0])) {
					player.kick(MessagesConfig.instance.serverKickShutdown);
					player = null;
				}	
			} catch (Exception ex) {
			}
			this.players.clear();
			Logging.logInfo("Kicked players");
			
			Logging.logInfo("Destroying Discord integration...");
			DiscordIntegration.destroy();
			Logging.logInfo("Destroyed Discord integration");	
			
			if (ServerConfig.instance.experimental_enablePluginSystem) {
				Logging.logInfo("Unloading plugins...");
				try {
					this.pluginManager.unloadAll();
				} catch (Exception ex) {
				}
				Logging.logInfo("Unloaded plugins");	
			}
			
			Logging.logInfo("Saving worlds...");
			this.worldManager.unloadWorlds();
			Logging.logInfo("Saved worlds");

			Logging.logInfo("Stopping core server...");
			this.isRunning = false;
			if (this.server != null) this.server.stop();
			if (this.consoleHandler != null) this.consoleHandler.close();
			if (this.serverListingManager != null) this.serverListingManager.stop();
			Logging.logInfo("Stopped core server");
			
			Thread.currentThread().interrupt();
			this.server = null;
			this.pluginManager = null;
			this.consoleHandler = null;
			
			System.gc();
		} catch (Exception ex) {
			Logging.logFatal(Utils.getExceptionStackTraceAsStr(ex));
		}
	}
	
	public ServerListingManager setupServerListingManager(String url) {
		try {
			if (!url.contains("/")) {
				throw new Exception();
			} else {
				String heartbeatServer = url.split("/", 2)[0];
				String heartbeatURL = "/" + url.split("/", 2)[1];
				int heartbeatPort = 80;
				
				if (url.contains(":")) {
					String heartbeatPortRaw = url.split(":", 2)[1];
					if (!Utils.isNumeric(heartbeatPortRaw, false)) throw new Exception();
					heartbeatPort = Integer.valueOf(heartbeatPortRaw);
				}
				
				ServerListingManager serverListingManager = new ServerListingManager(this, 
						heartbeatServer, heartbeatURL, heartbeatPort, 7);
				serverListingManager.start();
				
				return serverListingManager;
			}	
		} catch (Exception ex) {
			Logging.logError("Invalid server heartbeat URL specified!"
					+ " Make sure it is in the following format:"
					+ " <hostname>[:port]/<url> (example: minecraft.net/heartbeat.jsp)");
			return null;
		}
	}

	/**
	 * Loads the configuration files
	 * 
	 * @return true if the configurations were loaded successfully, false if otherwise
	 */
	public boolean loadConfigs() {
		try {
			ServerConfig.instance = new ServerConfig();
			MessagesConfig.instance = new MessagesConfig();
			PermissionsConfig.instance = new PermissionsConfig();
			BannedConfig.instance = new BannedConfig();
			MutedConfig.instance = new MutedConfig();
			
			ConfigLoaderSaver serverConfigLoaderSaver = new ConfigLoaderSaver(ServerConfig.instance, 
					new File("server.yml"));
			serverConfigLoaderSaver.load();
			
			if (ServerConfig.instance.maxPlayers > 128) {
				Logging.logWarn("The specified amount for maximum players exceedes 128!"
						+ " It has been set to 128 to prevent issues!");
				ServerConfig.instance.maxPlayers = 128;
			}

			if (ServerConfig.instance.verificationSalt.equals(new ServerConfig().verificationSalt)) {
				Logging.logWarn(Utils.getConsoleSeparatorWithHeader("IMPORTANT"));
				Logging.logWarn("The default verification salt is used as specified by the configuration file!");
				Logging.logWarn("The default verification salt allows bad actors to use any username they want!");
				Logging.logWarn("The verification salt should be changed to something different!");
				Logging.logWarn("To prevent the sense of security, NAME VERIFICATION HAS BEEN DISABLED!");
				Logging.logWarn(Utils.getConsoleSeparatorWithHeader("IMPORTANT"));
				
				ServerConfig.instance.performNameVerification = false;
			}
			
			if (!ServerConfig.instance.performNameVerification) {
				Logging.logWarn(Utils.getConsoleSeparatorWithHeader("IMPORTANT"));
				Logging.logWarn("Name verification has been disabled as specified by the configuration file!");
				Logging.logWarn("Name verification being disabled allows bad actors to use any username they want!");
				Logging.logWarn("You should turn this on, unless you want to play offline or use server proxing!");
				Logging.logWarn(Utils.getConsoleSeparatorWithHeader("IMPORTANT"));
			}
			
			if (this.worldManager.getWorldByName(ServerConfig.instance.mainWorld) == null)
				this.worldManager.loadWorld(ServerConfig.instance.mainWorld, false);
			
			ConfigLoaderSaver messagesConfigLoaderSaver = new ConfigLoaderSaver(MessagesConfig.instance, 
					new File("messages.yml"));
			messagesConfigLoaderSaver.load();
			
			ConfigLoaderSaver permissionsConfigLoaderSaver = new ConfigLoaderSaver(PermissionsConfig.instance, 
					new File("permissions.yml"));
			permissionsConfigLoaderSaver.load();
			
			ConfigLoaderSaver bannedConfigLoaderSaver = new ConfigLoaderSaver(BannedConfig.instance, 
					new File("banned.yml"));
			bannedConfigLoaderSaver.load();
			
			ConfigLoaderSaver mutedConfigLoaderSaver = new ConfigLoaderSaver(MutedConfig.instance, 
					new File("muted.yml"));
			mutedConfigLoaderSaver.load();
			
			for (EntityPlayer player : this.players.toArray(new EntityPlayer[0])) {
				player.updatePermissionUser();
			}
			
			return true;
		} catch (Exception ex) {
			Logging.logFatal("Unable to load configurations: " + 
					Utils.getExceptionStackTraceAsStr(ex));
			return false;
		}
	}
	
	/**
	 * Saves the configuration files
	 * 
	 * @return true if the configurations were saved successfully, false if otherwise
	 */
	public boolean saveConfigs() {
		try {
			ConfigLoaderSaver serverConfigLoaderSaver = new ConfigLoaderSaver(ServerConfig.instance, 
					new File("server.yml"));
			serverConfigLoaderSaver.save();
			
			ConfigLoaderSaver messagesConfigLoaderSaver = new ConfigLoaderSaver(MessagesConfig.instance, 
					new File("messages.yml"));
			messagesConfigLoaderSaver.save();
			
			ConfigLoaderSaver permissionsConfigLoaderSaver = new ConfigLoaderSaver(PermissionsConfig.instance, 
					new File("permissions.yml"));
			permissionsConfigLoaderSaver.save();
			
			ConfigLoaderSaver bannedConfigLoaderSaver = new ConfigLoaderSaver(BannedConfig.instance, 
					new File("banned.yml"));
			bannedConfigLoaderSaver.save();
			
			ConfigLoaderSaver mutedConfigLoaderSaver = new ConfigLoaderSaver(MutedConfig.instance, 
					new File("muted.yml"));
			mutedConfigLoaderSaver.save();
			
			return true;
		} catch (Exception ex) {
			Logging.logFatal("Unable to save configurations: " + 
					Utils.getExceptionStackTraceAsStr(ex));
			return false;
		}
	}
	
	/**
	 * Reloads the configuration files
	 * 
	 * @return true if the configurations were reloaded successfully, false if otherwise
	 * @return
	 */
	public boolean reloadConfigs() {
		boolean reloadStatus = false;
		reloadStatus = this.loadConfigs();
		reloadStatus = this.saveConfigs();
		return reloadStatus;
	}
	
	@EventHandler
	private void windowMain_inputSubmitHandler(EventBasicObject e) {
		if (e.id != WindowMain.EVENT_INPUTSUBMITTED_ID) return;
		String input = ((String)e.object).trim();

		if (input.isEmpty()) {
			JOptionPane.showMessageDialog(
					null,
					"Invalid input specified!",
					ZeroCraft.VERSION_DISPLAYABLE_STR,
					JOptionPane.ERROR_MESSAGE
			);
		} else {
			if (!this.isRunning && !input.startsWith("start")) {
				JOptionPane.showMessageDialog(
						null,
						"The server is not running!",
						ZeroCraft.VERSION_DISPLAYABLE_STR,
						JOptionPane.WARNING_MESSAGE
				);
			} else if (this.isRunning && input.startsWith("start")) {
				JOptionPane.showMessageDialog(
						null,
						"The server is already running!",
						ZeroCraft.VERSION_DISPLAYABLE_STR,
						JOptionPane.WARNING_MESSAGE
				);
			} else {
				this.windowMain.appendText("> " + input);
				
				if (!this.isRunning && input.startsWith("start")) {
					new Thread() {
						public void run() {
							ZeroCraft.instance.start();
						}
					}.start();
				} else {
					// TODO: Fix the core issue of commands freezing
					new Thread() {
						@Override
						public void run() {
							consoleHandler.handleInput(input);
						}
					}.start();
				}
			}
		}
	}
	
	@EventHandler
	private void windowMain_windowClose(EventBasicObject e) {
		if (e.id != Window.EVENT_WINDOWCLOSE_ID) return;
		if (this.isRunning)
			this.stop();
		this.windowMain.close();
	}
	
	/**
	 * Handles a player join
	 * 
	 * @param player the player
	 */
	public void joinPlayer(EntityPlayer player) {
		this.players.add(player);
		this.server.clientReceivedPacketID.addListener(player);
		this.server.clientDisconnected.addListener(player);
		Logging.logInfo(player.client.identifier + " has connected");
		this.updatePlayerCount(true);
		
		EventPlayerJoin eventPlayerJoin = new EventPlayerJoin(player);
		this.playerJoinEventFirer.fire(eventPlayerJoin);
		if (eventPlayerJoin.getCancelled()) {
			player.kick("You have been kicked");
		}
	}
	
	/**
	 * Handles a player login
	 * 
	 * @param player the player
	 */
	public void loginPlayer(EntityPlayer player) {
		this.sendGlobalChatMessage(
				MessagesConfig.instance.playerJoined.replace("%player%", player.name));
		Logging.logInfo(player.name + " is using protocol " + 
				player.protocolVersionNumber + " with protocol extensions " + 
				(player.classicProtocolExtensions ? "enabled" : "disabled"));
		
		EventPlayerLogin eventPlayerLogin = new EventPlayerLogin(player);
		this.playerLoginEventFirer.fire(eventPlayerLogin);
		if (eventPlayerLogin.getCancelled()) {
			player.kick("You have been kicked");
		}
	}
	
	/**
	 * Handles a player move
	 * 
	 * @param player the player
	 * @param x the x
	 * @param y the y
	 * @param z the z
	 * @param yaw the yaw
	 * @param pitch the pitch
	 */
	public void movePlayer(EntityPlayer player, short x, 
			short y, short z, byte yaw, byte pitch) {
		EventPlayerMove eventPlayerMove = new EventPlayerMove(player, x, y, z, yaw, pitch);
		this.playerMoveEventFirer.fire(eventPlayerMove);
		if (eventPlayerMove.getCancelled()) {
			player.packetHandler.sendPlayerTP((byte)-1, 
					player.posX, 
					player.posY, 
					player.posZ, 
					player.yaw,
					player.pitch);
			return;
		}
		
		player.posX = x;
		player.posY = y;
		player.posZ = z;
		player.yaw = yaw;
		player.pitch = pitch;
		
		this.foreachPlayer(new InlineForloop<EntityPlayer>() {
			@Override
			public void onEntry(EntityPlayer entry) {
				if (entry.id != player.id)
					entry.packetHandler.sendPlayerTP(player.id, 
							player.posX, 
							player.posY, 
							player.posZ, 
							player.yaw,
							player.pitch);
			}
		});	
	}
	
	/**
	 * Handles a player block
	 * 
	 * @param player the player
	 * @param x the block x
	 * @param y the block y
	 * @param z the block z
	 * @param yaw the block id
	 */
	public void blockPlayer(EntityPlayer player, short x, 
			short y, short z, byte id) {
		EventPlayerBlock eventPlayerBlock = new EventPlayerBlock(player, x, y, z, id);
		this.playerBlockEventFirer.fire(eventPlayerBlock);
		if (eventPlayerBlock.getCancelled()) {
			return;
		}
		
		player.world.setBlockWithNotify(x, y, z, id);
	}

	/**
	 * Handles a player chat
	 * 
	 * @param player the player
	 * @param msg the message
	 */
	public void chatPlayer(EntityPlayer player, String msg) {
		String muteReason = MutedConfig.instance.users.get(player.name);
		if (muteReason != null) {
			player.packetHandler.sendChatMessage(MessagesConfig.instance.feedbackMuted
					.replace("%reason%", muteReason));
			return;
		}
		
		EventPlayerChat eventPlayerChat = new EventPlayerChat(player, msg);
		this.playerChatEventFirer.fire(eventPlayerChat);
		if (eventPlayerChat.getCancelled()) {
			return;
		}
		
		if (player.permissionUser != null) {
			String chatPrefix = player.permissionUser.getChatPrefix();
			String chatSuffix = player.permissionUser.getChatSuffix();
			
			if (chatPrefix == null || chatPrefix.isEmpty()) {
				chatPrefix = "";
			} else {
				chatPrefix = MessagesConfig.instance.prefixFormat.replace("%prefix%", chatPrefix);
				chatPrefix += " ";
			}
			
			if (chatSuffix == null || chatSuffix.isEmpty()) {
				chatSuffix = "";
			} else {
				chatSuffix = MessagesConfig.instance.suffixFormat.replace("%suffix%", chatSuffix);
				chatSuffix += " ";
			}
			
			this.sendGlobalChatMessage(MessagesConfig.instance.chatFormat
					.replace("%prefix%", chatPrefix)
					.replace("%author%", player.name)
					.replace("%suffix%", chatSuffix)
					.replace("%message%", msg));	
		} else {
			this.sendGlobalChatMessage(MessagesConfig.instance.chatFormat
					.replace("%prefix%", "")
					.replace("%author%", player.name)
					.replace("%suffix%", "")
					.replace("%message%", msg));
		}
	}
	
	/**
	 * Handles a player leave
	 * 
	 * @param player the player
	 */
	public void leavePlayer(EntityPlayer player) {
		Logging.logInfo(player.client.identifier + " has disconnected");
		this.server.clientReceivedPacketID.removeListener(player);
		this.server.clientDisconnected.removeListener(player);	

		if (player.isLoggedIn) {
			try {
				this.foreachPlayer(new InlineForloop<EntityPlayer>() {
					@Override
					public void onEntry(EntityPlayer entry) {
						if (entry.id != player.id &&
							entry.world == player.world) {
								entry.packetHandler.sendPlayerLeave(player.id);
						}
					}
				});
			} catch (Exception ex) {
			}
			
			this.sendGlobalChatMessage(
					MessagesConfig.instance.playerLeft.replace("%player%", player.name));
		}
		
		this.players.remove(player);
		this.updatePlayerCount(true);
		
		this.playerLeaveEventFirer.fire(new EventPlayerLeave(player));
	}
	
	/**
	 * Updates the player count (Discord and heartbeat)
	 * 
	 * @param sendHeartBeat if to send a heartbeat
	 */
	public void updatePlayerCount(boolean sendHeartBeat) {
		DiscordIntegration.update(this.players.size(), Logging.totalWarns, Logging.totalErrors);
		if (sendHeartBeat)
			this.serverListingManager.heartbeat();
	}
	
	/**
	 * Gets the first available player ID
	 * @return the player id or -1
	 */
	public byte getFirstAvailablePlayerID() {
		for (int id = 0; id < ServerConfig.instance.maxPlayers; id++) {
			if (this.getPlayerByID((byte)(id + 1)) == null)
				return (byte)(id + 1);
		}
		
		return -1;
	}

	/**
	 * Gets a player by it's id
	 * 
	 * @param id the id
	 * @return the player or null
	 */
	public EntityPlayer getPlayerByID(byte id) {
		for (EntityPlayer player : this.players.toArray(new EntityPlayer[0])) {
			if (player.id == id)
				return player;
		}
		
		return null;
	}
	
	/**
	 * Gets a player by it's name
	 * 
	 * @param name the name
	 * @return the player or null
	 */
	public EntityPlayer getPlayerByName(String name) {
		for (EntityPlayer player : this.players.toArray(new EntityPlayer[0])) {
			if (player.name.equals(name))
				return player;
		}
		
		return null;
	}
	
	/**
	 * Performs an for loop on the list of players
	 * 
	 * @param inlineForloop the in line for loop object to call
	 */
	public void foreachPlayer(InlineForloop<EntityPlayer> inlineForloop) {
		for (EntityPlayer player : players.toArray(new EntityPlayer[0])) {
			inlineForloop.onEntry(player);
		}
	}
	
	/**
	 * Sends a chat message to all players
	 * 
	 * @param msg the message
	 */
	public void sendGlobalChatMessage(String msg) {
		this.foreachPlayer(new InlineForloop<EntityPlayer>() {
			@Override
			public void onEntry(EntityPlayer arg0) {
				arg0.packetHandler.sendChatMessage(msg);
			}
		});
		
		Logging.logInfo(msg, true, true);
		new Thread() {
			@Override
			public void run() {
				if (ServerConfig.instance.discordWebhookURL != null) {
					int responseCode = DiscordIntegration.submitWebook(
							ServerConfig.instance.discordWebhookURL, ColorUtil.stripColorCodes(msg, '&'));
					
					if (responseCode != HttpURLConnection.HTTP_OK && 
						responseCode != HttpURLConnection.HTTP_NO_CONTENT) {
						Logging.logError("Unable to submit the Discord webhook! (Response code: " + responseCode + ")");
					}
				}	
			}
		}.start();
	}
}