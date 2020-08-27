package com.aslanovaslan.skypeclone.model;

public class FriendModel {
    private  String nameSurname;
    private  String friendUid;
    private  String state;

public FriendModel() {
}

public FriendModel(String nameSurname, String friendUid, String state) {
    this.nameSurname = nameSurname;
    this.friendUid = friendUid;
    this.state = state;
}

public String getNameSurname() {
    return nameSurname;
}

public void setNameSurname(String nameSurname) {
    this.nameSurname = nameSurname;
}

public String getFriendUid() {
    return friendUid;
}

public void setFriendUid(String friendUid) {
    this.friendUid = friendUid;
}

public String getState() {
    return state;
}

public void setState(String state) {
    this.state = state;
}
}
