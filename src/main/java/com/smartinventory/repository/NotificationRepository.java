package com.smartinventory.repository;

import com.smartinventory.config.Database;
import com.smartinventory.model.NotificationItem;
import com.smartinventory.model.Product;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class NotificationRepository {
    public void stockAlert(Product product, boolean emailSent) {
        String sql = "INSERT INTO notifications (type, message, email_sent) VALUES (?, ?, ?)";
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, product.getQuantity() == 0 ? "OUT_OF_STOCK" : "LOW_STOCK");
            statement.setString(2, product.getName() + " has stock " + product.getQuantity() + " (" + product.getStatus() + ")");
            statement.setBoolean(3, emailSent);
            statement.executeUpdate();
        } catch (Exception ignored) {
            // Notifications are secondary; product saving must continue.
        }
    }

    public List<NotificationItem> latest() throws SQLException {
        List<NotificationItem> notifications = new ArrayList<>();
        String sql = "SELECT id, type, message, email_sent, created_at FROM notifications ORDER BY created_at DESC LIMIT 100";
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {
            while (rs.next()) {
                notifications.add(new NotificationItem(rs.getInt("id"), rs.getString("type"), rs.getString("message"),
                        rs.getBoolean("email_sent"), rs.getObject("created_at", LocalDateTime.class)));
            }
        }
        return notifications;
    }
}
