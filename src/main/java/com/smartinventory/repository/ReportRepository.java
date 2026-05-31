package com.smartinventory.repository;

import com.smartinventory.config.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

public class ReportRepository {
    public int totalProducts() throws SQLException {
        return singleInt("SELECT COUNT(*) FROM products");
    }

    public int lowStockProducts() throws SQLException {
        return singleInt("SELECT COUNT(*) FROM products WHERE quantity <= 5");
    }

    public int outOfStockProducts() throws SQLException {
        return singleInt("SELECT COUNT(*) FROM products WHERE quantity = 0");
    }

    public int totalSuppliers() throws SQLException {
        return singleInt("SELECT COUNT(*) FROM suppliers");
    }

    public int totalUsers() throws SQLException {
        return singleInt("SELECT COUNT(*) FROM users");
    }

    public Map<String, Integer> byCategory() throws SQLException {
        return grouped("SELECT category, COUNT(*) total FROM products GROUP BY category ORDER BY category");
    }

    public Map<String, Integer> stockByCategory() throws SQLException {
        return grouped("SELECT category, SUM(quantity) total FROM products GROUP BY category ORDER BY category");
    }

    private int singleInt(String sql) throws SQLException {
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    private Map<String, Integer> grouped(String sql) throws SQLException {
        Map<String, Integer> values = new LinkedHashMap<>();
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {
            while (rs.next()) {
                values.put(rs.getString(1), rs.getInt(2));
            }
        }
        return values;
    }
}
