package net.fieme.zerocraft;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;

import net.fieme.zerocraft.configuration.ServerConfig;
import net.fieme.zerocraft.logging.Logging;

/**
 * Manages the server listing of the specified ZeroCraft instance
 */
public class ServerListingManager {
	private boolean isStarted;
	private Thread heartbeatThread;
	private String server;
	private String url;
	private int port;
	private ZeroCraft serverInstance;
	
	/**
	 * Manages the server listing of the specified ZeroCraft instance
	 * 
	 * @param serverInstance the instance of ZeroCraft to manage
	 * @param server the server listing
	 * @param url the heartbeat URL
	 * @param port the server listing port
	 */
	public ServerListingManager(ZeroCraft serverInstance, 
			String server, String url, int port) {
		this.serverInstance = serverInstance;
		this.server = server;
		this.url = url;
		this.port = port;
	}
	
	/**
	 * Starts listing
	 */
	public void start() {
		this.heartbeatThread = new Thread() {
			@Override
			public void run() {
				heartBeatThread_func();
			}
		};
		this.isStarted = true;
		this.heartbeatThread.start();
	}
	
	/**
	 * Stops listing
	 */
	public void stop() {
		this.isStarted = false;
		if (this.heartbeatThread != null)
			this.heartbeatThread.interrupt();
	}
	
	private String getRequestArgs() throws UnsupportedEncodingException {
		HashMap<String, String> args = new HashMap<String, String>();
		String exportedArgs = "";
		
		args.put("port", "" + ServerConfig.listenPort);
		args.put("users", "" + this.serverInstance.players.size());
		args.put("max", "" + ServerConfig.maxPlayers);
		args.put("version", "6");
		args.put("salt", ServerConfig.verificationSalt);
		args.put("name", ServerConfig.serverName);
		args.put("public", "true");
		
		for (String key : args.keySet()) {
			String value = args.get(key);
			exportedArgs += "&" + key + "=" + URLEncoder.encode(value, StandardCharsets.UTF_8.toString());
		}
		exportedArgs = exportedArgs.substring(1);
		
		return exportedArgs;
	}
	
	private void heartBeatThread_func() {
		while (this.isStarted) {
			try {
				Socket connection = new Socket();
				InetSocketAddress endpoint = new InetSocketAddress(
						InetAddress.getByName(this.server), this.port);
				connection.connect(endpoint);
				
				OutputStream outputStream = connection.getOutputStream();
				InputStream inputStream = connection.getInputStream();
				outputStream.write(("GET " + this.url + "?" + this.getRequestArgs() + "\n\r").getBytes());
				outputStream.flush();
				
				ArrayList<Byte> receivedData = new ArrayList<Byte>();
				int readByte = 0;
				while ((readByte = inputStream.read()) != -1 && this.isStarted) {
					receivedData.add(Byte.valueOf((byte)readByte));
				}
				byte[] receivedDataUsable = new byte[receivedData.size()];
				int arrayIndex = 0;
				for (Byte dataByte : receivedData) {
					receivedDataUsable[arrayIndex] = dataByte.byteValue();
					arrayIndex++;
				}
				
				String receivedResponse = new String(receivedDataUsable);
				Logging.logInfo("Received following response from heartbeat: " + 
						receivedResponse);
				
				outputStream.close();
				inputStream.close();
				connection.close();
			} catch (Exception ex) {
				Logging.logError("Unable to send heartbeat to " + 
						this.server + ":" + this.port + "!");
			}
			
			try {
				Thread.sleep(45000);
			} catch (InterruptedException ex) {
				break;
			}
		}
	}
}
