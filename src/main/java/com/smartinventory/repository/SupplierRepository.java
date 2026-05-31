package com.smartinventory.repository;

import com.smartinventory.config.Database;
import com.smartinventory.model.Supplier;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SupplierRepository {
    public List<Supplier> findAll() throws SQLException {
        List<Supplier> suppliers = new ArrayList<>();
        String sql = "SELECT id, name, email, phone, address FROM suppliers ORDER BY name";
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {
            while (rs.next()) {
                suppliers.add(map(rs));
            }
        }
        return suppliers;
    }

    public Supplier save(Supplier supplier) throws SQLException {
        if (supplier.id() == 0) {
            String sql = "INSERT INTO suppliers (name, email, phone, address) VALUES (?, ?, ?, ?)";
            try (Connection connection = Database.getConnection();
                 PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                bind(supplier, statement);
                statement.executeUpdate();
                try (ResultSet keys = statement.getGeneratedKeys()) {
                    if (keys.next()) {
                        return new Supplier(keys.getInt(1), supplier.name(), supplier.email(), supplier.phone(), supplier.address());
                    }
                }
            }
        } else {
            String sql = "UPDATE suppliers SET name = ?, email = ?, phone = ?, address = ? WHERE id = ?";
            try (Connection connection = Database.getConnection();
                 PreparedStatement statement = connection.prepareStatement(sql)) {
                bind(supplier, statement);
                statement.setInt(5, supplier.id());
                statement.executeUpdate();
            }
        }
        return supplier;
    }

    public void delete(int id) throws SQLException {
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement("DELETE FROM suppliers WHERE id = ?")) {
            statement.setInt(1, id);
            statement.executeUpdate();
        }
    }

    private void bind(Supplier supplier, PreparedStatement statement) throws SQLException {
        statement.setString(1, supplier.name());
        statement.setString(2, supplier.email());
        statement.setString(3, supplier.phone());
        statement.setString(4, supplier.address());
    }

    private Supplier map(ResultSet rs) throws SQLException {
        return new Supplier(rs.getInt("id"), rs.getString("name"), rs.getString("email"), rs.getString("phone"), rs.getString("address"));
    }
}
