package com.aslanovaslan.skypeclone.util.internal;

import com.aslanovaslan.skypeclone.model.UserModel;

public class MessageEvent {
public static class MessageShareEvent {
    public UserModel getUserModel() {
        return userModel;
    }

    UserModel userModel;
    public MessageShareEvent(UserModel userModel) {
        this.userModel=userModel;
    }
}
}
