package net.fieme.zerocraft.game;

import java.nio.BufferOverflowException;
import java.util.ArrayList;

import net.fieme.zerocraft.InlineForloop;
import net.fieme.zerocraft.Utils;
import net.fieme.zerocraft.ZeroCraft;
import net.fieme.zerocraft.configuration.MessagesConfig;
import net.fieme.zerocraft.configuration.PermissionsConfig;
import net.fieme.zerocraft.console.PlayerConsoleHandler;
import net.fieme.zerocraft.event.EventBasicObject;
import net.fieme.zerocraft.event.EventBasicTuple;
import net.fieme.zerocraft.event.EventHandler;
import net.fieme.zerocraft.event.EventListener;
import net.fieme.zerocraft.logging.Logging;
import net.fieme.zerocraft.networking.Client;
import net.fieme.zerocraft.networking.Server;
import net.fieme.zerocraft.networking.protocol.FilledPacket;
import net.fieme.zerocraft.networking.protocol.Packet;
import net.fieme.zerocraft.networking.protocol.PacketType;
import net.fieme.zerocraft.networking.protocol.Packets;
import net.fieme.zerocraft.permission.PermissionGroup;
import net.fieme.zerocraft.permission.PermissionUser;

/**
 * A connected player
 */
public class EntityPlayer extends EntityAlive implements EventListener {
	private ZeroCraft serverInstance;
	public byte id;
	public String name;
	public boolean isLoggedIn;
	public Client client;
	public int protocolVersionNumber;
	public boolean classicProtocolExtensions;
	public final ArrayList<String> supportedCPEList = new ArrayList<String>();
	public EntityPlayerPacketManager packetHandler;
	public PlayerConsoleHandler consoleHandler;
	public PermissionUser permissionUser;
	public WorldTile forcePlaceBlock;
	
	/**
	 * A connected player
	 * 
	 * @param serverInstance the ZeroCraft instance
	 * @param client the connection client associated
	 * @param id the player id
	 */
	public EntityPlayer(ZeroCraft serverInstance, Client client, byte id) {
		this.serverInstance = serverInstance;
		this.client = client;
		this.id = id;
		this.packetHandler = new EntityPlayerPacketManager(this.serverInstance, this);
		this.consoleHandler = new PlayerConsoleHandler(this);
		this.serverInstance.joinPlayer(this);
	}
	
	/**
	 * Sends a filled packet to this player
	 * 
	 * @param packet the packet
	 */
	public void sendPacket(FilledPacket packet) {
		try {
			if (client.writeBuffer == null) return;
			client.writeBuffer.put(packet.packageToByteArray());
		} catch (BufferOverflowException ex) {
			try {
				if (client.writeBuffer.position() > 0) {
					client.writeBuffer.flip();
					client.socketChannel.write(client.writeBuffer);
					client.writeBuffer.compact();
				}
				client.writeBuffer.put(packet.packageToByteArray());
			} catch (Exception ex2) {
				ex2.printStackTrace();
				Logging.logSevere("Unable to send packet to " + client.identifier + 
						" even after cleaning the write buffer!"
						+ " There must be a lot of packets that are sent"
						+ " or something has gone wrong!");
				this.kick("&cAn internal server error has occured!");
			}
		}
	}

	/**
	 * Kicks this player from the server<br>
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
				this.name + " (" + 
				this.client.identifier + "): " + reason, true, true);
		boolean showKickMessage = this.isLoggedIn;
		
		this.sendPacket(new FilledPacket(Packets.kick, new Object[] { reason }));
		try {
			if (this.client.writeBuffer.position() > 0) {
				this.client.writeBuffer.flip();
				this.client.socketChannel.write(this.client.writeBuffer);
				this.client.writeBuffer.compact();
			}	
		} catch (Exception ex) {
		}
		
		this.client.close();
		this.isLoggedIn = false;
		
		if (showKickMessage && this.name != null) {
			this.serverInstance.sendGlobalChatMessage(MessagesConfig.instance.playerKick
					.replace("%player%", this.name)
					.replace("%reason%", reason));		
		}
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
		this.packetHandler.sendPlayerTP(
				(byte) -1, (short)(x << 5), (short)(y << 5), (short)(z << 5), yaw, pitch);
		this.packetHandler.handlePlayerPos((short)(x << 5), (short)(y << 5), (short)(z << 5), yaw, pitch);
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
		this.packetHandler.sendPlayerTP(
				(byte) -1, x, y, z, yaw, pitch);
		this.packetHandler.handlePlayerPos(x, y, z, yaw, pitch);
	}
	
	/**
	 * Switches the world of this player
	 * 
	 * @param world the world
	 */
	public void switchWorld(World world) {
		if (this.world != null) {
			this.serverInstance.foreachPlayer(new InlineForloop<EntityPlayer>() {
				@Override
				public void onEntry(EntityPlayer entry) {
					if (entry.id != id &&
						entry.world == world) {
						entry.packetHandler.sendPlayerLeave(id);
					}
				}
			});
			this.world.removeEntity(this);
		}
			
		if (world != null) {
			world.addEntity(this);
			this.packetHandler.sendWorldData();
			
			this.serverInstance.sendGlobalChatMessage(
					MessagesConfig.instance.playerSwitchedWorld
					.replace("%player%", this.name)
					.replace("%world%", world.name));

			this.teleport(world.spawnX,
					world.spawnY, 
					world.spawnZ,
					world.spawnYaw,
					world.spawnPitch);
			
			this.packetHandler.sendPlayerJoin((byte) -1, this.name, 
					this.posX,
					this.posY,
					this.posZ, 
					this.yaw,
					this.pitch);
			
			this.serverInstance.foreachPlayer(new InlineForloop<EntityPlayer>() {
				@Override
				public void onEntry(EntityPlayer entry) {
					if (entry.id != id &&
						entry.world == world) {
						entry.packetHandler.sendPlayerJoin(id, name, 
								posX, 
								posY, 
								posZ, 
								yaw, 
								pitch);
						packetHandler.sendPlayerJoin(entry.id, entry.name, 
								entry.posX, 
								entry.posY, 
								entry.posZ,
								entry.yaw,
								entry.pitch);
					}
				}
			});
		}
	}
	
	/**
	 * Updates this user's permissions
	 */
	public void updatePermissionUser() {
		if (PermissionsConfig.instance.users.containsKey(this.name)) {
			this.permissionUser = PermissionsConfig.instance.users.get(this.name);
		} else {
			this.permissionUser = new PermissionUser();
			this.permissionUser.groupName = PermissionsConfig.instance.defaultGroup;
			PermissionsConfig.instance.users.put(this.name, this.permissionUser);
		}
		
		String playerGroupName = this.permissionUser.groupName;
		PermissionGroup playerGroup = PermissionsConfig.instance.getGroup(playerGroupName);
		
		if (playerGroup != null) {
			this.permissionUser.group = playerGroup;
			Logging.logInfo(this.name + " has the permission group \"" + playerGroupName + "\"");
		} else {
			PermissionGroup permissionGroup = PermissionsConfig.instance.createGroupWithDefaultPerms(playerGroupName);
			this.permissionUser.group = permissionGroup;
			PermissionsConfig.instance.groups.add(permissionGroup);
			
			Logging.logWarn("Created permission group \"" + 
					playerGroupName + "\" because it didn't exist and \"" + 
					this.name + "\" requested it");
		}
		
		boolean canBreakWater = 
				this.permissionUser.hasPermission("zerocraft.world.break.special.water", false);
		boolean canBreakLava = 
				this.permissionUser.hasPermission("zerocraft.world.break.special.water", false);
		boolean canPlaceWater = 
				this.permissionUser.hasPermission("zerocraft.world.place.special.water", false);
		boolean canPlaceLava = 
				this.permissionUser.hasPermission("zerocraft.world.place.special.water", false);
		
		if (this.classicProtocolExtensions && this.supportedCPEList.contains("BlockPermissions")) {
			this.sendPacket(new FilledPacket(Packets.cpe_blockperm, new Object[] {
					(byte)WorldTiles.water.id,
					(byte)(canPlaceWater ? 1 : 0),
					(byte)(canBreakWater ? 1 : 0)
			}));
			
			this.sendPacket(new FilledPacket(Packets.cpe_blockperm, new Object[] {
					(byte)WorldTiles.lava.id,
					(byte)(canPlaceLava ? 1 : 0),
					(byte)(canBreakLava ? 1 : 0)
			}));
			
			this.sendPacket(new FilledPacket(Packets.cpe_blockperm, new Object[] {
					(byte)WorldTiles.stillwater.id,
					(byte)(canPlaceWater ? 1 : 0),
					(byte)(canBreakWater ? 1 : 0)
			}));
			
			this.sendPacket(new FilledPacket(Packets.cpe_blockperm, new Object[] {
					(byte)WorldTiles.stilllava.id,
					(byte)(canPlaceLava ? 1 : 0),
					(byte)(canBreakLava ? 1 : 0)
			}));	
		}
		
		this.serverInstance.saveConfigs();
	}
	
	@EventHandler
	public void server_clientReceivedPacketID(EventBasicTuple e) {
		if (e.id != Server.EVENT_CLIENTRECEIVEDPACKETID_ID) return;
		if (e.tuple.item1 != this.client) return;
		
		try {
			byte[] packetData = new byte[0];
			
			try {
				if (!Packets.isValidPacketID((byte)e.tuple.item2)) throw new Exception();
				Packet packet = Packets.getPacketByType(PacketType.valueOfID((byte)e.tuple.item2));
				packetData = new byte[Packet.getPacketFieldsSize(packet)];
				
				if (this.client.readBuffer.remaining() < packetData.length) {
					this.client.readBuffer.compact();
					return;
				}
				
				this.client.readBuffer.get(packetData);	
			} catch (Exception ex) {
				this.kick("&cInvalid or malformed packet ID: " + (byte)e.tuple.item2);
				return;
			}
			
			FilledPacket packetFromClient = FilledPacket.unpackageFromByteArray((byte)e.tuple.item2, packetData);
			this.packetHandler.handlePacket(packetFromClient);
		} catch (Exception ex) {
			Logging.logError("Unable to process the packet received from " + 
					this.client.identifier + ": " +
					Utils.getExceptionStackTraceAsStr(ex));
		}
	}
	
	@EventHandler
	public void server_clientDisconnected(EventBasicObject e) {
		if (e.id != Server.EVENT_CLIENTDISCONNECTED_ID) return;
		if (e.object != this.client) return;
		this.serverInstance.leavePlayer(this);
		
		this.switchWorld(null);
		this.consoleHandler.close();

		this.serverInstance = null;
		this.client = null;
		this.id = -1;
		this.packetHandler = null;
		this.consoleHandler = null;
		this.permissionUser = null;
		this.name = null;
		this.protocolVersionNumber = -1;
		this.isLoggedIn = false;
	}
}
