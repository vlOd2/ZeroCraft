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
	public Socket socket;
	public SocketChannel socketChannel;
	public ByteBuffer readBuffer = ByteBuffer.allocate(BUFFER_SIZE);
	public ByteBuffer writeBuffer = ByteBuffer.allocate(BUFFER_SIZE);
	public ClientNetworkIdentifier identifier;

	public Client(SocketChannel socketChannel) {
		try {
			this.socketChannel = socketChannel;
			this.socketChannel.configureBlocking(false);
			this.socket = this.socketChannel.socket();
			this.isConnected = true;
			
			this.readBuffer.clear();
			this.writeBuffer.clear();
			
			this.socket.setTcpNoDelay(true);
			this.socket.setKeepAlive(false);
			this.socket.setReuseAddress(false);
			
			this.socket.setTrafficClass(24);
			this.socket.setSoTimeout(100);	
			
			this.identifier = Utils.getIdentifierFromClientSocket(this.socket);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void close() {
		try {
			this.socketChannel.close();
		} catch (Exception ex) {
		}
		
		try {
			this.readBuffer.clear();
			this.readBuffer.flip();
		} catch (Exception ex) {
		}
		
		try {
			this.writeBuffer.clear();
			this.writeBuffer.flip();	
		} catch (Exception ex) {
		}

		this.readBuffer = null;
		this.writeBuffer = null;
		this.socket = null;
		this.socketChannel = null;
		this.isConnected = false;
		
		System.gc();
	}
}
