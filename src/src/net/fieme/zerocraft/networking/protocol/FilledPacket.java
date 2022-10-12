package net.fieme.zerocraft.networking.protocol;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * NOTE: Not intended to be used class
 */
@SuppressWarnings("rawtypes")
public class FilledPacket {
	public final Packet packet;
	public final Object[] packetFieldsValues;
	
	/**
	 * NOTE: Not intended to be used class
	 * 
	 * @param arg0 unknown
	 * @param arg1 unknown
	 * @param arg2 unknown
	 */
	public FilledPacket(Packet arg0, Object[] arg1) {
		this.packet = arg0;
		this.packetFieldsValues = arg1;
		
		if (arg1.length < this.packet.packetFields.length)
			throw new IllegalArgumentException();
	}
	
    /**
     * NOTE: Not intended to be used function
     * 
     * @return unknown
     */
	public byte[] packageToByteArray() {
		byte[] finalData = new byte[] { this.packet.packetID };
		
		for (int packetFieldIndex = 0; 
			packetFieldIndex < this.packet.packetFields.length; 
			packetFieldIndex++) {
			Class packetField = this.packet.packetFields[packetFieldIndex];
			Object packetFieldValue = this.packetFieldsValues[packetFieldIndex]; 
			
			byte[] data = new byte[0];
            if (packetField == byte.class) {
                data = new byte[] { (byte)packetFieldValue };
            } else if (packetField == short.class) {
            	data = ByteBuffer.allocate(2).order(ByteOrder.BIG_ENDIAN).putShort((short)packetFieldValue).array();
            } else if (packetField == String.class) {
                String str = (String)packetFieldValue;

                if (str.length() < 64)
                    str = StringUtils.rightPad(str, 64, ' ');
                else
                    str = str.substring(0, 64);
                
                data = str.getBytes(StandardCharsets.UTF_8);
            } else if (packetField == byte[].class) {
                byte[] arr = (byte[])packetFieldValue;

                if (arr.length < 1024) {
                	byte[] paddingData = new byte[1024 - arr.length];
                	Arrays.fill(paddingData, (byte) 0x00);
                	arr = ArrayUtils.addAll(arr, paddingData);
                }
                else
                    arr = ArrayUtils.subarray(arr, 0, 1024);

                data = arr;
            }

            finalData = ArrayUtils.addAll(finalData, data);
		}
		
		return finalData;
	}
	
	/**
	 * NOTE: Not intended to be used function
	 * 
	 * @param arg0 unknown
	 * @return unknown
	 */
	public static FilledPacket unpackageFromByteArray(byte arg0, byte[] arg1) {
		try {
			if (!Packet.isValidPacketID(arg0)) throw new IllegalArgumentException();

			Packet packet = Packets.getPacketByType(PacketType.values()[arg0]);
			ArrayList<Object> packetData = new ArrayList<Object>();

	        int currentDataIndex = 0;
	        if (packet.packetFields.length >= 1) {
	            for (int packetFieldIndex = 0;
	                packetFieldIndex < packet.packetFields.length;
	                packetFieldIndex++) {
	                Class packetField = packet.packetFields[packetFieldIndex];

	                if (packetField == byte.class) {
	                    packetData.add(arg1[currentDataIndex]);
	                    currentDataIndex++;
	                } else if (packetField == short.class) {
	                	byte[] rawShortVal = ArrayUtils.subarray(arg1,
	                			currentDataIndex, currentDataIndex + 2);
	                	packetData.add(ByteBuffer.wrap(rawShortVal).order(ByteOrder.BIG_ENDIAN).getShort());
	                    currentDataIndex += 2;
	                } else if (packetField == String.class) {
	                	byte[] rawStringVal = ArrayUtils.subarray(arg1,
	                			currentDataIndex, currentDataIndex + 64);
	                	packetData.add(new String(rawStringVal, StandardCharsets.UTF_8).trim());
	                    currentDataIndex += 64;
	                } else if (packetField == byte[].class) {
	                	packetData.add(ArrayUtils.subarray(arg1,
	                			currentDataIndex, currentDataIndex + 1024));
	                    currentDataIndex += 1024;
	                }
	            }
	        }
			
			return new FilledPacket(packet, packetData.toArray());	
		} catch (Exception ex) {
			return null;
		}
	}
}
