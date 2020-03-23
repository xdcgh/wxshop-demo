package com.xdc.wxshopdemo.entity;

import com.xdc.wxshopdemo.generate.User;

public class LoginResponse {
    private boolean login = false;
    private User user;

    public static LoginResponse notLogin() {
        return new LoginResponse(false, null);
    }

    public static LoginResponse login(User user) {
        return new LoginResponse(true, user);
    }

    public LoginResponse() {
    }

    private LoginResponse(boolean login, User user) {
        this.login = login;
        this.user = user;
    }

    public boolean isLogin() {
        return login;
    }

    public User getUser() {
        return user;
    }
}
