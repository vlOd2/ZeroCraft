package net.fieme.zerocraft.configuration;

public class MessagesConfig implements Config {
	public static MessagesConfig instance;
	public String prefixFormat = "&8[%prefix%&8]&f";
	public String suffixFormat = "&8<%suffix%&8>&f";
	public String chatFormat = "%prefix%%author%%suffix%&8:&f %message%";
	public String feedbackInvalidCommand = "&cInvalid command! See &e/help&c for help";
	public String feedbackInvalidArguments = "&cInvalid arguments provided!";
	public String feedbackNoPermission = "&cInsufficient permissions!";
	public String feedbackMuted = "&cYou are muted: &f%reason%";
	public String playerJoined = "&8(&a+&8) &f%player%";
	public String playerLeft = "&8(&c-&8) &f%player%";
	public String playerKick = "&8(&cKICK&8) &f%player% &8(%reason%&8)";
	public String playerMute = "&a%player% &ahas been &emuted";
	public String playerUnmute = "&a%player% &ahas been &eunmuted";
	public String playerSwitchedWorld = "&a%player% &8-> &e%world%";
	public String serverKickFull = "&cServer full!";
	public String serverKickShutdown = "&cServer shutting down!";
	public String serverKickTooManyConnections = "&cToo many connections on this IP address!";
}
