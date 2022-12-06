package net.fieme.zerocraft.networking.protocol;

/**
 * A network packet
 */
@SuppressWarnings("rawtypes")
public class Packet {
    public final byte packetID;
	public final Class[] packetFields;

	/**
	 * A network packet
	 * 
	 * @param id the packet id
	 * @param fields the packet fields
	 */
    public Packet(byte id, Class[] fields) {
        this.packetID = id;
        this.packetFields = fields;
    }

    /**
     * Gets the size of the packet's fields
     * 
     * @param packet the packet
     * @return the size or 0 if no fields
     */
    public static int getPacketFieldsSize(Packet packet) {
    	int finalSize = 0;
    	
		for (int packetFieldIndex = 0; 
			packetFieldIndex < packet.packetFields.length; 
			packetFieldIndex++) {
			Class packetField = packet.packetFields[packetFieldIndex];

            if (packetField == byte.class) {
                finalSize++;
            } else if (packetField == short.class) {
            	finalSize += 2;
            } else if (packetField == int.class) {
            	finalSize += 4;
            } else if (packetField == String.class) {
                finalSize += 64;
            } else if (packetField == byte[].class) {
                finalSize += 1024;
            }
		}
    	
    	return finalSize;
    }
}
