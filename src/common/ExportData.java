package common;

import java.io.Serializable;
import java.util.List;

public class ExportData implements Serializable {
    private String roomName;
    private List<Message> messages;
    private long exportedAt;

    public ExportData(String roomName, List<Message> messages) {
        this.roomName = roomName;
        this.messages = messages;
        this.exportedAt = System.currentTimeMillis();
    }

    public String getRoomName() {
        return roomName;
    }

    public List<Message> getMessages() {
        return messages;
    }

    public long getExportedAt() {
        return exportedAt;
    }
}
