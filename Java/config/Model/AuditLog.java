package com.smartinventory.model;

import java.time.LocalDateTime;

public record AuditLog(int id, String username, String action, String entity, String details, LocalDateTime createdAt) {
}
