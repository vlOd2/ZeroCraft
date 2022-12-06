package net.fieme.zerocraft.networking;

/**
 * A class that represents the network identifier for a client
 */
public class ClientNetworkIdentifier {
	public final String networkAddress;
	public final int networkPort;
	
	private ClientNetworkIdentifier(String networkAddress, int networkPort) {
		this.networkAddress = networkAddress;
		this.networkPort = networkPort;
	}
	
	/**
	 * Gets an identifier from an network address and a port
	 * 
	 * @param networkAddress the network address
	 * @param networkPort the network port
	 * @return the network identifier
	 */
	public static ClientNetworkIdentifier getIdentifier(String networkAddress, int networkPort) {
		return new ClientNetworkIdentifier(networkAddress, networkPort);
	}
	
	/**
	 * Gets an identifier from the string representation of one
	 * 
	 * @param str the string representation
	 * @return the network identifier (or null in case of an error)
	 */
	public static ClientNetworkIdentifier getFromIdentifierString(String str) {
		try {
			String[] strSplitted = str.split(":");
			String networkAddress = strSplitted[0];
			int networkPort = Integer.valueOf(strSplitted[1]);
			return new ClientNetworkIdentifier(networkAddress, networkPort);	
		} catch (Exception ex) {
			return null;
		}
	}
	
	/**
	 * Gets the string representation of this identifier
	 */
	@Override
	public String toString() {
		return this.networkAddress + ":" + this.networkPort;
	}
}
