package net.fieme.zerocraft.configuration;

public class ServerConfig implements Config {
	public static ServerConfig instance;
	public String listenIP = "0.0.0.0";
	public int listenPort = 25565;
	public int maxPlayers = 64;
	public int maxPlayersPerIP = 5;
	public int maxPlayerPackets = 100;
	public String mainWorld = "main";
	public boolean antiCheat = false;
	public boolean experimental_enablePluginSystem = false;
	public boolean experimental_useVeryBadCPEImplementation = false;
	public String verificationSalt = "!!!CHANGEME!!!";
	public String serverURL = "api.betacraft.uk/heartbeat.jsp";
	public String discordWebhookURL = null;
	public boolean performNameVerification = true;
	public String serverName = "&aZeroCraft &bServer";
	public String serverMOTD = "&aWelcome, &b%player%&a!";
	public String[] serverRules = {};
}