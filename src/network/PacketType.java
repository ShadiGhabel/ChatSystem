package network;

public enum PacketType {
    REGISTER,
    LOGIN,
    CHAT,
    SYSTEM,
    ERROR,

    CREATE_ROOM,
    JOIN_ROOM,
    LEAVE_ROOM,
    LIST_ROOMS,
    LIST_USERS
}
