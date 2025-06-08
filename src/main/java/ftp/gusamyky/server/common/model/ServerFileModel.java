package ftp.gusamyky.server.common.model;

import java.time.LocalDateTime;

public class ServerFileModel {
    private int id;
    private String filename;
    private long size;
    private int ownerId;
    private LocalDateTime createdAt;

    public ServerFileModel(int id, String filename, long size, int ownerId, LocalDateTime createdAt) {
        this.id = id;
        this.filename = filename;
        this.size = size;
        this.ownerId = ownerId;
        this.createdAt = createdAt;
    }

    public int getId() {
        return id;
    }

    public String getFilename() {
        return filename;
    }

    public long getSize() {
        return size;
    }

    public int getOwnerId() {
        return ownerId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public void setOwnerId(int ownerId) {
        this.ownerId = ownerId;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "ServerFileModel{" +
                "id=" + id +
                ", filename='" + filename + '\'' +
                ", size=" + size +
                ", ownerId=" + ownerId +
                ", createdAt=" + createdAt +
                '}';
    }
}