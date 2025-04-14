package edu.pucmm.models;

import java.util.Date;

public class AccessLog {
    private Date timestamp;
    private String ip;
    private String userAgent;

    public AccessLog(String ip, String userAgent) {
        this.timestamp = new Date();
        this.ip = ip;
        this.userAgent = userAgent;
    }

    public Date getTimestamp() { return timestamp; }
    public String getIp() { return ip; }
    public String getUserAgent() { return userAgent; }
}
