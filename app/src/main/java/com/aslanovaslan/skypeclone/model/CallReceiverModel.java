package com.aslanovaslan.skypeclone.model;

import java.util.HashMap;
import java.util.Map;

public class CallReceiverModel {
private String receiverId;
private String senderUserName;
private String senderImage;
private String calling;

public CallReceiverModel() {
}

public CallReceiverModel(String receiverId, String senderUserName, String senderImage, String calling) {
    this.receiverId = receiverId;
    this.senderUserName = senderUserName;
    this.senderImage = senderImage;
    this.calling = calling;
}

public String  getReceiverId() {
    return receiverId;
}

public void  setReceiverId(String senderId) {
    this.receiverId = senderId;
}

public String getSenderUserName() {
    return senderUserName;
}

public void setSenderUserName(String senderUserName) {
    this.senderUserName = senderUserName;
}

public String getSenderImage() {
    return senderImage;
}

public void setSenderImage(String senderImage) {
    this.senderImage = senderImage;
}

public String getCalling() {
    return calling;
}
public Map<String,Object> toMap(){
    HashMap<String,Object> result=new HashMap<>();
    result.put("receiverId", this.receiverId);
    result.put("senderUserName", this.senderUserName);
    result.put("senderImage", this.senderImage);
    result.put("calling", this.calling);
    return result;
}
public void setCalling(String calling) {
    this.calling = calling;
}
}
