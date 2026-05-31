package com.smartinventory.service;

import com.smartinventory.model.Product;

public class StockAlertService {
    private final EmailService emailService = new EmailService();

    public boolean notifyIfNeeded(Product product) {
        if (product.getQuantity() > 5) {
            return false;
        }
        String subject = product.getQuantity() == 0
                ? "OUT OF STOCK: " + product.getName()
                : "LOW STOCK: " + product.getName();
        String body = """
                Smart Inventory stock alert

                Product ID: %d
                Name: %s
                Category: %s
                Supplier: %s
                Price: %s
                Stock: %d
                Status: %s

                Recommended action: restock this product as soon as possible.
                """.formatted(
                product.getId(),
                product.getName(),
                product.getCategory(),
                product.getSupplier(),
                product.getPrice().toPlainString(),
                product.getQuantity(),
                product.getStatus()
        );
        return emailService.send(subject, body);
    }
}
