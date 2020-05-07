package com.example.legacymessenger.Data;

public class Message {

    String conversationId;
    String senderId;
    String receiverId;
    String message;
    String type;
    String time;

    public Message() {
    }

    public Message(String conversationId, String senderId, String receiverId, String message, String type, String time) {
        this.conversationId = conversationId;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.message = message;
        this.type = type;
        this.time = time;
    }

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
