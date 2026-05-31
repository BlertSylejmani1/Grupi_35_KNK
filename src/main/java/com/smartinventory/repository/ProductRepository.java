package com.smartinventory.repository;

import com.smartinventory.config.Database;
import com.smartinventory.model.Product;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ProductRepository {
    public List<Product> findAll(String search, String category) throws SQLException {
        List<Product> products = new ArrayList<>();
        String sql = """
                SELECT id, name, category, quantity, price, supplier_id, supplier, status
                FROM products
                WHERE (? = '' OR name LIKE ? OR supplier LIKE ? OR CAST(id AS CHAR) = ?)
                  AND (? = '' OR category = ?)
                ORDER BY id DESC
                """;
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            String term = search == null ? "" : search.trim();
            String like = "%" + term + "%";
            String selectedCategory = category == null ? "" : category;
            statement.setString(1, term);
            statement.setString(2, like);
            statement.setString(3, like);
            statement.setString(4, term);
            statement.setString(5, selectedCategory);
            statement.setString(6, selectedCategory);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    products.add(map(rs));
                }
            }
        }
        return products;
    }

    public List<String> categories() throws SQLException {
        List<String> categories = new ArrayList<>();
        String sql = "SELECT DISTINCT category FROM products ORDER BY category";
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {
            while (rs.next()) {
                categories.add(rs.getString("category"));
            }
        }
        return categories;
    }

    public Optional<Product> findById(int id) throws SQLException {
        String sql = "SELECT id, name, category, quantity, price, supplier_id, supplier, status FROM products WHERE id = ?";
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        }
    }

    public void insert(Product product) throws SQLException {
        String sql = "INSERT INTO products (name, category, quantity, price, supplier_id, supplier, status) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bind(product, statement);
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    product.setId(keys.getInt(1));
                }
            }
        }
    }

    public void update(Product product) throws SQLException {
        String sql = "UPDATE products SET name = ?, category = ?, quantity = ?, price = ?, supplier_id = ?, supplier = ?, status = ? WHERE id = ?";
        try (Connection connection = Database.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql)) {
            bind(product, statement);
            statement.setInt(8, product.getId());
            statement.executeUpdate();
        }
    }

    public List<Product> advancedSearch(String name, String category, String supplier, BigDecimal minPrice,
                                        BigDecimal maxPrice, Integer minStock, Integer maxStock) throws SQLException {
        List<Product> products = new ArrayList<>();
        String sql = """
                SELECT id, name, category, quantity, price, supplier_id, supplier, status
                FROM products
                WHERE (? = '' OR name LIKE ?)
                  AND (? = '' OR category = ?)
                  AND (? = '' OR supplier LIKE ?)
                  AND (? IS NULL OR price >= ?)
                  AND (? IS NULL OR price <= ?)
                  AND (? IS NULL OR quantity >= ?)
                  AND (? IS NULL OR quantity <= ?)
                ORDER BY id DESC
                """;
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            String productName = name == null ? "" : name.trim();
            String categoryValue = category == null ? "" : category.trim();
            String supplierValue = supplier == null ? "" : supplier.trim();
            statement.setString(1, productName);
            statement.setString(2, "%" + productName + "%");
            statement.setString(3, categoryValue);
            statement.setString(4, categoryValue);
            statement.setString(5, supplierValue);
            statement.setString(6, "%" + supplierValue + "%");
            statement.setBigDecimal(7, minPrice);
            statement.setBigDecimal(8, minPrice);
            statement.setBigDecimal(9, maxPrice);
            statement.setBigDecimal(10, maxPrice);
            statement.setObject(11, minStock);
            statement.setObject(12, minStock);
            statement.setObject(13, maxStock);
            statement.setObject(14, maxStock);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    products.add(map(rs));
                }
            }
        }
        return products;
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM products WHERE id = ?";
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            statement.executeUpdate();
        }
    }

    public boolean existsByName(String name, int exceptId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM products WHERE LOWER(name) = LOWER(?) AND id <> ?";
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, name);
            statement.setInt(2, exceptId);
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    private void bind(Product product, PreparedStatement statement) throws SQLException {
        product.setStatus(statusFor(product.getQuantity()));
        statement.setString(1, product.getName());
        statement.setString(2, product.getCategory());
        statement.setInt(3, product.getQuantity());
        statement.setBigDecimal(4, product.getPrice());
        if (product.getSupplierId() > 0) {
            statement.setInt(5, product.getSupplierId());
        } else {
            statement.setNull(5, Types.INTEGER);
        }
        statement.setString(6, product.getSupplier());
        statement.setString(7, product.getStatus());
    }

    private String statusFor(int quantity) {
        if (quantity == 0) {
            return "OUT_OF_STOCK";
        }
        if (quantity <= 5) {
            return "LOW_STOCK";
        }
        return "IN_STOCK";
    }

    private Product map(ResultSet rs) throws SQLException {
        return new Product(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getString("category"),
                rs.getInt("quantity"),
                rs.getBigDecimal("price"),
                rs.getInt("supplier_id"),
                rs.getString("supplier"),
                rs.getString("status")
        );
    }
}
