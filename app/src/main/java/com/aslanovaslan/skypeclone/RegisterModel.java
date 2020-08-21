package com.aslanovaslan.skypeclone;

import org.jetbrains.annotations.NotNull;

public class RegisterModel {

    private String nameSurname="";
    private String userBio="";
    private String profilePicturePath="";

public RegisterModel() {
}

public RegisterModel(String nameSurname, String userBio, String profilePicturePath) {
    this.nameSurname = nameSurname;
    this.userBio = userBio;
    this.profilePicturePath = profilePicturePath;
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
}
