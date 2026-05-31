package com.smartinventory.repository;

import com.smartinventory.config.Database;
import com.smartinventory.model.Product;
import com.smartinventory.model.PurchaseOrder;
import com.smartinventory.service.SessionService;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PurchaseOrderRepository {
    public int createForProduct(Product product, int reorderQuantity) throws SQLException {
        String orderSql = "INSERT INTO purchase_orders (supplier_id, status, created_by) VALUES (?, 'PENDING', ?)";
        String itemSql = "INSERT INTO purchase_order_items (purchase_order_id, product_id, quantity, unit_price) VALUES (?, ?, ?, ?)";
        try (Connection connection = Database.getConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement order = connection.prepareStatement(orderSql, Statement.RETURN_GENERATED_KEYS)) {
                if (product.getSupplierId() > 0) {
                    order.setInt(1, product.getSupplierId());
                } else {
                    order.setNull(1, Types.INTEGER);
                }
                order.setString(2, SessionService.requireUser().username());
                order.executeUpdate();
                try (ResultSet keys = order.getGeneratedKeys()) {
                    if (!keys.next()) {
                        throw new SQLException("No purchase order id returned");
                    }
                    int orderId = keys.getInt(1);
                    try (PreparedStatement item = connection.prepareStatement(itemSql)) {
                        item.setInt(1, orderId);
                        item.setInt(2, product.getId());
                        item.setInt(3, reorderQuantity);
                        item.setBigDecimal(4, product.getPrice());
                        item.executeUpdate();
                    }
                    connection.commit();
                    return orderId;
                }
            } catch (SQLException ex) {
                connection.rollback();
                throw ex;
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    public List<PurchaseOrder> findAll() throws SQLException {
        List<PurchaseOrder> orders = new ArrayList<>();
        String sql = """
                SELECT po.id, COALESCE(s.name, 'No supplier') supplier, p.name product_name,
                       poi.quantity, po.status, po.created_by, po.created_at
                FROM purchase_orders po
                LEFT JOIN suppliers s ON s.id = po.supplier_id
                LEFT JOIN purchase_order_items poi ON poi.purchase_order_id = po.id
                LEFT JOIN products p ON p.id = poi.product_id
                ORDER BY po.created_at DESC
                """;
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {
            while (rs.next()) {
                orders.add(new PurchaseOrder(rs.getInt("id"), rs.getString("supplier"), rs.getString("product_name"),
                        rs.getInt("quantity"), rs.getString("status"), rs.getString("created_by"),
                        rs.getObject("created_at", LocalDateTime.class)));
            }
        }
        return orders;
    }

    public void updateStatus(int id, String status) throws SQLException {
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement("UPDATE purchase_orders SET status = ? WHERE id = ?")) {
            statement.setString(1, status);
            statement.setInt(2, id);
            statement.executeUpdate();
        }
    }

    public String receive(int id, String changedBy) throws SQLException {
        String selectSql = """
                SELECT poi.product_id, poi.quantity, p.name, p.quantity old_quantity
                FROM purchase_order_items poi
                JOIN products p ON p.id = poi.product_id
                JOIN purchase_orders po ON po.id = poi.purchase_order_id
                WHERE po.id = ? AND po.status = 'APPROVED'
                FOR UPDATE
                """;
        try (Connection connection = Database.getConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement select = connection.prepareStatement(selectSql)) {
                select.setInt(1, id);
                try (ResultSet rs = select.executeQuery()) {
                    if (!rs.next()) {
                        connection.rollback();
                        return "No pending items";
                    }
                    int productId = rs.getInt("product_id");
                    int orderedQuantity = rs.getInt("quantity");
                    int oldQuantity = rs.getInt("old_quantity");
                    int newQuantity = oldQuantity + orderedQuantity;
                    String productName = rs.getString("name");
                    try (PreparedStatement updateProduct = connection.prepareStatement("UPDATE products SET quantity = ?, status = inventory_status(?) WHERE id = ?")) {
                        updateProduct.setInt(1, newQuantity);
                        updateProduct.setInt(2, newQuantity);
                        updateProduct.setInt(3, productId);
                        updateProduct.executeUpdate();
                    }
                    try (PreparedStatement history = connection.prepareStatement("INSERT INTO product_stock_history (product_id, old_quantity, new_quantity, changed_by, change_type, reason) VALUES (?, ?, ?, ?, 'RESTOCK', ?)")) {
                        history.setInt(1, productId);
                        history.setInt(2, oldQuantity);
                        history.setInt(3, newQuantity);
                        history.setString(4, changedBy);
                        history.setString(5, "Purchase order #" + id + " received");
                        history.executeUpdate();
                    }
                    try (PreparedStatement updateOrder = connection.prepareStatement("UPDATE purchase_orders SET status = 'RECEIVED' WHERE id = ?")) {
                        updateOrder.setInt(1, id);
                        updateOrder.executeUpdate();
                    }
                    connection.commit();
                    return productName + ": " + oldQuantity + " -> " + newQuantity;
                }
            } catch (SQLException ex) {
                connection.rollback();
                throw ex;
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }
}
