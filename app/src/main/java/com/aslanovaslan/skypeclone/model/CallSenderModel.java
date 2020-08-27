package com.aslanovaslan.skypeclone.model;

import java.util.HashMap;
import java.util.Map;

public class CallSenderModel {

private String  senderId;
private String receiverUserName;
private String receiverImage;
private String ringing;

public CallSenderModel() {
}

public CallSenderModel(String senderId, String receiverUserName, String receiverImage, String ringing) {
    this.senderId = senderId;
    this.receiverUserName = receiverUserName;
    this.receiverImage = receiverImage;
    this.ringing = ringing;
}

public String getSenderId() {
    return senderId;
}

public void setSenderId(String receiverId) {
    this.senderId = receiverId;
}

public String getReceiverUserName() {
    return receiverUserName;
}

public void setReceiverUserName(String receiverUserName) {
    this.receiverUserName = receiverUserName;
}

public String getReceiverImage() {
    return receiverImage;
}

public void setReceiverImage(String receiverImage) {
    this.receiverImage = receiverImage;
}

public String getRinging() {
    return ringing;
}

public void setRinging(String ringing) {
    this.ringing = ringing;
}
public Map<String, Object> toMap() {

    HashMap<String, Object> result = new HashMap<>();
    result.put("senderId", this.senderId);
    result.put("receiverUserName", this.receiverUserName);
    result.put("receiverImage", this.receiverImage);
    result.put("ringing", this.ringing);

    return result;
}

}
