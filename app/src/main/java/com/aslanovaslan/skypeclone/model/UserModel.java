package com.aslanovaslan.skypeclone.model;

import android.os.Parcel;
import android.os.Parcelable;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class UserModel implements Parcelable {

private String nameSurname;
private String userBio;
private String profilePicturePath;
private String uid;
private String status;
private String channel;

public String getStatus() {
    return status;
}

public void setStatus(String status) {
    this.status = status;
}

public static Creator<UserModel> getCREATOR() {
    return CREATOR;
}


public UserModel() {
}

public UserModel(String nameSurname, String userBio, String profilePicturePath, String uid,String status,String channel) {
    this.nameSurname = nameSurname;
    this.userBio = userBio;
    this.profilePicturePath = profilePicturePath;
    this.uid = uid;
    this.status = status;
    this.channel = channel;
}


protected UserModel(Parcel in) {
    nameSurname = in.readString();
    userBio = in.readString();
    profilePicturePath = in.readString();
    uid = in.readString();
}

public static final Creator<UserModel> CREATOR = new Creator<UserModel>() {
    @Override
    public UserModel createFromParcel(Parcel in) {
        return new UserModel(in);
    }

    @Override
    public UserModel[] newArray(int size) {
        return new UserModel[size];
    }
};

public String getChannel() {
    return channel;
}

public void setChannel(String channel) {
    this.channel = channel;
}

@NotNull
@Override
public String toString() {
    return "RegisterModel{" +
                   "nameSurname='" + nameSurname + '\'' +
                   ", userBio='" + userBio + '\'' +
                   ", profilePicturePath='" + profilePicturePath + '\'' +
                   '}';
}

public Map<String, Object> toMap() {

    HashMap<String, Object> result = new HashMap<>();
    result.put("nameSurname", this.nameSurname);
    result.put("userBio", this.userBio);
    result.put("profilePicturePath", this.profilePicturePath);
    result.put("status", this.status);

    return result;
}

public String getUid() {
    return uid;
}

public void setUid(String uid) {
    this.uid = uid;
}

public String getNameSurname() {
    return nameSurname;
}

public void setNameSurname(String nameSurname) {
    this.nameSurname = nameSurname;
}

public String getUserBio() {
    return userBio;
}

public void setUserBio(String userBio) {
    this.userBio = userBio;
}

public String getProfilePicturePath() {
    return profilePicturePath;
}

public void setProfilePicturePath(String profilePicturePath) {
    this.profilePicturePath = profilePicturePath;
}

@Override
public int describeContents() {
    return 0;
}

@Override
public void writeToParcel(Parcel parcel, int i) {
    parcel.writeString(nameSurname);
    parcel.writeString(userBio);
    parcel.writeString(profilePicturePath);
    parcel.writeString(uid);
}
}
