package com.smartinventory.repository;

import com.smartinventory.config.Database;
import com.smartinventory.model.AuditLog;
import com.smartinventory.service.SessionService;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AuditLogRepository {
    public void record(String action, String entity, String details) {
        String sql = "INSERT INTO audit_logs (username, action, entity, details) VALUES (?, ?, ?, ?)";
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, SessionService.requireUser().username());
            statement.setString(2, action);
            statement.setString(3, entity);
            statement.setString(4, details);
            statement.executeUpdate();
        } catch (Exception ignored) {
            // Audit logging must never block the user's main workflow.
        }
    }

    public List<AuditLog> latest() throws SQLException {
        List<AuditLog> logs = new ArrayList<>();
        String sql = "SELECT id, username, action, entity, details, created_at FROM audit_logs ORDER BY created_at DESC LIMIT 100";
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {
            while (result.next()) {
                logs.add(new AuditLog(
                        result.getInt("id"),
                        result.getString("username"),
                        result.getString("action"),
                        result.getString("entity"),
                        result.getString("details"),
                        result.getObject("created_at", LocalDateTime.class)
                ));
            }
        }
        return logs;
    }
}
