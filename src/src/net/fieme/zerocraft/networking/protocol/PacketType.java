package net.fieme.zerocraft.networking.protocol;

public enum PacketType
{
    LOGIN(0),
    PING(1),
    LEVEL_INIT(2),
    LEVEL_DATA(3),
    LEVEL_END(4),
    BLOCK(5),
    BLOCK_2(6),
    PLAYER_CONNECT(7),
    PLAYER_TP(8),
    PLAYER_POS_ORT(9),
    PLAYER_POS(10),
    PLAYER_ORT(11),
    PLAYER_DISCONNECT(12),
    CHAT(13),
    KICK(14),
    UPDATE(15),
    CPE_EXTINFO(16),
    CPE_EXTENTRY(17),
    CPE_BLOCKPERM(28);
	
    public final int id;
    
    private PacketType(int id) {
        this.id = id;
    }
    
    public static PacketType valueOfID(int id) {
        for (PacketType packetType : PacketType.values()) {
            if (packetType.id == id) {
                return packetType;
            }
        }
        
        return null;
    }
}