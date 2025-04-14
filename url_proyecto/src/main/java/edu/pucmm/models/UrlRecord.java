package edu.pucmm.models;

import org.bson.types.ObjectId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class UrlRecord {
    private ObjectId id;
    private String originalUrl;
    private String shortCode;
    private Date creationDate;
    private int accessCount;
    private List<AccessLog> accessLogs;

    public UrlRecord(String originalUrl, String shortCode) {
        this.id = new ObjectId();
        this.originalUrl = originalUrl;
        this.shortCode = shortCode;
        this.creationDate = new Date();
        this.accessCount = 0;
        this.accessLogs = new ArrayList<>();
    }

    public ObjectId getId() { return id; }
    public String getOriginalUrl() { return originalUrl; }
    public String getShortCode() { return shortCode; }
    public Date getCreationDate() { return creationDate; }
    public int getAccessCount() { return accessCount; }
    public List<AccessLog> getAccessLogs() { return accessLogs; }

    public void incrementAccessCount() {
        this.accessCount++;
    }

    public void addAccessLog(AccessLog log) {
        this.accessLogs.add(log);
    }
}
