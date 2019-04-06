package com.android.instapost;

public class User {

    private final String name;
    private final String nickname;
    private final String email;

    public User(String name, String nickname, String email) {
        this.name = name;
        this.nickname = nickname;
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public String getNickname() {
        return nickname;
    }

    public String getEmail() {
        return email;
    }

}
