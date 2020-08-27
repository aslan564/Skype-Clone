package com.aslanovaslan.skypeclone.model;

import android.os.Parcel;
import android.os.Parcelable;

import org.jetbrains.annotations.NotNull;

public class RequestModel implements Parcelable {

private String receiverId;
private String senderId;
private String state;

public RequestModel() {
}

public RequestModel(String senderId, String receiverId, String state) {
    this.receiverId = receiverId;
    this.senderId = senderId;
    this.state = state;
}

protected RequestModel(Parcel in) {
    receiverId = in.readString();
    senderId = in.readString();
    state = in.readString();
}

@NotNull
@Override
public String toString() {
    return "RequestModel{" +
                   "receiverId='" + receiverId + '\'' +
                   ", senderId='" + senderId + '\'' +
                   ", state='" + state + '\'' +
                   '}';
}

public static final Creator<RequestModel> CREATOR = new Creator<RequestModel>() {
    @Override
    public RequestModel createFromParcel(Parcel in) {
        return new RequestModel(in);
    }

    @Override
    public RequestModel[] newArray(int size) {
        return new RequestModel[size];
    }
};

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

public String getState() {
    return state;
}

public void setState(String state) {
    this.state = state;
}

@Override
public int describeContents() {
    return 0;
}

@Override
public void writeToParcel(Parcel parcel, int i) {
    parcel.writeString(receiverId);
    parcel.writeString(senderId);
    parcel.writeString(state);
}
}
