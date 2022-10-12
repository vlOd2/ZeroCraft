package net.fieme.zerocraft.networking.protocol;

import org.apache.commons.lang3.ArrayUtils;

import net.fieme.zerocraft.UndocumentedClass000;
import net.fieme.zerocraft.Utils;
import net.fieme.zerocraft.ZeroCraft;
import net.fieme.zerocraft.configuration.MessagesConfig;
import net.fieme.zerocraft.configuration.ServerConfig;
import net.fieme.zerocraft.game.Player;
import net.fieme.zerocraft.logging.Logging;

/**
 * Manager for player packets
 */
public class PlayerPacketManager {
	private ZeroCraft serverInstance;
	public final Player player;
	
	/**
	 * Manager for player packets
	 * 
	 * @param serverInstance the instance of ZeroCraft the player is connected to
	 * @param player the player to manage
	 */
	public PlayerPacketManager(ZeroCraft serverInstance, Player player) {
		this.serverInstance = serverInstance;
		this.player = player;
	}
	
	/**
	 * Handles a login packet received from this player
	 * 
	 * @param protocolVersion the received protocol version
	 * @param username the received username
	 * @param verificationKey the received verification key
	 */
	public void handleLogin(byte protocolVersion, 
			String username, String verificationKey) {
		if (protocolVersion < 6 && 
			protocolVersion > 7) {
			this.kick("&cUnsupported protocol version!");
			return;
		}
		
		this.player.name = username;
		this.player.protocolVersionNumber = protocolVersion;
		this.player.isLoggedIn = true;
		if (ServerConfig.performNameVerification) {
			String correctHash = Utils.getMD5HashFromStr(ServerConfig.verificationSalt, 
					this.player.name);
			
			if (!verificationKey.equalsIgnoreCase(correctHash)) {
				this.kick("&cUnable to verify your username!");
				return;
			}
		}
		this.serverInstance.foreachPlayer(new UndocumentedClass000<Player>() {
			@Override
			public void func_0000(Player entry) {
				if (player.id != entry.id &&
					player.name.equals(entry.name)) {
					kick("&cYou are already logged in!");
					player.isLoggedIn = false;
				}
			}
		});
		
		if (this.player.isLoggedIn) {
			this.serverInstance.sendGlobalChatMessage(
					MessagesConfig.playerJoined.replace("%player%", this.player.name));
			
			this.player.switchWorld(this.serverInstance.mainWorld);

			if (this.player.classicProtocolExtensions) {
				this.sendChatMessage("&c!!! INCOMPATIBILITY DETECTED !!!");
				this.sendChatMessage("&cYou client has reported that it supports CPE!");
				this.sendChatMessage("&cZeroCraft has currently no support for CPE!");
				this.sendChatMessage("&cCPE features will not work!");
				this.sendChatMessage("&c!!! INCOMPATIBILITY DETECTED !!!");
			}
			
			if (this.player.protocolVersionNumber == 6) {
				this.sendChatMessage("&c!!! INCOMPATIBILITY DETECTED !!!");
				this.sendChatMessage("&cYour client is using an older protocol! (" + 
						this.player.protocolVersionNumber + ")");
				this.sendChatMessage("&cSome features WILL BE UNAVAILABLE/CAUSE ISSUES!");
				this.sendChatMessage("&cYou are recommended to USE A NEWER CLIENT");
				this.sendChatMessage("&c!!! INCOMPATIBILITY DETECTED !!!");
			}	
		}
	}

	/**
	 * Handles a block packet received from this player
	 * 
	 * @param blockX the received block x
	 * @param blockY the received block y
	 * @param blockZ the received block z
	 * @param mode the received block mode (true: place / false: break)
	 * @param blockID the received block ID
	 */
	public void handleBlock(short blockX, 
			short blockY, short blockZ, boolean mode, byte blockID) {
		if (this.player.entity.world.isValidBlockPos(blockX, blockY, blockZ)) {
			this.player.entity.world.setBlock(blockX, blockY, blockZ, (mode ? blockID : 0));
			
			this.serverInstance.foreachPlayer(new UndocumentedClass000<Player>() {
				@Override
				public void func_0000(Player entry) {
					entry.packetHandler.sendBlock(blockX, blockY, blockZ, (mode ? blockID : 0));
				}
			});
		} else {
			if (ServerConfig.antiCheat)
				this.kick("&cAttempted to place block outside allowed boundary!");
			else
				this.sendChatMessage("&cAttempted to place block outside allowed boundary!");
		}
	}
	
	/**
	 * Handles a player tp (position) packet received from this player
	 * 
	 * @param x the received x pos
	 * @param y the received y pos
	 * @param z the received z pos
	 * @param yaw the received yaw
	 * @param pitch the received pitch
	 */
	public void handlePlayerPos(short x, 
			short y, short z, byte yaw, byte pitch) {
		this.player.entity.posX = x;
		this.player.entity.posY = y;
		this.player.entity.posZ = z;
		this.player.entity.yaw = yaw;
		this.player.entity.pitch = pitch;

		this.serverInstance.foreachPlayer(new UndocumentedClass000<Player>() {
			@Override
			public void func_0000(Player entry) {
				if (entry.id != player.id)
					entry.packetHandler.sendPlayerTP(player.id, 
							player.entity.posX, 
							player.entity.posY, 
							player.entity.posZ, 
							player.entity.yaw,
							player.entity.pitch);
			}
		});	
	}
	
	/**
	 * Handles a chat packet received from this player
	 * 
	 * @param msg the received message
	 */
	public void handleChat(String msg) {
		if (msg.startsWith("/")) {
			Logging.logInfo(this.player.name + " executed " + msg);
			this.player.consoleHandler.handleInput(msg.substring(1));
		} else {
			if (this.player.permissionUser != null) {
				this.serverInstance.sendGlobalChatMessage(MessagesConfig.chatFormat
						.replace("%prefix%", this.player.permissionUser.getChatPrefix())
						.replace("%author%", this.player.name)
						.replace("%suffix%", this.player.permissionUser.getChatSuffix())
						.replace("%message%", msg));	
			} else {
				this.serverInstance.sendGlobalChatMessage(MessagesConfig.chatFormat
						.replace("%prefix%", "")
						.replace("%author%", this.player.name)
						.replace("%suffix%", "")
						.replace("%message%", msg));
			}
		}
	}
	
	/**
	 * Sends to the player the player's current world data
	 */
	public void sendWorldData() {
		this.player.sendPacket(new FilledPacket(Packets.login, new Object[] { 
				(byte) this.player.protocolVersionNumber,
				ServerConfig.serverName,
				ServerConfig.serverMOTD,
				(byte) 0x64
		}));
		this.player.sendPacket(new FilledPacket(Packets.levelInit, new Object[0]));
		byte[] compressedData = Utils.compressToGZip(this.player.entity.world.getAsArray());
		
		try {
			int dataChunksCount = Math.round(compressedData.length / 1024) + 1;
			for (int chunk = 0; chunk < dataChunksCount; chunk++) {
				int startIndex = chunk * 1024;
				byte[] chunkData = ArrayUtils.subarray(compressedData, startIndex, startIndex + 1024);
				this.player.sendPacket(new FilledPacket(Packets.levelData, new Object[] { (short) 1024, 
						chunkData, (byte) 0 }));
			}	
		} catch (Exception ex) {
			Logging.logWarn("Unable to send world data chunk to " + this.player.name + "!"
					+ " This will cause issues!");
			this.sendChatMessage("&cUnable to send world data. You might experience issues!");
		}
		
		this.player.sendPacket(new FilledPacket(Packets.levelEnd, new Object[] { 
				(short) this.player.entity.world.width, 
				(short) this.player.entity.world.height, 
				(short) this.player.entity.world.depth }));
	}
	
	/**
	 * Sends a message to this player
	 * 
	 * @param msg the message
	 */
	public void sendChatMessage(String msg) {
		for (String str : Utils.splitStringIntoChunks(msg, 64)) {
			this.player.sendPacket(new FilledPacket(Packets.chat, new Object[] { (byte)0, str }));
		}
	}
	
	/**
	 * Sends a block packet to this player
	 * 
	 * @param x block x
	 * @param y block y
	 * @param z block z
	 * @param id block ID
	 */
	public void sendBlock(int x, int y, int z, byte id) {
		this.player.sendPacket(new FilledPacket(Packets.block2, new Object[] { 
				(short) x, 
				(short) y, 
				(short) z,
				id }));
	}
	
	/**
	 * Sends a player join packet to this player
	 * 
	 * @param id the id of the player who joined
	 * @param name the name of the player who joined
	 * @param x the x pos of the player who joined
	 * @param y the y pos of the player who joined
	 * @param z the z pos of the player who joined
	 * @param yaw the yaw of the player who joined
	 * @param pitch the pitch of the player who joined
	 */
	public void sendPlayerJoin(byte id, String name, 
			short x, short y, short z, byte yaw, byte pitch) {
		this.player.sendPacket(new FilledPacket(Packets.playerConnect, new Object[] {
				id,
				name,
				x, 
				y, 
				z,
				yaw,
				pitch }));
	}
	
	/**
	 * Sends a player teleport packet to this player
	 * 
	 * @param id the id of the player who teleported
	 * @param x the x pos of the player who teleported
	 * @param y the y pos of the player who teleported
	 * @param z the z pos of the player who teleported
	 * @param yaw the yaw of the player who teleported
	 * @param pitch the pitch of the player who teleported
	 */
	public void sendPlayerTP(byte id, 
			short x, short y, short z, byte yaw, byte pitch) {
		this.player.sendPacket(new FilledPacket(Packets.playerTP, new Object[] {
				id,
				x, 
				y, 
				z,
				yaw,
				pitch }));
	}
	
	/**
	 * Sends a player leave packet to this player
	 * 
	 * @param id the id of the player who left
	 */
	public void sendPlayerLeave(byte id) {
		this.player.sendPacket(new FilledPacket(Packets.playerDisconnect, new Object[] { id }));
	}
	
	/**
	 * Kicks this player instance from the server<br>
	 * If no reason is specified (null or empty) then the default reason is used
	 * 
	 * @param reason the reason to kick the player for
	 */
	public void kick(String reason) {
		if (reason == null ||
			reason.isEmpty()) {
			reason = "Kicked by an administrator";
		}
		
		Logging.logInfo("Kicked " + 
				this.player.name + " (" + 
				this.player.client.clientIdentifier + "): " + reason);
		if (this.player.isLoggedIn) {
			this.serverInstance.sendGlobalChatMessage(MessagesConfig.playerKick
					.replace("%player%", this.player.name)
					.replace("%reason%", reason));	
		}
		
		this.player.sendPacket(new FilledPacket(Packets.kick, new Object[] { reason }));
		try {
			if (this.player.client.clientWriteBuffer.position() > 0) {
				this.player.client.clientWriteBuffer.flip();
				this.player.client.clientSocketChannel.write(this.player.client.clientWriteBuffer);
				this.player.client.clientWriteBuffer.compact();
			}	
		} catch (Exception ex) {
		}
		this.player.client.close();
	}
	
	/**
	 * Teleports this player to the specified block position
	 * 
	 * @param x new block x pos
	 * @param y new block y pos
	 * @param z new block z pos
	 * @param yaw new yaw
	 * @param pitch new pitch
	 */
	public void teleport(short x, short y, short z, 
			byte yaw, byte pitch) {
		this.sendPlayerTP(
				(byte) -1, (short)(x << 5), (short)(y << 5), (short)(z << 5), yaw, pitch);
		this.handlePlayerPos((short)(x << 5), (short)(y << 5), (short)(z << 5), yaw, pitch);
	}
	
	/**
	 * Teleports this player to the specified position
	 * 
	 * @param x new x pos
	 * @param y new y pos
	 * @param z new z pos
	 * @param yaw new yaw
	 * @param pitch new pitch
	 */
	public void teleportRaw(short x, short y, short z, 
			byte yaw, byte pitch) {
		this.sendPlayerTP(
				(byte) -1, x, y, z, yaw, pitch);
		this.handlePlayerPos(x, y, z, yaw, pitch);
	}
	
	/**
	 * Handles a received packet
	 * 
	 * @param packet the received packet
	 */
	public void handlePacket(FilledPacket packet) {
		if (packet.packet.packetID == PacketType.LOGIN.ordinal()) {
			if (this.player.isLoggedIn) {
				this.kick("Attempted to login but already logged in!");
				return;
			}
			
			byte protocolVersion = (byte)packet.packetFieldsValues[0];
			String username = (String)packet.packetFieldsValues[1];
			String verificationKey = (String)packet.packetFieldsValues[2];
			byte clientOPStatus = (byte) packet.packetFieldsValues[3];

			if (clientOPStatus == 0x42)
				this.player.classicProtocolExtensions = true;
				
			this.handleLogin(protocolVersion, username, verificationKey);
		} else {
			if (!this.player.isLoggedIn) {
				this.kick("Attempted to perform operation but not logged in!");
				return;
			}
			
			if (packet.packet.packetID == PacketType.BLOCK.ordinal()) {
				short blockX = (short)packet.packetFieldsValues[0];
				short blockY = (short)packet.packetFieldsValues[1];
				short blockZ = (short)packet.packetFieldsValues[2];
				byte mode = (byte)packet.packetFieldsValues[3];
				byte blockID = (byte)packet.packetFieldsValues[4];
				
				this.handleBlock(blockX, blockY, blockZ, (mode == 0x01 ? true : false), blockID);
			} else if (packet.packet.packetID == PacketType.PLAYER_TP.ordinal()) {
				short x = (short)packet.packetFieldsValues[1];
				short y = (short)packet.packetFieldsValues[2];
				short z = (short)packet.packetFieldsValues[3];
				byte yaw = (byte)packet.packetFieldsValues[4];
				byte pitch = (byte)packet.packetFieldsValues[5];
				
				this.handlePlayerPos(x, y, z, yaw, pitch);
			} else if (packet.packet.packetID == PacketType.CHAT.ordinal()) {
				String message = (String)packet.packetFieldsValues[1];
				this.handleChat(message);
			}
		} 
	}
}
