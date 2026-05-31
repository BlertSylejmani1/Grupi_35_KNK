package com.smartinventory.model;

import javafx.beans.property.*;

import java.math.BigDecimal;

public class Product {
    private final IntegerProperty id = new SimpleIntegerProperty();
    private final StringProperty name = new SimpleStringProperty();
    private final StringProperty category = new SimpleStringProperty();
    private final IntegerProperty quantity = new SimpleIntegerProperty();
    private final ObjectProperty<BigDecimal> price = new SimpleObjectProperty<>(BigDecimal.ZERO);
    private final IntegerProperty supplierId = new SimpleIntegerProperty();
    private final StringProperty supplier = new SimpleStringProperty();
    private final StringProperty status = new SimpleStringProperty();

    public Product() {
    }

    public Product(int id, String name, String category, int quantity, BigDecimal price, String supplier, String status) {
        this(id, name, category, quantity, price, 0, supplier, status);
    }

    public Product(int id, String name, String category, int quantity, BigDecimal price, int supplierId, String supplier, String status) {
        setId(id);
        setName(name);
        setCategory(category);
        setQuantity(quantity);
        setPrice(price);
        setSupplierId(supplierId);
        setSupplier(supplier);
        setStatus(status);
    }

    public int getId() { return id.get(); }
    public void setId(int value) { id.set(value); }
    public IntegerProperty idProperty() { return id; }

    public String getName() { return name.get(); }
    public void setName(String value) { name.set(value); }
    public StringProperty nameProperty() { return name; }

    public String getCategory() { return category.get(); }
    public void setCategory(String value) { category.set(value); }
    public StringProperty categoryProperty() { return category; }

    public int getQuantity() { return quantity.get(); }
    public void setQuantity(int value) { quantity.set(value); }
    public IntegerProperty quantityProperty() { return quantity; }

    public BigDecimal getPrice() { return price.get(); }
    public void setPrice(BigDecimal value) { price.set(value); }
    public ObjectProperty<BigDecimal> priceProperty() { return price; }

    public int getSupplierId() { return supplierId.get(); }
    public void setSupplierId(int value) { supplierId.set(value); }
    public IntegerProperty supplierIdProperty() { return supplierId; }

    public String getSupplier() { return supplier.get(); }
    public void setSupplier(String value) { supplier.set(value); }
    public StringProperty supplierProperty() { return supplier; }

    public String getStatus() { return status.get(); }
    public void setStatus(String value) { status.set(value); }
    public StringProperty statusProperty() { return status; }
}
