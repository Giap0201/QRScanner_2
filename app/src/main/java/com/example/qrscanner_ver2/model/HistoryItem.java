package com.example.qrscanner_ver2.model;

/**
 * POJO representing a single history entry stored in SQLite.
 */
public class HistoryItem {
    private int id;
    private String content;
    private String type;      // QrType name as String
    private long timestamp;

    public HistoryItem() {}

    public HistoryItem(String content, String type, long timestamp) {
        this.content = content;
        this.type = type;
        this.timestamp = timestamp;
    }

    // Getters
    public int getId() { return id; }
    public String getContent() { return content; }
    public String getType() { return type; }
    public long getTimestamp() { return timestamp; }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setContent(String content) { this.content = content; }
    public void setType(String type) { this.type = type; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}
