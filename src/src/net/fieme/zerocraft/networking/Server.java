package net.fieme.zerocraft.networking;

import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;

import net.fieme.zerocraft.DiscordIntegration;
import net.fieme.zerocraft.Tuple;
import net.fieme.zerocraft.Utils;
import net.fieme.zerocraft.ZeroCraft;
import net.fieme.zerocraft.configuration.MessagesConfig;
import net.fieme.zerocraft.event.EventBasicObject;
import net.fieme.zerocraft.event.EventBasicTuple;
import net.fieme.zerocraft.event.EventFirer;
import net.fieme.zerocraft.game.Player;
import net.fieme.zerocraft.logging.Logging;
import net.fieme.zerocraft.networking.protocol.FilledPacket;
import net.fieme.zerocraft.networking.protocol.Packet;
import net.fieme.zerocraft.networking.protocol.Packets;

public class Server {
	public static final long EVENT_CLIENTRECEIVEDPACKETID_ID = 904796065;
	public static final long EVENT_CLIENTDISCONNECTED_ID = 872580607;
	private ZeroCraft serverInstance;
	private boolean isRunning;
	private String listenIP;
	private int listenPort;
	private ServerSocket serverSocket;
	private ServerSocketChannel serverSocketChannel;
	private Thread serverThread;
	private Thread serverCheckThread;
	public final ArrayList<Client> clients = new ArrayList<Client>();
	public EventFirer<EventBasicTuple> clientReceivedPacketID;
	public EventFirer<EventBasicObject> clientDisconnected;
	
	public Server(ZeroCraft serverInstance, String listenIP, int listenPort) {
		this.serverInstance = serverInstance;
		this.listenIP = listenIP;
		this.listenPort = listenPort;
	}

	public void start() {
		try {
			this.serverSocketChannel = ServerSocketChannel.open();
			this.serverSocketChannel.configureBlocking(false);
			this.serverSocket = this.serverSocketChannel.socket();
			this.serverSocket.bind(new InetSocketAddress(this.listenIP, this.listenPort));
			this.serverThread = new Thread() {
				@Override
				public void run() {
					serverThread_func();
				}
			};
			this.serverCheckThread = new Thread() {
				@Override
				public void run() {
					serverCheckThread_func();
				}
			};
			this.clientReceivedPacketID = new EventFirer<EventBasicTuple>();
			this.clientDisconnected = new EventFirer<EventBasicObject>();
			
			this.isRunning = true;
			this.serverThread.start();
			this.serverCheckThread.start();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public void stop() {
		this.isRunning = false;
		
		if (this.serverCheckThread != null)
			this.serverCheckThread.interrupt();
		if (this.serverThread != null)
			this.serverThread.interrupt();
		if (this.serverSocketChannel != null) 
			try { this.serverSocketChannel.close(); } catch (Exception ex) { }
		this.serverSocket = null;
		this.serverSocketChannel = null;
		this.serverThread = null;
		this.serverCheckThread = null;
		this.clientReceivedPacketID = null;
		this.clientDisconnected = null;
		
		this.clients.clear();
	}

	private void serverThread_func() {
		while (this.isRunning) {
			try {
				SocketChannel acceptedClient;
				while ((acceptedClient = this.serverSocketChannel.accept()) != null) {
					Client client = new Client(acceptedClient);
					
					byte playerID = this.serverInstance.getFirstAvailablePlayerID();
					Player player = new Player(this.serverInstance, client, playerID);
					
					if (playerID == -1) {
						player.packetHandler.kick(MessagesConfig.serverKickFull);
						break;
					}

					this.serverInstance.players.add(player);
					this.clients.add(client);
					DiscordIntegration.update(this.serverInstance.players.size(), 
							Logging.totalWarns, Logging.totalErrors);
				}

				for (Client client : this.clients.toArray(new Client[0])) {
					if (Thread.interrupted()) break;
					
					try {
						client.clientSocketChannel.read(client.clientReadBuffer);
						
						while (client.clientReadBuffer.position() > 0) {
							client.clientReadBuffer.flip();
							byte packetID = client.clientReadBuffer.get();
							
							if (!Packet.isValidPacketID(packetID))
								throw new Exception();
							
							if (this.clientReceivedPacketID == null) break;
							this.clientReceivedPacketID.fire(
									new EventBasicTuple(EVENT_CLIENTRECEIVEDPACKETID_ID, 
											new Tuple<Object, Object>(client, packetID)));
							client.clientReadBuffer.compact();
						}

						if (client.clientWriteBuffer.position() > 0) {
							client.clientWriteBuffer.flip();
							client.clientSocketChannel.write(client.clientWriteBuffer);
							client.clientWriteBuffer.compact();
						}
					} catch (Exception ex) {
						client.isConnected = false;
					}
					
					if (!client.isConnected) {
						if (this.clientDisconnected == null) break;
						
						this.clientDisconnected.fire(
								new EventBasicObject(EVENT_CLIENTDISCONNECTED_ID, client));
						client.close();
						this.clients.remove(client);
					}	
				}
				
				Thread.sleep(1);
			} catch (Exception ex) {
				if (ex instanceof InterruptedException) break;
				Logging.logSevere("Unable to operate the connection server: " + 
						Utils.getExceptionStackTraceAsStr(ex));
			}
		}
	}

	private void serverCheckThread_func() {
		while (this.isRunning) {
			try {
				for (Client client : this.clients.toArray(new Client[0])) {
					client.clientWriteBuffer.put(
							new FilledPacket(Packets.ping, new Object[0]).packageToByteArray());
				}
				Thread.sleep(1000);
			} catch (Exception ex) {
				if (ex instanceof InterruptedException) break;
				Logging.logError("Unable to send ping packets to all clients!");
			}
		}
	}
}
