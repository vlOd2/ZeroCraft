package net.fieme.zerocraft.networking.protocol;

public class Packets
{
    public static Packet login = new Packet((byte)PacketType.LOGIN.ordinal(), new Class[]
    {
        byte.class,
        String.class,
        String.class,
        byte.class
    });

    public static Packet ping = new Packet((byte)PacketType.PING.ordinal(), new Class[0]);
    public static Packet levelInit = new Packet((byte)PacketType.LEVEL_INIT.ordinal(), new Class[0]);

    public static Packet levelData = new Packet((byte)PacketType.LEVEL_DATA.ordinal(), new Class[]
    {
        short.class,
        byte[].class,
        byte.class
    });

    public static Packet levelEnd = new Packet((byte)PacketType.LEVEL_END.ordinal(), new Class[]
    {
        short.class,
        short.class,
        short.class
    });

    public static Packet block = new Packet((byte)PacketType.BLOCK.ordinal(), new Class[]
    {
        short.class,
        short.class,
        short.class,
        byte.class,
        byte.class
    });

    public static Packet block2 = new Packet((byte)PacketType.BLOCK_2.ordinal(), new Class[]
    {
        short.class,
        short.class,
        short.class,
        byte.class
    });

    public static Packet playerConnect = new Packet((byte)PacketType.PLAYER_CONNECT.ordinal(), new Class[]
    {
        byte.class,
        String.class,
        short.class,
        short.class,
        short.class,
        byte.class,
        byte.class
    });

    public static Packet playerTP = new Packet((byte)PacketType.PLAYER_TP.ordinal(), new Class[]
    {
        byte.class,
        short.class,
        short.class,
        short.class,
        byte.class,
        byte.class
    });

    public static Packet playerPosOrt = new Packet((byte)PacketType.PLAYER_POS_ORT.ordinal(), new Class[]
    {
        byte.class,
        byte.class,
        byte.class,
        byte.class,
        byte.class,
        byte.class
    });

    public static Packet playerPos = new Packet((byte)PacketType.PLAYER_POS.ordinal(), new Class[]
    {
        byte.class,
        byte.class,
        byte.class,
        byte.class
    });

    public static Packet playerOrt = new Packet((byte)PacketType.PLAYER_ORT.ordinal(), new Class[]
    {
        byte.class,
        byte.class,
        byte.class
    });

    public static Packet playerDisconnect = new Packet((byte)PacketType.PLAYER_DISCONNECT.ordinal(), new Class[]
    {
        byte.class
    });

    public static Packet chat = new Packet((byte)PacketType.CHAT.ordinal(), new Class[]
    {
        byte.class,
        String.class
    });

    public static Packet kick = new Packet((byte)PacketType.KICK.ordinal(), new Class[]
    {
        String.class
    });

    public static Packet update = new Packet((byte)PacketType.UPDATE.ordinal(), new Class[]
    {
        byte.class
    });

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
            default:
                return null;
        }
    }
}