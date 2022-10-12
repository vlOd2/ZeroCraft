package net.fieme.zerocraft.networking.protocol;

public enum PacketType
{
    LOGIN,
    PING,
    LEVEL_INIT,
    LEVEL_DATA,
    LEVEL_END,
    BLOCK,
    BLOCK_2,
    PLAYER_CONNECT,
    PLAYER_TP,
    PLAYER_POS_ORT,
    PLAYER_POS,
    PLAYER_ORT,
    PLAYER_DISCONNECT,
    CHAT,
    KICK,
    UPDATE
}