package net.fieme.zerocraft.game;

import org.apache.commons.lang3.ArrayUtils;

import net.fieme.zerocraft.AllowedCharacters;
import net.fieme.zerocraft.ColorUtil;
import net.fieme.zerocraft.InlineForloop;
import net.fieme.zerocraft.Utils;
import net.fieme.zerocraft.ZeroCraft;
import net.fieme.zerocraft.configuration.BannedConfig;
import net.fieme.zerocraft.configuration.MessagesConfig;
import net.fieme.zerocraft.configuration.ServerConfig;
import net.fieme.zerocraft.logging.Logging;
import net.fieme.zerocraft.networking.protocol.FilledPacket;
import net.fieme.zerocraft.networking.protocol.PacketType;
import net.fieme.zerocraft.networking.protocol.Packets;

/**
 * Manager for player packets
 */
public class EntityPlayerPacketManager {
	private ZeroCraft serverInstance;
	public final EntityPlayer player;
	
	/**
	 * Manager for player packets
	 * 
	 * @param serverInstance the ZeroCraft instance
	 * @param player the player to manage
	 */
	public EntityPlayerPacketManager(ZeroCraft serverInstance, EntityPlayer player) {
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
			this.player.kick("&cUnsupported protocol version!");
			return;
		}
		
		this.player.name = username.trim();
		this.player.protocolVersionNumber = protocolVersion;
		this.player.isLoggedIn = true;
		
		// Name check
		if (this.player.name.length() < 3 || !AllowedCharacters.isStringValid(this.player.name)) {
			this.player.isLoggedIn = false;
			this.player.kick("&cYour username is invalid!");
			return;
		}
		// Name authentication check
		if (ServerConfig.instance.performNameVerification) {
			String correctHash = Utils.getMD5HashFromStr(ServerConfig.instance.verificationSalt, 
					this.player.name);
			
			if (!verificationKey.equalsIgnoreCase(correctHash)) {
				this.player.kick("&cUnable to verify your username!");
				return;
			}
		}
		// Already connected check
		this.serverInstance.foreachPlayer(new InlineForloop<EntityPlayer>() {
			@Override
			public void onEntry(EntityPlayer entry) {
				if (player.id != entry.id &&
					player.name.equals(entry.name)) {
					player.kick("&cYou are already logged in!");
				}
			}
		});
		
		String ipBannedReason = BannedConfig.instance.ipaddresses.get(
				this.player.client.identifier.networkAddress);
		String userBannedReason = BannedConfig.instance.users.get(this.player.name);
		
		// Ban check
		if (ipBannedReason != null) {
			this.player.kick("Banned: " + ipBannedReason);
			return;
		} else if (userBannedReason != null) {
			this.player.kick("Banned: " + userBannedReason);
			return;
		}

		if (this.player.isLoggedIn) {
			World mainWorld = this.serverInstance.worldManager.getWorldByName(
					ServerConfig.instance.mainWorld);
			if (mainWorld == null) {
				this.player.kick("&cThe main level wasn't loaded properly!");
				return;
			}
			
			this.serverInstance.loginPlayer(this.player);
			this.player.switchWorld(mainWorld);
			this.player.updatePermissionUser();
			
			if (this.player.protocolVersionNumber < 7) {
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
		if (mode && (WorldTiles.getFromID(blockID) == WorldTiles.air)) {
			if (ServerConfig.instance.antiCheat) {
				this.player.kick("&cAttempted to place block with invalid id!");
			} else {
				this.sendChatMessage("&cAttempted to place block with invalid id!");
				this.player.packetHandler.sendBlock(blockX, blockY, blockZ, WorldTiles.air.id);
			}	
		} else if (this.player.world.isValidBlockPos(blockX, blockY, blockZ)) {
			byte currentBlockID = this.player.world.getBlock(blockX, blockY, blockZ);
			
			if (!mode && !this.player.permissionUser.hasPermission("zerocraft.world.break", false)) {
				this.sendChatMessage(MessagesConfig.instance.feedbackNoPermission);
				this.sendBlock(blockX, blockY, blockZ, currentBlockID);
				return;
			} else if (mode && !this.player.permissionUser.hasPermission("zerocraft.world.place", false)) {
				this.sendChatMessage(MessagesConfig.instance.feedbackNoPermission);
				this.sendBlock(blockX, blockY, blockZ, WorldTiles.air.id);
				return;
			}
			
			if (player.forcePlaceBlock != null) {
				blockID = (byte)player.forcePlaceBlock.id;
			}
			
			boolean canBreakWater = 
					this.player.permissionUser.hasPermission("zerocraft.world.break.special.water", false);
			boolean canBreakLava = 
					this.player.permissionUser.hasPermission("zerocraft.world.break.special.water", false);
			boolean canPlaceWater = 
					this.player.permissionUser.hasPermission("zerocraft.world.place.special.water", false);
			boolean canPlaceLava = 
					this.player.permissionUser.hasPermission("zerocraft.world.place.special.water", false);
			
			if ((((currentBlockID == WorldTiles.water.id || currentBlockID == WorldTiles.stillwater.id) && !canBreakWater) || 
					((currentBlockID == WorldTiles.lava.id || currentBlockID == WorldTiles.stilllava.id) && !canBreakLava)) && 
					!mode) {
				this.sendChatMessage(MessagesConfig.instance.feedbackNoPermission);
				this.sendBlock(blockX, blockY, blockZ, currentBlockID);
				return;
			}
			
			if ((((blockID == WorldTiles.water.id || blockID == WorldTiles.stillwater.id) && !canPlaceWater) || 
					((blockID == WorldTiles.lava.id || blockID == WorldTiles.stilllava.id) && !canPlaceLava)) && 
					mode) {
				this.sendChatMessage(MessagesConfig.instance.feedbackNoPermission);
				this.sendBlock(blockX, blockY, blockZ, WorldTiles.air.id);
				return;
			}
			
			this.serverInstance.blockPlayer(player, blockX, blockY, blockZ, (mode ? blockID : 0));
		} else {
			if (ServerConfig.instance.antiCheat) {
				this.player.kick("&cAttempted to place block outside allowed boundary!");
			} else {
				this.sendChatMessage("&cAttempted to place block outside allowed boundary!");
				this.player.packetHandler.sendBlock(blockX, blockY, blockZ, WorldTiles.air.id);
			}
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
		if (!((this.player.posX != x) || 
			(this.player.posY != y) || 
			(this.player.posZ != z) || 
			(this.player.yaw != yaw) || 
			(this.player.pitch != pitch))) {
			return;
		}

		this.serverInstance.movePlayer(player, x, y, z, yaw, pitch);
	}
	
	/**
	 * Handles a chat packet received from this player
	 * 
	 * @param msg the received message
	 */
	public void handleChat(String msg) {
		if (msg.startsWith("/")) {
			if (!this.player.permissionUser.hasPermission("zerocraft.chat.command", false)) {
				this.sendChatMessage(MessagesConfig.instance.feedbackNoPermission);
				return;
			}
			
			Logging.logInfo(this.player.name + " executed " + msg);
			this.player.consoleHandler.handleInput(msg.substring(1));
		} else {
			if (!player.permissionUser.hasPermission("zerocraft.chat", false)) {
				player.packetHandler.sendChatMessage(MessagesConfig.instance.feedbackNoPermission);
				return;
			}
			
			String strippedMsg = ColorUtil.stripColorCodes(msg, '&');
			if (!msg.equals(strippedMsg) || 
				strippedMsg.isEmpty() ||
				!AllowedCharacters.isStringValid(strippedMsg)) {
				if (ServerConfig.instance.antiCheat) {
					player.kick("&cAttempted to send an invalid chat message!");
				} else {
					player.packetHandler.sendChatMessage("&cAttempted to send an invalid chat message!");
				}
				return;
			}
			msg = ColorUtil.cleanColorCodes(strippedMsg.replace("%", "&"), '&');
			
			this.serverInstance.chatPlayer(this.player, msg);
		}
	}
	
	/**
	 * Sends to the player the player's current world data
	 */
	public void sendWorldData() {
		this.player.sendPacket(new FilledPacket(Packets.login, new Object[] { 
				(byte) this.player.protocolVersionNumber,
				ServerConfig.instance.serverName,
				ServerConfig.instance.serverMOTD
				.replace("%player%", this.player.name),
				(byte) 0x64
		}));
		
		if (this.player.classicProtocolExtensions && 
			ServerConfig.instance.experimental_useVeryBadCPEImplementation) {
			this.player.sendPacket(new FilledPacket(Packets.cpe_extinfo, new Object[] { 
					ZeroCraft.VERSION_STR,
					(short)1
			}));
			
			this.player.sendPacket(new FilledPacket(Packets.cpe_extentry, new Object[] { 
					"BlockPermissions",
					(int)1
			}));
		}
		
		this.player.sendPacket(new FilledPacket(Packets.levelInit, new Object[0]));
		byte[] compressedData = new byte[0];
		
		if (this.player.protocolVersionNumber < 7) {
			compressedData = Utils.compressToGZip(this.player.world.getAsArrayLegacy());
		} else {
			compressedData = Utils.compressToGZip(this.player.world.getAsArray());
		}
		
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
				(short) this.player.world.width, 
				(short) this.player.world.height, 
				(short) this.player.world.depth }));
	}
	
	/**
	 * Sends a message to this player
	 * 
	 * @param msg the message
	 */
	public void sendChatMessage(String msg) {
		msg = ColorUtil.cleanColorCodes(msg, '&');
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
		if (this.player.protocolVersionNumber < 7) {
			id = WorldTiles.getFromIDLegacy(id).id;
		}
		
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
	 * Handles a received packet
	 * 
	 * @param packet the received packet
	 */
	public void handlePacket(FilledPacket packet) {
		if (packet == null) {
			this.player.kick("&cInternal server error!");
			return;
		}
		
		if (packet.packet.packetID == PacketType.LOGIN.id) {
			if (this.player.isLoggedIn) {
				this.player.kick("&cAttempted to login but already logged in!");
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
				this.player.kick("&cAttempted to perform protocol operations but not logged in!");
				return;
			}
			
			if (packet.packet.packetID == PacketType.BLOCK.id) {
				short blockX = (short)packet.packetFieldsValues[0];
				short blockY = (short)packet.packetFieldsValues[1];
				short blockZ = (short)packet.packetFieldsValues[2];
				byte mode = (byte)packet.packetFieldsValues[3];
				byte blockID = (byte)packet.packetFieldsValues[4];
				
				this.handleBlock(blockX, blockY, blockZ, (mode == 0x01 ? true : false), blockID);
			} else if (packet.packet.packetID == PacketType.PLAYER_TP.id) {
				short x = (short)packet.packetFieldsValues[1];
				short y = (short)packet.packetFieldsValues[2];
				short z = (short)packet.packetFieldsValues[3];
				byte yaw = (byte)packet.packetFieldsValues[4];
				byte pitch = (byte)packet.packetFieldsValues[5];
				
				this.handlePlayerPos(x, y, z, yaw, pitch);
			} else if (packet.packet.packetID == PacketType.CHAT.id) {
				String message = (String)packet.packetFieldsValues[1];
				this.handleChat(message);
			} else if (packet.packet.packetID == PacketType.CPE_EXTENTRY.id) {
				String extName = (String)packet.packetFieldsValues[0];
				this.player.supportedCPEList.add(extName);
			}
		} 
	}
}
