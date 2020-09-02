package com.aslanovaslan.skypeclone.model;

import java.util.HashMap;
import java.util.Map;

public class CallingModel {
private String answeringId;

private String callerId;



public CallingModel() {
}


public CallingModel(String answeringId, String callerId) {
    this.answeringId = answeringId;

    this.callerId = callerId;

}

public String getAnsweringId() {
    return answeringId;
}

public void setAnsweringId(String answeringId) {
    this.answeringId = answeringId;
}

public String getCallerId() {
    return callerId;
}

public void setCallerId(String callerId) {
    this.callerId = callerId;
}


public Map<String,Object> toMap(){
    HashMap<String,Object> result=new HashMap<>();
    result.put("answeringId", this.answeringId);
    result.put("callerId", this.callerId);

    return result;
}

}
