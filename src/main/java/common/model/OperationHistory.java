package common.model;

import java.time.LocalDateTime;

public class OperationHistory {
    private int id;
    private int clientId;
    private String operation;
    private LocalDateTime timestamp;

    public OperationHistory(int id, int clientId, String operation, LocalDateTime timestamp) {
        this.id = id;
        this.clientId = clientId;
        this.operation = operation;
        this.timestamp = timestamp;
    }

    public int getId() { return id; }
    public int getClientId() { return clientId; }
    public String getOperation() { return operation; }
    public LocalDateTime getTimestamp() { return timestamp; }

    public void setId(int id) { this.id = id; }
    public void setClientId(int clientId) { this.clientId = clientId; }
    public void setOperation(String operation) { this.operation = operation; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
} 