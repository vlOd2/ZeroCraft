package net.fieme.zerocraft.networking.protocol;

/**
 * NOTE: Not intended to be used class
 */
@SuppressWarnings("rawtypes")
public class Packet {
    public final byte packetID;
	public final Class[] packetFields;

	/**
	 * NOTE: Not intended to be used class
	 * 
	 * @param arg0 unknown
	 * @param arg1 unknown
	 */
    public Packet(byte arg0, Class[] arg1) {
        this.packetID = arg0;
        this.packetFields = arg1;
    }

    /**
     * NOTE: Not intended to be used function
     * 
     * @param arg0 unknown
     * @return unknown
     */
    public static boolean isValidPacketID(byte arg0) {
    	return !(arg0 < 0x00 || arg0 > 0x0f);
    }
    
    /**
     * NOTE: Not intended to be used function
     * 
     * @param arg0 unknown
     * @return unknown
     */
    public static int getPacketFieldsSize(Packet arg0) {
    	int finalSize = 0;
    	
		for (int packetFieldIndex = 0; 
			packetFieldIndex < arg0.packetFields.length; 
			packetFieldIndex++) {
			Class packetField = arg0.packetFields[packetFieldIndex];

            if (packetField == byte.class) {
                finalSize++;
            } else if (packetField == short.class) {
            	finalSize += 2;
            } else if (packetField == String.class) {
                finalSize += 64;
            } else if (packetField == byte[].class) {
                finalSize += 1024;
            }
		}
    	
    	return finalSize;
    }
}
