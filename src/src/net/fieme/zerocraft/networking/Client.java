package net.fieme.zerocraft.networking;

import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import net.fieme.zerocraft.Utils;

/**
 * A wrapper for a socket (CLIENT ONLY)
 */
public class Client {
	public static final int ONE_MEGABYTE = 1048576;
	public static final int BUFFER_SIZE = ONE_MEGABYTE;
	public boolean isConnected;
	public Socket clientSocket;
	public SocketChannel clientSocketChannel;
	public ByteBuffer clientReadBuffer = ByteBuffer.allocate(BUFFER_SIZE);
	public ByteBuffer clientWriteBuffer = ByteBuffer.allocate(BUFFER_SIZE);
	public ClientNetworkIdentifier clientIdentifier;

	public Client(SocketChannel clientSocketChannel) {
		try {
			this.clientSocketChannel = clientSocketChannel;
			this.clientSocketChannel.configureBlocking(false);
			this.clientSocket = this.clientSocketChannel.socket();
			this.isConnected = true;
			
			this.clientReadBuffer.clear();
			this.clientWriteBuffer.clear();
			
			this.clientSocket.setTcpNoDelay(true);
			this.clientSocket.setKeepAlive(false);
			this.clientSocket.setReuseAddress(false);
			
			this.clientSocket.setTrafficClass(24);
			this.clientSocket.setSoTimeout(100);	
			
			this.clientIdentifier = Utils.getIdentifierFromClientSocket(this.clientSocket);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public void close() {
		try {
			this.clientSocketChannel.close();
		} catch (Exception ex) {
		}
		
		try {
			this.clientSocket = null;
			this.clientSocketChannel = null;
			this.isConnected = false;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
