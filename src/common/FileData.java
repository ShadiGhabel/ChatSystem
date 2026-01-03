package common;

import java.io.Serializable;

public class FileData implements Serializable {
    private FileMetadata metadata;
    private byte[] content;

    public FileData(FileMetadata metadata, byte[] content) {
        this.metadata = metadata;
        this.content = content;
    }

    public FileMetadata getMetadata() {
        return metadata;
    }

    public void setMetadata(FileMetadata metadata) {
        this.metadata = metadata;
    }

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }
}
