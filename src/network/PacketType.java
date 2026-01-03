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
    LIST_USERS,

    EXPORT_REQ,
    EXPORT_RES,

    FILE_UPLOAD_REQ,
    FILE_UPLOAD_RES,
    FILE_DOWNLOAD_REQ,
    FILE_DOWNLOAD_RES,
    FILE_DATA
}
