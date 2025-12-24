package network;
import java.io.Serializable;

public class Packet<T> implements Serializable {
    private PacketType type;
    private T payload;

    public Packet(PacketType type, T payload) {
        this.type = type;
        this.payload = payload;
    }

    public PacketType getType() {
        return type;
    }
    public T getPayload() {
        return payload;
    }
}
