package net.fieme.zerocraft;

import java.awt.Color;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Scanner;

import javax.swing.JOptionPane;

import net.fieme.zerocraft.configuration.MessagesConfig;
import net.fieme.zerocraft.configuration.ServerConfig;
import net.fieme.zerocraft.console.ConsoleHandler;
import net.fieme.zerocraft.event.EventBasicObject;
import net.fieme.zerocraft.event.EventHandler;
import net.fieme.zerocraft.event.EventListener;
import net.fieme.zerocraft.game.Entity;
import net.fieme.zerocraft.game.Player;
import net.fieme.zerocraft.game.World;
import net.fieme.zerocraft.gui.Window;
import net.fieme.zerocraft.gui.WindowMain;
import net.fieme.zerocraft.logging.Logging;
import net.fieme.zerocraft.networking.Server;
import net.fieme.zerocraft.permission.PermissionGroup;
import net.fieme.zerocraft.plugins.PluginManager;

public class ZeroCraft implements EventListener {
	/**
	 * The current ZeroCraft instance
	 */
	public static ZeroCraft instance;
	public static final double VERSION = 1.0;
	public static final String VERSION_STR = "ZeroCraft/" + VERSION;
	public static final String VERSION_DISPLAYABLE_STR = "ZeroCraft v" + VERSION;
	public boolean isRunning = false;
	public WindowMain windowMain;
	public Server server;
	public ConsoleHandler consoleHandler;
	public PluginManager pluginManager;
	public ServerListingManager serverListingManager;
	public World mainWorld;
	public final ArrayList<Player> players = new ArrayList<Player>();
	public final ArrayList<World> worlds = new ArrayList<World>();
	public final ArrayList<PermissionGroup> permissionGroups = new ArrayList<PermissionGroup>();
	
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
			if (this.loadConfigs()) {
				Logging.logInfo("Loaded server configuration");
				
				Logging.logInfo("Loading main level...");
				this.mainWorld = this.loadWorld("main");
				if (this.mainWorld == null) {
					Logging.logWarn("Unable to find the main level! Creating...");
					this.mainWorld = this.generateWorld("main", 
							(short) 128, 
							(short) 256, 
							(short) 128, 
							false,
							false);
					this.saveWorld(this.mainWorld);
				}
				this.addWorld(this.mainWorld);
				Logging.logInfo("Loaded main level!");
				
				if (ServerConfig.enablePlugins) {
					Logging.logInfo("Loading plugins...");
					this.pluginManager = new PluginManager();
					this.pluginManager.loadPluginsInDir();
					Logging.logInfo("Loaded plugins");
				}
				
				Logging.logInfo("Starting listener on " + ServerConfig.listenIP + ":" + ServerConfig.listenPort + "...");
				this.server = new Server(this, ServerConfig.listenIP, ServerConfig.listenPort);
				this.server.start();
				try {
					if (!ServerConfig.serverURL.contains("/")) {
						throw new Exception();
					} else {
						String heartbeatServer = ServerConfig.serverURL.split("/", 2)[0];
						String heartbeatURL = "/" + ServerConfig.serverURL.split("/", 2)[1];
						int heartbeatPort = 80;
						
						if (ServerConfig.serverURL.contains(":")) {
							String heartbeatPortRaw = ServerConfig.serverURL.split(":", 2)[1];
							if (!Utils.isNumeric(heartbeatPortRaw, false)) throw new Exception();
							heartbeatPort = Integer.valueOf(heartbeatPortRaw);
						}
						
						this.serverListingManager = new ServerListingManager(this, 
								heartbeatServer, heartbeatURL, heartbeatPort);
						this.serverListingManager.start();
					}	
				} catch (Exception ex) {
					ex.printStackTrace();
					Logging.logError("Invalid server heartbeat URL specified!"
							+ " Make sure it is in the following format:"
							+ " <hostname>[:port]/<url> (example: minecraft.net/heartbeat.jsp)");
				}
				Logging.logInfo("Started listener on " + ServerConfig.listenIP + ":" + ServerConfig.listenPort);
				
				Logging.logInfo("Starting Discord integration...");
				DiscordIntegration.init();
				DiscordIntegration.update(0, Logging.totalWarns, Logging.totalErrors);
				Logging.logInfo("Started Discord integration");
				
				Logging.logInfo("Server started! Took " + 
						(System.currentTimeMillis() - startTime) + "ms");
				
				this.consoleHandler = new ConsoleHandler("# ");	
				this.consoleHandler.start();
				
				while (this.isRunning) {
					if (Thread.interrupted())
						break;
				}
			}
		} catch (Exception ex) {
			Logging.logFatal(Utils.getExceptionStackTraceAsStr(ex));
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
				for (Player player : this.players.toArray(new Player[0])) {
					player.packetHandler.kick(MessagesConfig.serverKickShutdown);
					player = null;
				}	
			} catch (Exception ex) {
			}
			this.players.clear();
			Logging.logInfo("Kicked players");
			
			Logging.logInfo("Destroying Discord integration...");
			DiscordIntegration.destroy();
			Logging.logInfo("Destroyed Discord integration");	
			
			if (ServerConfig.enablePlugins) {
				Logging.logInfo("Unloading plugins...");
				try {
					this.pluginManager.unloadAll();
				} catch (Exception ex) {
				}
				Logging.logInfo("Unloaded plugins");	
			}
			
			Logging.logInfo("Saving worlds...");
			for (World world : this.worlds.toArray(new World[0])) {
				this.saveWorld(world);
				world = null;
			}
			this.worlds.clear();
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
		} catch (Exception ex) {
			Logging.logFatal(Utils.getExceptionStackTraceAsStr(ex));
		}
	}
	
	private boolean loadConfigs() {
		try {
			File serverConfigFile = new File("server.properties");
			File messagesConfigFile = new File("messages.properties");

			if (!serverConfigFile.exists()) serverConfigFile.createNewFile();
			if (!messagesConfigFile.exists()) messagesConfigFile.createNewFile();

			FileReader serverConfigReader = new FileReader(serverConfigFile);
			FileReader messagesConfigReader = new FileReader(messagesConfigFile);
			
			Properties serverConfig = new Properties();
			Properties messagesConfig = new Properties();

			serverConfig.load(serverConfigReader);
			messagesConfig.load(messagesConfigReader);
			
			ServerConfig.listenIP = serverConfig.getProperty("listen-ip", 
					ServerConfig.listenIP);
			ServerConfig.listenPort = Integer.parseInt(
					serverConfig.getProperty("listen-port", 
							"" + ServerConfig.listenPort));
			ServerConfig.maxPlayers = Integer.parseInt(
					serverConfig.getProperty("max-players", 
							"" + ServerConfig.maxPlayers));
			ServerConfig.verificationSalt = serverConfig.getProperty("verification-salt", 
					ServerConfig.verificationSalt);
			ServerConfig.usePermissionSystem = Boolean.parseBoolean(
					serverConfig.getProperty("use-permission-system", 
							"" + ServerConfig.usePermissionSystem));
			ServerConfig.serverURL = serverConfig.getProperty("server-url", 
					ServerConfig.serverURL);
			ServerConfig.performNameVerification = Boolean.parseBoolean(
					serverConfig.getProperty("perform-name-verification", 
							"" + ServerConfig.performNameVerification));
			ServerConfig.antiCheat = Boolean.parseBoolean(
					serverConfig.getProperty("anti-cheat", 
							"" + ServerConfig.antiCheat));
			ServerConfig.serverName = serverConfig.getProperty("server-name", 
					ServerConfig.serverName);
			ServerConfig.serverMOTD = serverConfig.getProperty("server-motd", 
					ServerConfig.serverMOTD);

			serverConfig.setProperty("listen-ip", ServerConfig.listenIP);
			serverConfig.setProperty("listen-port", "" + ServerConfig.listenPort);
			serverConfig.setProperty("max-players", "" + ServerConfig.maxPlayers);
			serverConfig.setProperty("verification-salt", ServerConfig.verificationSalt);
			serverConfig.setProperty("use-permission-system", "" + ServerConfig.usePermissionSystem);
			serverConfig.setProperty("server-url", ServerConfig.serverURL);
			serverConfig.setProperty("perform-name-verification", "" + ServerConfig.performNameVerification);
			serverConfig.setProperty("anti-cheat", "" + ServerConfig.antiCheat);
			serverConfig.setProperty("server-name", ServerConfig.serverName);
			serverConfig.setProperty("server-motd", ServerConfig.serverMOTD);
			
			FileWriter serverConfigWriter = new FileWriter(serverConfigFile);
			FileWriter messagesConfigWriter = new FileWriter(messagesConfigFile);
			
			serverConfig.store(serverConfigWriter, "ZeroCraft Configuration");
			messagesConfig.store(messagesConfigWriter, "ZeroCraft Messages Configuration");

			serverConfigReader.close();
			serverConfigWriter.close();
			messagesConfigReader.close();
			messagesConfigWriter.close();
			
			return true;
		} catch (Exception ex) {
			Logging.logFatal("Unable to load server configuration: " + 
					Utils.getExceptionStackTraceAsStr(ex));
			this.stop();
			return false;
		}
	}
	
	@EventHandler
	public void windowMain_inputSubmitHandler(EventBasicObject e) {
		if (e.id != WindowMain.EVENT_INPUTSUBMITTED_ID) return;
		String input = (String)e.object;

		if (input.isEmpty()) {
			JOptionPane.showMessageDialog(
					null,
					"Invalid input specified!",
					"ZeroCraft",
					JOptionPane.ERROR_MESSAGE
			);
		} else {
			this.windowMain.appendText("> " + input);
			
			if (!this.isRunning && !input.startsWith("start")) {
				this.windowMain.appendText("The server is not running!", Color.red);
			} else if (!this.isRunning && input.startsWith("start")) {
				new Thread() {
					public void run() {
						ZeroCraft.instance.start();
					}
				}.start();
			} else if (this.isRunning && input.startsWith("start")) {
				this.windowMain.appendText("The server is already running!", Color.red);
			} else {
				this.consoleHandler.handleInput(input);
			}
		}
	}
	
	@EventHandler
	public void windowMain_windowClose(EventBasicObject e) {
		if (e.id != Window.EVENT_WINDOWCLOSE_ID) return;
		if (this.isRunning)
			this.stop();
		this.windowMain.close();
	}
	
	/**
	 * Gets a world by it's name
	 *
	 * @param name the name
	 * @return the world or null
	 */
	public World getWorldByName(String name) {
		for (World world : this.worlds.toArray(new World[0])) {
			if (world.name.equalsIgnoreCase(name))
				return world;
		}
		
		return null;
	}
	
	/**
	 * Adds an world
	 * 
	 * @param world the world
	 */
	public void addWorld(World world) {
		if (getWorldByName(world.name) != null) return;
		this.worlds.add(world);
	}
	
	/**
	 * Removes an world
	 * 
	 * @param name the name of the world
	 */
	public void removeWorld(String name) {
		World world = getWorldByName(name);
		if (world == null) return;
		this.worlds.remove(world);
	}
	
	/**
	 * Loads an world with the specified name<br>
	 * If the world cannot be read or found, null is returned
	 * 
	 * @param name the name of the world
	 * @return the world or null
	 */
	public World loadWorld(String name) {
		try {
			File file = new File(name.toLowerCase() + ".world");
			
			if (!file.exists()) {
				return null;
			}
			
			FileInputStream fileInputStream = new FileInputStream(file);
			ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
			World world = (World) objectInputStream.readObject();
			world.func_0000();
			
			objectInputStream.close();
			fileInputStream.close();
			
			return world;	
		} catch (Exception ex) {
			return null;
		}
	}
	
	/**
	 * Saves the specified world to a file<br>
	 * If the world is already saved, it is overwritten
	 * 
	 * @param world the world
	 */
	public void saveWorld(World world) {
		try {
			File file = new File(world.name.toLowerCase() + ".world");
			if (!file.exists()) file.createNewFile();
			
			FileOutputStream fileOutputStream = new FileOutputStream(file);
			ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
			
			objectOutputStream.writeObject(world);
			objectOutputStream.flush();	
			
			objectOutputStream.close();
			fileOutputStream.close();	
		} catch (Exception ex) {
		}
	}
	
	public World generateWorld(String name, short width, 
			short height, short depth, boolean useNoiseGenerator, boolean useFarlandsGenerator) {
		Logging.logInfo("Generating world \"" + name + "\"...");
		World world = new World(name, width, height, depth);
		if (useNoiseGenerator)
			world.generateNoiseTerrain();
		else if (useFarlandsGenerator)
			world.generateFarLandsTerrain();
		else
			world.generateFlatTerrain();
		Logging.logInfo("Generated world \"" + name + "\"");
		
		return world;
	}
	
	/**
	 * Gets the first available player ID
	 * @return the player id or -1
	 */
	public byte getFirstAvailablePlayerID() {
		for (int id = 0; id < ServerConfig.maxPlayers; id++) {
			if (this.getPlayerByID((byte)(id + 1)) == null)
				return (byte)(id + 1);
		}
		
		return -1;
	}
	
	/**
	 * Gets a player by it's entity
	 * 
	 * @param ent the entity
	 * @return the player or null
	 */
	public Player getPlayerByEntity(Entity ent) {
		for (Player player : this.players.toArray(new Player[0])) {
			if (player.entity == ent)
				return player;
		}
		
		return null;
	}
	
	/**
	 * Gets a player by it's id
	 * 
	 * @param id the id
	 * @return the player or null
	 */
	public Player getPlayerByID(byte id) {
		for (Player player : this.players.toArray(new Player[0])) {
			if (player.id == id)
				return player;
		}
		
		return null;
	}
	
	/**
	 * Sends a chat message to all players
	 * 
	 * @param msg the message
	 */
	public void sendGlobalChatMessage(String msg) {
		this.foreachPlayer(new UndocumentedClass000<Player>() {
			@Override
			public void func_0000(Player arg0) {
				arg0.packetHandler.sendChatMessage(msg);
			}
		});
		Logging.logInfo(msg);
	}

	/**
	 * NOTE: Not intended to be used function
	 * 
	 * @param arg0 unknown
	 */
	public void foreachPlayer(UndocumentedClass000<Player> arg0) {
		for (Player player : players.toArray(new Player[0])) {
			arg0.func_0000(player);
		}
	}
}