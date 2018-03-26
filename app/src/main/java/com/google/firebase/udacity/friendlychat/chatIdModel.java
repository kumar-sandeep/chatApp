package com.google.firebase.udacity.friendlychat;

import java.util.SimpleTimeZone;

/**
 * Created by sandeep on 25-03-2018.
 */

public class chatIdModel {
    private String lastMessage;
    private String timestamp;
    private String title;

    public chatIdModel(String lastMessage, String timestamp, String title) {
        this.lastMessage = lastMessage;
        this.timestamp = timestamp;
        this.title = title;
    }

    public chatIdModel() {
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public String toString() {
        return "chatIdModel{" +
                "lastMessage='" + lastMessage + '\'' +
                ", timestamp='" + timestamp + '\'' +
                ", title='" + title + '\'' +
                '}';
    }
}
