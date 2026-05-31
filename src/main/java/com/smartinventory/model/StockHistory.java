package com.smartinventory.model;

import java.time.LocalDateTime;

public record StockHistory(int id, int productId, int oldQuantity, int newQuantity, String changedBy,
                           String changeType, String reason, LocalDateTime createdAt) {
}
