package com.aslanovaslan.skypeclone.model;

import java.util.HashMap;
import java.util.Map;

public class TalkingModel {
public TalkingModel() {
}

public TalkingModel(String caller, String answering) {
    this.caller = caller;
    this.answering = answering;
}

private String caller, answering;


public String getCaller() {
    return caller;
}

public void setCaller(String caller) {
    this.caller = caller;
}

public String getAnswering() {
    return answering;
}

public void setAnswering(String answering) {
    this.answering = answering;
}

public Map<String,Object>toMap(){
        HashMap<String, Object> result = new HashMap<>();
        result.put("caller", this.caller);
        result.put("answering", this.answering);
        return result;
     };
}
