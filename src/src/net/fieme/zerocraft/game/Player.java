package net.fieme.zerocraft.game;

import java.nio.BufferOverflowException;

import net.fieme.zerocraft.DiscordIntegration;
import net.fieme.zerocraft.UndocumentedClass000;
import net.fieme.zerocraft.Utils;
import net.fieme.zerocraft.ZeroCraft;
import net.fieme.zerocraft.configuration.MessagesConfig;
import net.fieme.zerocraft.configuration.ServerConfig;
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
import net.fieme.zerocraft.networking.protocol.PlayerPacketManager;
import net.fieme.zerocraft.networking.protocol.PacketType;
import net.fieme.zerocraft.networking.protocol.Packets;
import net.fieme.zerocraft.permission.PermissionUser;

/**
 * A connected player
 */
public class Player implements EventListener {
	private ZeroCraft serverInstance;
	public byte id;
	public String name;
	public boolean isLoggedIn;
	public Client client;
	public PlayerPacketManager packetHandler;
	public int protocolVersionNumber;
	public AliveEntity entity;
	public PlayerConsoleHandler consoleHandler;
	public PermissionUser permissionUser;
	public boolean classicProtocolExtensions;
	
	/**
	 * A connected player
	 */
	public Player(ZeroCraft serverInstance, Client client, byte id) {
		this.serverInstance = serverInstance;
		this.client = client;
		this.id = id;
		this.packetHandler = new PlayerPacketManager(this.serverInstance, this);
		this.entity = new AliveEntity();
		this.consoleHandler = new PlayerConsoleHandler(this);
		if (ServerConfig.usePermissionSystem) this.permissionUser = new PermissionUser();
		
		this.serverInstance.server.clientReceivedPacketID.addListener(this);
		this.serverInstance.server.clientDisconnected.addListener(this);
		Logging.logInfo(this.client.clientIdentifier + " has connected");
	}
	
	/**
	 * Sends a filled packet to this player
	 * 
	 * @param packet the packet
	 */
	public void sendPacket(FilledPacket packet) {
		try {
			client.clientWriteBuffer.put(packet.packageToByteArray());
		} catch (BufferOverflowException ex) {
			try {
				if (client.clientWriteBuffer.position() > 0) {
					client.clientWriteBuffer.flip();
					client.clientSocketChannel.write(client.clientWriteBuffer);
					client.clientWriteBuffer.compact();
				}
				client.clientWriteBuffer.put(packet.packageToByteArray());
			} catch (Exception ex2) {
				Logging.logSevere("Unable to send packet to " + client.clientIdentifier + 
						" even after cleaning the write buffer!"
						+ " There must be a lot of packets that are sent"
						+ " or something has gone wrong!");
			}
		}
	}

	/**
	 * Switches the world of this player
	 * 
	 * @param world the world
	 */
	public void switchWorld(World world) {
		if (this.entity.world != null) {
			this.serverInstance.foreachPlayer(new UndocumentedClass000<Player>() {
				@Override
				public void func_0000(Player entry) {
					if (entry.id != id &&
						entry.entity.world == entity.world) {
						entry.packetHandler.sendPlayerLeave(id);
					}
				}
			});
			this.entity.world.removeEntity(this.entity);
		}
			
		if (world != null) {
			world.addEntity(this.entity);
			this.packetHandler.sendWorldData();
			
			this.serverInstance.sendGlobalChatMessage(
					MessagesConfig.playerSwitchedWorld
					.replace("%player%", this.name)
					.replace("%world%", world.name));

			this.packetHandler.teleport(world.spawnX,
					world.spawnY, 
					world.spawnZ,
					this.entity.yaw,
					this.entity.pitch);
			
			this.packetHandler.sendPlayerJoin((byte) -1, this.name, 
					this.entity.posX,
					this.entity.posY,
					this.entity.posZ, 
					this.entity.yaw,
					this.entity.pitch);
			
			this.serverInstance.foreachPlayer(new UndocumentedClass000<Player>() {
				@Override
				public void func_0000(Player entry) {
					if (entry.id != id &&
						entry.entity.world == entity.world) {
						entry.packetHandler.sendPlayerJoin(id, name, 
								entity.posX, 
								entity.posY, 
								entity.posZ, 
								entity.yaw, 
								entity.pitch);
						packetHandler.sendPlayerJoin(entry.id, entry.name, 
								entry.entity.posX, 
								entry.entity.posY, 
								entry.entity.posZ,
								entry.entity.yaw,
								entry.entity.pitch);
					}
				}
			});
		}
	}
	
	@EventHandler
	public void server_clientReceivedPacketID(EventBasicTuple e) {
		if (e.id != Server.EVENT_CLIENTRECEIVEDPACKETID_ID) return;
		if (e.tuple.item1 != this.client) return;
		
		try {
			byte[] packetData = new byte[0];
			
			try {
				Packet packet = Packets.getPacketByType(PacketType.values()[(byte)e.tuple.item2]);
				packetData = new byte[Packet.getPacketFieldsSize(packet)];
				
				if (this.client.clientReadBuffer.remaining() < packetData.length) {
					this.client.clientReadBuffer.compact();
					return;
				}
				
				this.client.clientReadBuffer.get(packetData);	
			} catch (Exception ex) {
				Logging.logWarn("PROTOCOL VIOLATION: " + this.client.clientIdentifier + 
						" has sent an invalid packet ID or invalid packet data!"
						+ " (Received ID: " + (byte)e.tuple.item2 + ")");
				return;
			}
			
			FilledPacket packetFromClient = FilledPacket.unpackageFromByteArray((byte)e.tuple.item2, packetData);
			this.packetHandler.handlePacket(packetFromClient);
		} catch (Exception ex) {
			Logging.logError("Unable to process the packet received from " + 
					this.client.clientIdentifier + ": " +
					Utils.getExceptionStackTraceAsStr(ex));
		}
	}
	
	@EventHandler
	public void server_clientDisconnected(EventBasicObject e) {
		if (e.id != Server.EVENT_CLIENTDISCONNECTED_ID) return;
		if (e.object != this.client) return;
		Logging.logInfo(this.client.clientIdentifier + " has disconnected");

		this.serverInstance.server.clientReceivedPacketID.removeListener(this);
		this.serverInstance.server.clientDisconnected.removeListener(this);	

		if (this.isLoggedIn) {
			try {
				serverInstance.foreachPlayer(new UndocumentedClass000<Player>() {
					@Override
					public void func_0000(Player entry) {
						if (entry.id != id &&
							entry.entity.world == entity.world) {
								entry.packetHandler.sendPlayerLeave(id);
						}
					}
				});
			} catch (Exception ex) {
			}
			
			this.serverInstance.sendGlobalChatMessage(
					MessagesConfig.playerLeft.replace("%player%", this.name));
		}

		this.switchWorld(null);
		this.consoleHandler.close();
		if (this.permissionUser != null) this.permissionUser.changeGroup(null);
		this.serverInstance.players.remove(this);
		DiscordIntegration.update(this.serverInstance.players.size(), 
				Logging.totalWarns, Logging.totalErrors);
		
		this.serverInstance = null;
		this.client = null;
		this.id = -1;
		this.packetHandler = null;
		this.entity = null;
		this.consoleHandler = null;
		this.permissionUser = null;
		this.name = null;
		this.protocolVersionNumber = -1;
		this.isLoggedIn = false;
	}
}
