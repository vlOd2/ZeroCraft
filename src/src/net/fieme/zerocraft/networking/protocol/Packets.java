package net.fieme.zerocraft.networking.protocol;

public class Packets
{
    public static Packet login = new Packet((byte)PacketType.LOGIN.id, new Class[]
    {
        byte.class,
        String.class,
        String.class,
        byte.class
    });

    public static Packet ping = new Packet((byte)PacketType.PING.id, new Class[0]);
    public static Packet levelInit = new Packet((byte)PacketType.LEVEL_INIT.id, new Class[0]);

    public static Packet levelData = new Packet((byte)PacketType.LEVEL_DATA.id, new Class[]
    {
        short.class,
        byte[].class,
        byte.class
    });

    public static Packet levelEnd = new Packet((byte)PacketType.LEVEL_END.id, new Class[]
    {
        short.class,
        short.class,
        short.class
    });

    public static Packet block = new Packet((byte)PacketType.BLOCK.id, new Class[]
    {
        short.class,
        short.class,
        short.class,
        byte.class,
        byte.class
    });

    public static Packet block2 = new Packet((byte)PacketType.BLOCK_2.id, new Class[]
    {
        short.class,
        short.class,
        short.class,
        byte.class
    });

    public static Packet playerConnect = new Packet((byte)PacketType.PLAYER_CONNECT.id, new Class[]
    {
        byte.class,
        String.class,
        short.class,
        short.class,
        short.class,
        byte.class,
        byte.class
    });

    public static Packet playerTP = new Packet((byte)PacketType.PLAYER_TP.id, new Class[]
    {
        byte.class,
        short.class,
        short.class,
        short.class,
        byte.class,
        byte.class
    });

    public static Packet playerPosOrt = new Packet((byte)PacketType.PLAYER_POS_ORT.id, new Class[]
    {
        byte.class,
        byte.class,
        byte.class,
        byte.class,
        byte.class,
        byte.class
    });

    public static Packet playerPos = new Packet((byte)PacketType.PLAYER_POS.id, new Class[]
    {
        byte.class,
        byte.class,
        byte.class,
        byte.class
    });

    public static Packet playerOrt = new Packet((byte)PacketType.PLAYER_ORT.id, new Class[]
    {
        byte.class,
        byte.class,
        byte.class
    });

    public static Packet playerDisconnect = new Packet((byte)PacketType.PLAYER_DISCONNECT.id, new Class[]
    {
        byte.class
    });

    public static Packet chat = new Packet((byte)PacketType.CHAT.id, new Class[]
    {
        byte.class,
        String.class
    });

    public static Packet kick = new Packet((byte)PacketType.KICK.id, new Class[]
    {
        String.class
    });

    public static Packet update = new Packet((byte)PacketType.UPDATE.id, new Class[]
    {
        byte.class
    });

    public static Packet cpe_extinfo = new Packet((byte)PacketType.CPE_EXTINFO.id, new Class[] 
    {
    	String.class,
    	short.class
    });
    
    public static Packet cpe_extentry = new Packet((byte)PacketType.CPE_EXTENTRY.id, new Class[] 
    {
    	String.class,
    	int.class
    });
    
    public static Packet cpe_blockperm = new Packet((byte)PacketType.CPE_BLOCKPERM.id, new Class[] 
    {
    	byte.class,
    	byte.class,
    	byte.class
    });
    
    /**
     * Gets a packet from it's type
     * 
     * @param type the type
     * @return the packet or null (if the type is invalid)
     */
    public static Packet getPacketByType(PacketType type)
    {
        switch (type)
        {
            case LOGIN:
                return login;
            case PING:
                return ping;
            case LEVEL_INIT:
                return levelInit;
            case LEVEL_DATA:
                return levelData;
            case LEVEL_END:
                return levelEnd;
            case BLOCK:
                return block;
            case BLOCK_2:
                return block2;
            case PLAYER_CONNECT:
                return playerConnect;
            case PLAYER_TP:
                return playerTP;
            case PLAYER_POS_ORT:
                return playerPosOrt;
            case PLAYER_POS:
                return playerPos;
            case PLAYER_ORT:
                return playerOrt;
            case PLAYER_DISCONNECT:
                return playerDisconnect;
            case CHAT:
                return chat;
            case KICK:
                return kick;
            case UPDATE:
                return update;
            case CPE_EXTINFO:
            	return cpe_extinfo;
            case CPE_EXTENTRY:
            	return cpe_extentry;
            case CPE_BLOCKPERM:
            	return cpe_blockperm;
            default:
                return null;
        }
    }
    
    /**
     * Checks if the specified packet ID is valid
     * 
     * @param id the packet ID
     * @return true if the packet is valid, false if otherwise
     */
    public static boolean isValidPacketID(byte id) {
    	PacketType packetType = PacketType.valueOfID(id);
    	Packet packet = Packets.getPacketByType(packetType);
    	return packet != null;
    }
}