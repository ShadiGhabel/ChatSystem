package server;

import java.io.Serializable;

public class Message implements Serializable {
    public enum MessageType {
        TEXT,
        SYSTEM,
        FILE
    }
    private String sender;
    private String content;
    private long timestamp;
    private MessageType type;
    public Message(String sender, MessageType type, String content) {
        this.sender = sender;
        this.timestamp = System.currentTimeMillis();
        this.content = content;
        this.type = type;
    }
    public String getSender() {
        return sender;
    }
    public void setSender(String sender) {
        this.sender = sender;
    }

    public long getTimestamp() {
        return timestamp;
    }
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
    public MessageType getType() {
        return type;
    }

    public void setType(MessageType type) {
        this.type = type;
    }
}
