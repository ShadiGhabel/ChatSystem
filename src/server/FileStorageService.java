package server;

import common.FileMetadata;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class FileStorageService {
    private static FileStorageService instance;
    private Map<String, FileMetadata> fileRegistry;
    private static final String STORAGE_DIR = "server_storage";
    private static final long MAX_FILE_SIZE = 200 * 1024; // 200KB

    private FileStorageService() {
        fileRegistry = new ConcurrentHashMap<>();
        createStorageDirectory();
    }

    public static synchronized FileStorageService getInstance() {
        if (instance == null) {
            instance = new FileStorageService();
        }
        return instance;
    }

    private void createStorageDirectory() {
        File dir = new File(STORAGE_DIR);
        if (!dir.exists()) {
            boolean created = dir.mkdirs();
            if (created) {
                System.out.println("Created storage directory: " + STORAGE_DIR);
            }
        }
    }

    public synchronized String saveFile(FileMetadata metadata, byte[] content) throws Exception {
        if (content.length > MAX_FILE_SIZE) {
            throw new Exception("File too large (max 200KB)");
        }

        String fileName = metadata.getOriginalName();
        if (!fileName.toLowerCase().endsWith(".txt")) {
            throw new Exception("Only .txt files are allowed");
        }

        String serverFileName = metadata.getFileId() + "_" + fileName;
        String serverPath = STORAGE_DIR + File.separator + serverFileName;
        metadata.setServerPath(serverPath);

        try {
            Path path = Paths.get(serverPath);
            Files.write(path, content);
        } catch (IOException e) {
            throw new Exception("Failed to save file: " + e.getMessage());
        }

        fileRegistry.put(metadata.getFileId(), metadata);

        return metadata.getFileId();
    }

    public synchronized byte[] loadFile(String fileId) throws Exception {
        FileMetadata metadata = fileRegistry.get(fileId);
        if (metadata == null) {
            throw new Exception("File not found: " + fileId);
        }

        try {
            Path path = Paths.get(metadata.getServerPath());
            return Files.readAllBytes(path);
        } catch (IOException e) {
            throw new Exception("Failed to load file: " + e.getMessage());
        }
    }

    public synchronized FileMetadata getFileMetadata(String fileId) {
        return fileRegistry.get(fileId);
    }

    public synchronized boolean fileExists(String fileId) {
        return fileRegistry.containsKey(fileId);
    }

    public synchronized void deleteFile(String fileId) throws Exception {
        FileMetadata metadata = fileRegistry.get(fileId);
        if (metadata == null) {
            throw new Exception("File not found: " + fileId);
        }

        try {
            Path path = Paths.get(metadata.getServerPath());
            Files.deleteIfExists(path);
            fileRegistry.remove(fileId);
        } catch (IOException e) {
            throw new Exception("Failed to delete file: " + e.getMessage());
        }
    }
}
