package net.fieme.zerocraft.networking;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;

import net.fieme.zerocraft.Tuple;
import net.fieme.zerocraft.Utils;
import net.fieme.zerocraft.ZeroCraft;
import net.fieme.zerocraft.configuration.MessagesConfig;
import net.fieme.zerocraft.configuration.ServerConfig;
import net.fieme.zerocraft.event.EventBasicObject;
import net.fieme.zerocraft.event.EventBasicTuple;
import net.fieme.zerocraft.event.EventFirer;
import net.fieme.zerocraft.game.EntityPlayer;
import net.fieme.zerocraft.logging.Logging;
import net.fieme.zerocraft.networking.protocol.FilledPacket;
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

	public void start() throws IOException {
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
					EntityPlayer player = new EntityPlayer(this.serverInstance, client, playerID);
					
					int clientsWithSameIP = 0;
					for (Client c : this.clients.toArray(new Client[0])) {
						if (c.identifier.networkAddress.equals(client.identifier.networkAddress)) {
							clientsWithSameIP++;
						}
					}
					
					if (clientsWithSameIP >= ServerConfig.instance.maxPlayersPerIP) {
						player.kick(MessagesConfig.instance.serverKickTooManyConnections);
						continue;
					}
					
					if (playerID == -1) {
						player.kick(MessagesConfig.instance.serverKickFull);
						continue;
					}
					
					this.clients.add(client);
				}

				for (Client client : this.clients.toArray(new Client[0])) {
					if (Thread.interrupted()) break;
					
					try {
						client.socketChannel.read(client.readBuffer);
						int packetsHandled = 0;
						
						while (client.readBuffer.position() > 0 && 
							!(packetsHandled >= ServerConfig.instance.maxPlayerPackets)) {
							client.readBuffer.flip();
							byte packetID = client.readBuffer.get();

							if (this.clientReceivedPacketID == null) break;
							this.clientReceivedPacketID.fire(
									new EventBasicTuple(EVENT_CLIENTRECEIVEDPACKETID_ID, 
											new Tuple<Object, Object>(client, packetID)));
							client.readBuffer.compact();
							
							packetsHandled++;
						}

						if (client.writeBuffer.position() > 0) {
							client.writeBuffer.flip();
							client.socketChannel.write(client.writeBuffer);
							client.writeBuffer.compact();
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
					if (client.isConnected) {
						client.writeBuffer.put(
								new FilledPacket(Packets.ping, new Object[0]).packageToByteArray());	
					} else {
						this.clients.remove(client);
						client.close();
					}
				}
				Thread.sleep(1000);
			} catch (Exception ex) {
				if (ex instanceof InterruptedException) break;
				Logging.logError("Unable to send ping packets to all clients: " +
						Utils.getExceptionStackTraceAsStr(ex));
			}
		}
	}
}
