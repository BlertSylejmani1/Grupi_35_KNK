package com.smartinventory.repository;

import com.smartinventory.config.Database;
import com.smartinventory.model.StockHistory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class StockHistoryRepository {
    public void record(int productId, int oldQuantity, int newQuantity, String changedBy, String changeType, String reason) {
        String sql = "INSERT INTO product_stock_history (product_id, old_quantity, new_quantity, changed_by, change_type, reason) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, productId);
            statement.setInt(2, oldQuantity);
            statement.setInt(3, newQuantity);
            statement.setString(4, changedBy);
            statement.setString(5, changeType);
            statement.setString(6, reason);
            statement.executeUpdate();
        } catch (Exception ignored) {
        }
    }

    public List<StockHistory> forProduct(int productId) throws SQLException {
        List<StockHistory> rows = new ArrayList<>();
        String sql = "SELECT id, product_id, old_quantity, new_quantity, changed_by, change_type, reason, created_at FROM product_stock_history WHERE product_id = ? ORDER BY created_at DESC";
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, productId);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    rows.add(new StockHistory(rs.getInt("id"), rs.getInt("product_id"), rs.getInt("old_quantity"),
                            rs.getInt("new_quantity"), rs.getString("changed_by"), rs.getString("change_type"),
                            rs.getString("reason"), rs.getObject("created_at", LocalDateTime.class)));
                }
            }
        }
        return rows;
    }
}
