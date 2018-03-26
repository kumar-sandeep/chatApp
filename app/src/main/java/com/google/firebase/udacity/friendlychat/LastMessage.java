package com.google.firebase.udacity.friendlychat;

/**
 * Created by sandeep on 25-03-2018.
 */

public class LastMessage {
    public String title;
    public String lastMessage;
    public String timestamp;
    public String key;

    public LastMessage(String title, String lastMessage, String timestamp) {
        this.title = title;
        this.lastMessage = lastMessage;
        this.timestamp = timestamp;
    }

    public LastMessage() {
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
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

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    @Override
    public String toString() {
        return "LastMessage{" +
                "title='" + title + '\'' +
                ", lastMessage='" + lastMessage + '\'' +
                ", timestamp='" + timestamp + '\'' +
                ", key='" + key + '\'' +
                '}';
    }
}
