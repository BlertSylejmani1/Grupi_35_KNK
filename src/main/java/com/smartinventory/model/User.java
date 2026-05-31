package com.smartinventory.model;

public record User(int id, String username, Role role) {
    public boolean isAdmin() {
        return role == Role.ADMIN;
    }
}
