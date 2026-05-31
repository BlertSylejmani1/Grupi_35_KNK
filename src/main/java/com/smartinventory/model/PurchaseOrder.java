package com.smartinventory.model;

import java.time.LocalDateTime;

public record PurchaseOrder(int id, String supplier, String productName, int quantity, String status,
                            String createdBy, LocalDateTime createdAt) {
}
