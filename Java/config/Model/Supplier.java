package com.smartinventory.model;

public record Supplier(int id, String name, String email, String phone, String address) {
    @Override
    public String toString() {
        return name;
    }
}
