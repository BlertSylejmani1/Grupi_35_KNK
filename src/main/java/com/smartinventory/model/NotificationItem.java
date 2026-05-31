package com.smartinventory.model;

import java.time.LocalDateTime;

public record NotificationItem(int id, String type, String message, boolean emailSent, LocalDateTime createdAt) {
}
