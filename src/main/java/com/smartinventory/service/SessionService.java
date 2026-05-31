package com.smartinventory.service;

import com.smartinventory.model.User;

public final class SessionService {
    private static User currentUser;

    private SessionService() {
    }

    public static void login(User user) {
        currentUser = user;
    }

    public static User requireUser() {
        if (currentUser == null) {
            throw new IllegalStateException("User is not logged in");
        }
        return currentUser;
    }

    public static void logout() {
        currentUser = null;
    }
}
