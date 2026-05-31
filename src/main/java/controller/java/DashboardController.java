package com.smartinventory.controller;

import com.smartinventory.SmartInventoryApp;
import com.smartinventory.config.Database;
import com.smartinventory.model.*;
import com.smartinventory.repository.*;
import com.smartinventory.service.AlertService;
import com.smartinventory.service.EmailService;
import com.smartinventory.service.ExportService;
import com.smartinventory.service.LanguageService;
import com.smartinventory.service.QrCodeService;
import com.smartinventory.service.SessionService;
import com.smartinventory.service.StockAlertService;
import javafx.animation.KeyFrame;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.prefs.Preferences;

public class DashboardController {
    @FXML private BorderPane rootPane;
    @FXML private TabPane mainTabs;
    @FXML private Tab productsTab;
    @FXML private Tab reportsTab;
    @FXML private Label userLabel;
    @FXML private Label footerUserLabel;
    @FXML private Label timeLabel;
    @FXML private Label dbStatusLabel;
    @FXML private Label footerDbStatusLabel;
    @FXML private Label totalProductsLabel;
    @FXML private Label lowStockLabel;
    @FXML private Label outStockLabel;
    @FXML private Label totalSuppliersLabel;
    @FXML private Label totalUsersLabel;
    @FXML private TextField searchField;
    @FXML private TextField advancedNameField;
    @FXML private TextField advancedSupplierField;
    @FXML private TextField minPriceField;
    @FXML private TextField maxPriceField;
    @FXML private TextField minStockField;
    @FXML private TextField maxStockField;
    @FXML private ComboBox<String> categoryFilter;
    @FXML private TableView<Product> productTable;
    @FXML private TableColumn<Product, Number> idColumn;
    @FXML private TableColumn<Product, String> nameColumn;
    @FXML private TableColumn<Product, String> categoryColumn;
    @FXML private TableColumn<Product, Number> quantityColumn;
    @FXML private TableColumn<Product, BigDecimal> priceColumn;
    @FXML private TableColumn<Product, String> supplierColumn;
    @FXML private TableColumn<Product, String> statusColumn;
    @FXML private PieChart categoryPieChart;
    @FXML private BarChart<String, Number> stockBarChart;
    @FXML private LineChart<String, Number> stockLineChart;
    @FXML private Tab usersTab;
    @FXML private Tab auditTab;
    @FXML private Tab settingsTab;
    @FXML private Tab suppliersTab;
    @FXML private Tab purchaseOrdersTab;
    @FXML private Tab stockHistoryTab;
    @FXML private Tab notificationsTab;
    @FXML private ListView<String> activityFeedList;
    @FXML private TableView<User> userTable;
    @FXML private TableColumn<User, Number> userIdColumn;
    @FXML private TableColumn<User, String> usernameColumn;
    @FXML private TableColumn<User, String> userRoleColumn;
    @FXML private TableView<AuditLog> auditTable;
    @FXML private TableColumn<AuditLog, String> auditTimeColumn;
    @FXML private TableColumn<AuditLog, String> auditUserColumn;
    @FXML private TableColumn<AuditLog, String> auditActionColumn;
    @FXML private TableColumn<AuditLog, String> auditEntityColumn;
    @FXML private TableColumn<AuditLog, String> auditDetailsColumn;
    @FXML private TableView<Supplier> supplierTable;
    @FXML private TableColumn<Supplier, Number> supplierIdColumn;
    @FXML private TableColumn<Supplier, String> supplierNameColumn;
    @FXML private TableColumn<Supplier, String> supplierEmailColumn;
    @FXML private TableColumn<Supplier, String> supplierPhoneColumn;
    @FXML private TableColumn<Supplier, String> supplierAddressColumn;
    @FXML private TableView<PurchaseOrder> purchaseOrderTable;
    @FXML private TableColumn<PurchaseOrder, Number> poIdColumn;
    @FXML private TableColumn<PurchaseOrder, String> poSupplierColumn;
    @FXML private TableColumn<PurchaseOrder, String> poProductColumn;
    @FXML private TableColumn<PurchaseOrder, Number> poQuantityColumn;
    @FXML private TableColumn<PurchaseOrder, String> poStatusColumn;
    @FXML private TableColumn<PurchaseOrder, String> poCreatedByColumn;
    @FXML private TableColumn<PurchaseOrder, String> poCreatedAtColumn;
    @FXML private TableView<StockHistory> stockHistoryTable;
    @FXML private TableColumn<StockHistory, String> historyTimeColumn;
    @FXML private TableColumn<StockHistory, Number> historyProductColumn;
    @FXML private TableColumn<StockHistory, Number> historyOldColumn;
    @FXML private TableColumn<StockHistory, Number> historyNewColumn;
    @FXML private TableColumn<StockHistory, String> historyTypeColumn;
    @FXML private TableColumn<StockHistory, String> historyByColumn;
    @FXML private TableColumn<StockHistory, String> historyReasonColumn;
    @FXML private TableView<NotificationItem> notificationTable;
    @FXML private TableColumn<NotificationItem, String> notificationTimeColumn;
    @FXML private TableColumn<NotificationItem, String> notificationTypeColumn;
    @FXML private TableColumn<NotificationItem, String> notificationMessageColumn;
    @FXML private TableColumn<NotificationItem, String> notificationEmailColumn;
    @FXML private Button addButton;
    @FXML private Button editButton;
    @FXML private Button deleteButton;
    @FXML private Button addUserButton;
    @FXML private Button editUserButton;
    @FXML private Button deleteUserButton;
    @FXML private Button themeButton;

    private final ProductRepository productRepository = new ProductRepository();
    private final ReportRepository reportRepository = new ReportRepository();
    private final UserRepository userRepository = new UserRepository();
    private final AuditLogRepository auditLogRepository = new AuditLogRepository();
    private final NotificationRepository notificationRepository = new NotificationRepository();
    private final SupplierRepository supplierRepository = new SupplierRepository();
    private final StockHistoryRepository stockHistoryRepository = new StockHistoryRepository();
    private final PurchaseOrderRepository purchaseOrderRepository = new PurchaseOrderRepository();
    private final StockAlertService stockAlertService = new StockAlertService();
    private final ExportService exportService = new ExportService();
    private final Preferences preferences = Preferences.userNodeForPackage(DashboardController.class);
    private final PauseTransition sessionTimeout = new PauseTransition(Duration.minutes(15));
    private final PauseTransition searchDebounce = new PauseTransition(Duration.millis(250));
    private final DateTimeFormatter auditFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @FXML
    private void initialize() {
        User user = SessionService.requireUser();
        userLabel.setText(user.username() + " (" + user.role() + ")");
        footerUserLabel.setText(user.username() + " (" + user.role() + ")");
        boolean admin = user.isAdmin();
        addButton.setDisable(!admin);
        editButton.setDisable(!admin);
        deleteButton.setDisable(!admin);
        addUserButton.setDisable(!admin);
        editUserButton.setDisable(!admin);
        deleteUserButton.setDisable(!admin);
        usersTab.setDisable(!admin);
        auditTab.setDisable(!admin);

        idColumn.setCellValueFactory(data -> data.getValue().idProperty());
        nameColumn.setCellValueFactory(data -> data.getValue().nameProperty());
        categoryColumn.setCellValueFactory(data -> data.getValue().categoryProperty());
        quantityColumn.setCellValueFactory(data -> data.getValue().quantityProperty());
        priceColumn.setCellValueFactory(data -> data.getValue().priceProperty());
        supplierColumn.setCellValueFactory(data -> data.getValue().supplierProperty());
        statusColumn.setCellValueFactory(data -> data.getValue().statusProperty());
        productTable.setRowFactory(table -> new TableRow<>() {
            @Override
            protected void updateItem(Product product, boolean empty) {
                super.updateItem(product, empty);
                getStyleClass().removeAll("low-stock-row", "out-stock-row");
                if (!empty && product != null) {
                    if ("OUT_OF_STOCK".equals(product.getStatus())) {
                        getStyleClass().add("out-stock-row");
                    } else if ("LOW_STOCK".equals(product.getStatus())) {
                        getStyleClass().add("low-stock-row");
                    }
                }
            }
        });
        userIdColumn.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().id()));
        usernameColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().username()));
        userRoleColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().role().name()));
        auditTimeColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().createdAt().format(auditFormatter)));
        auditUserColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().username()));
        auditActionColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().action()));
        auditEntityColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().entity()));
        auditDetailsColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().details()));
        supplierIdColumn.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().id()));
        supplierNameColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().name()));
        supplierEmailColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().email()));
        supplierPhoneColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().phone()));
        supplierAddressColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().address()));
        poIdColumn.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().id()));
        poSupplierColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().supplier()));
        poProductColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().productName()));
        poQuantityColumn.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().quantity()));
        poStatusColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().status()));
        poCreatedByColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().createdBy()));
        poCreatedAtColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().createdAt().format(auditFormatter)));
        historyTimeColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().createdAt().format(auditFormatter)));
        historyProductColumn.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().productId()));
        historyOldColumn.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().oldQuantity()));
        historyNewColumn.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().newQuantity()));
        historyTypeColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().changeType()));
        historyByColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().changedBy()));
        historyReasonColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().reason()));
        notificationTimeColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().createdAt().format(auditFormatter)));
        notificationTypeColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().type()));
        notificationMessageColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().message()));
        notificationEmailColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(String.valueOf(data.getValue().emailSent())));

        searchDebounce.setOnFinished(event -> applySmartSearch());
        searchField.textProperty().addListener((obs, oldValue, newValue) -> searchDebounce.playFromStart());
        advancedNameField.textProperty().addListener((obs, oldValue, newValue) -> searchDebounce.playFromStart());
        advancedSupplierField.textProperty().addListener((obs, oldValue, newValue) -> searchDebounce.playFromStart());
        minPriceField.textProperty().addListener((obs, oldValue, newValue) -> searchDebounce.playFromStart());
        maxPriceField.textProperty().addListener((obs, oldValue, newValue) -> searchDebounce.playFromStart());
        minStockField.textProperty().addListener((obs, oldValue, newValue) -> searchDebounce.playFromStart());
        maxStockField.textProperty().addListener((obs, oldValue, newValue) -> searchDebounce.playFromStart());
        categoryFilter.valueProperty().addListener((obs, oldValue, newValue) -> searchDebounce.playFromStart());
        productTable.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, product) -> {
            loadStockHistory(product);
        });
        Platform.runLater(() -> {
            rootPane.getScene().getAccelerators().put(new KeyCodeCombination(KeyCode.F1), this::showHelp);
            rootPane.getScene().addEventFilter(javafx.scene.input.InputEvent.ANY, event -> sessionTimeout.playFromStart());
            applyTheme(preferences.getBoolean("darkMode", false));
            sessionTimeout.setOnFinished(event -> logout());
            sessionTimeout.playFromStart();
        });
        startClock();
        refresh();
    }

    @FXML
    private void refresh() {
        String databaseStatus = Database.isAvailable() ? LanguageService.get("status.connected") : LanguageService.get("status.disconnected");
        dbStatusLabel.setText(databaseStatus);
        footerDbStatusLabel.setText(databaseStatus);
        loadProducts();
        loadCategories();
        loadReports();
        loadUsers();
        loadAuditLogs();
        loadSuppliers();
        loadPurchaseOrders();
        loadNotifications();
    }

    @FXML
    private void clearFilter() {
        searchField.clear();
        advancedNameField.clear();
        advancedSupplierField.clear();
        minPriceField.clear();
        maxPriceField.clear();
        minStockField.clear();
        maxStockField.clear();
        categoryFilter.getSelectionModel().clearSelection();
    }

    @FXML
    private void addProduct() {
        if (!ensureAdmin()) {
            return;
        }
        openProductDialog(new Product());
    }

    @FXML
    private void editProduct() {
        if (!ensureAdmin()) {
            return;
        }
        Product selected = productTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertService.info(LanguageService.get("product.select"));
            return;
        }
        openProductDialog(new Product(selected.getId(), selected.getName(), selected.getCategory(), selected.getQuantity(), selected.getPrice(),
                selected.getSupplierId(), selected.getSupplier(), selected.getStatus()));
    }

    @FXML
    private void deleteProduct() {
        if (!ensureAdmin()) {
            return;
        }
        Product selected = productTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertService.info(LanguageService.get("product.select"));
            return;
        }
        try {
            productRepository.delete(selected.getId());
            auditLogRepository.record("DELETE", "PRODUCT", selected.getName());
            refresh();
        } catch (Exception ex) {
            AlertService.error(LanguageService.get("error.database"));
        }
    }

    @FXML
    private void showQrCode() {
        Product selected = productTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertService.info(LanguageService.get("product.select"));
            return;
        }
        ImageView imageView = new ImageView(QrCodeService.forProduct(selected));
        TextArea qrText = new TextArea(QrCodeService.payload(selected));
        qrText.setEditable(false);
        qrText.setWrapText(true);
        qrText.setPrefRowCount(8);
        VBox content = new VBox(12, imageView, qrText);
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(LanguageService.get("qr.title"));
        alert.setHeaderText(selected.getName());
        alert.getDialogPane().setContent(content);
        alert.showAndWait();
    }

    @FXML
    private void showHelp() {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle(LanguageService.get("menu.help.open"));
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        TextArea helpText = new TextArea(LanguageService.get("help.text"));
        helpText.setEditable(false);
        helpText.setWrapText(true);
        helpText.setPrefColumnCount(72);
        helpText.setPrefRowCount(18);
        dialog.getDialogPane().setContent(helpText);
        dialog.showAndWait();
    }

    @FXML
    private void showProductsTab() {
        selectTab(productsTab);
    }

    @FXML
    private void showReportsTab() {
        selectTab(reportsTab);
    }

    @FXML
    private void showSettingsTab() {
        selectTab(settingsTab);
    }

    @FXML
    private void showUsersTab() {
        if (ensureAdmin()) {
            selectTab(usersTab);
        }
    }

    @FXML
    private void showAuditTab() {
        if (ensureAdmin()) {
            selectTab(auditTab);
        }
    }

    @FXML
    private void showSuppliersTab() {
        selectTab(suppliersTab);
    }

    @FXML
    private void showPurchaseOrdersTab() {
        selectTab(purchaseOrdersTab);
    }

    @FXML
    private void showStockHistoryTab() {
        selectTab(stockHistoryTab);
    }

    @FXML
    private void showNotificationsTab() {
        selectTab(notificationsTab);
    }

    @FXML
    private void showSelectedProductHistory() {
        Product selected = productTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertService.info(LanguageService.get("product.select"));
            return;
        }
        loadStockHistory(selected);
        selectTab(stockHistoryTab);
    }

    @FXML
    private void advancedSearch() {
        applySmartSearch();
    }

    private void applySmartSearch() {
        try {
            String productName = advancedNameField.getText().isBlank() ? searchField.getText() : advancedNameField.getText();
            productTable.setItems(FXCollections.observableArrayList(productRepository.advancedSearch(
                    productName,
                    categoryFilter.getValue(),
                    advancedSupplierField.getText(),
                    decimalOrNull(minPriceField.getText()),
                    decimalOrNull(maxPriceField.getText()),
                    integerOrNull(minStockField.getText()),
                    integerOrNull(maxStockField.getText())
            )));
        } catch (NumberFormatException ex) {
            // Ignore partial numeric input while the user is typing.
        } catch (Exception ex) {
            AlertService.error(LanguageService.get("error.database"));
        }
    }

    @FXML
    private void addSupplier() {
        if (ensureAdmin()) {
            openSupplierDialog(new Supplier(0, "", "", "", ""));
        }
    }

    @FXML
    private void editSupplier() {
        if (!ensureAdmin()) {
            return;
        }
        Supplier selected = supplierTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertService.info(LanguageService.get("supplier.select"));
            return;
        }
        openSupplierDialog(selected);
    }

    @FXML
    private void deleteSupplier() {
        if (!ensureAdmin()) {
            return;
        }
        Supplier selected = supplierTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertService.info(LanguageService.get("supplier.select"));
            return;
        }
        try {
            supplierRepository.delete(selected.id());
            auditLogRepository.record("DELETE", "SUPPLIER", selected.name());
            refresh();
        } catch (Exception ex) {
            AlertService.error(LanguageService.get("error.database"));
        }
    }

    @FXML
    private void createReorder() {
        if (!ensureAdmin()) {
            return;
        }
        Product selected = productTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertService.info(LanguageService.get("product.select"));
            return;
        }
        if (selected.getQuantity() > 5) {
            AlertService.info(LanguageService.get("po.lowStockOnly"));
            return;
        }
        try {
            int orderId = purchaseOrderRepository.createForProduct(selected, Math.max(10, 20 - selected.getQuantity()));
            auditLogRepository.record("CREATE", "PURCHASE_ORDER", "PO #" + orderId + " for " + selected.getName());
            refresh();
            selectTab(purchaseOrdersTab);
        } catch (Exception ex) {
            AlertService.error(LanguageService.get("error.database"));
        }
    }

    @FXML
    private void createReordersForLowStock() {
        if (!ensureAdmin()) {
            return;
        }
        try {
            int created = 0;
            for (Product product : productRepository.advancedSearch("", null, "", null, null, null, 5)) {
                purchaseOrderRepository.createForProduct(product, Math.max(10, 20 - product.getQuantity()));
                created++;
            }
            auditLogRepository.record("CREATE", "PURCHASE_ORDER", created + " low-stock reorder(s)");
            refresh();
            selectTab(purchaseOrdersTab);
            AlertService.info(LanguageService.get("po.bulkCreated") + " " + created);
        } catch (Exception ex) {
            AlertService.error(LanguageService.get("error.database"));
        }
    }

    @FXML
    private void approvePurchaseOrder() {
        updatePurchaseOrderStatus("APPROVED");
    }

    @FXML
    private void receivePurchaseOrder() {
        if (!ensureAdmin()) {
            return;
        }
        PurchaseOrder selected = purchaseOrderTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertService.info(LanguageService.get("po.select"));
            return;
        }
        try {
            String result = purchaseOrderRepository.receive(selected.id(), SessionService.requireUser().username());
            auditLogRepository.record("RECEIVE", "PURCHASE_ORDER", "PO #" + selected.id() + " / " + result);
            refresh();
            AlertService.info(LanguageService.get("po.received") + " " + result);
        } catch (Exception ex) {
            AlertService.error(LanguageService.get("error.database"));
        }
    }

    @FXML
    private void adjustStock() {
        if (!ensureAdmin()) {
            return;
        }
        Product selected = productTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertService.info(LanguageService.get("product.select"));
            return;
        }
        Dialog<StockAdjustment> dialog = new Dialog<>();
        dialog.setTitle(LanguageService.get("stock.adjust"));
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        TextField quantity = new TextField(String.valueOf(selected.getQuantity()));
        TextField reason = new TextField();
        ComboBox<String> type = new ComboBox<>(FXCollections.observableArrayList("RESTOCK", "SALE", "UPDATE"));
        type.setValue("UPDATE");
        GridPane grid = new GridPane();
        grid.getStyleClass().add("form-grid");
        grid.addRow(0, new Label(LanguageService.get("product.quantity")), quantity);
        grid.addRow(1, new Label(LanguageService.get("history.type")), type);
        grid.addRow(2, new Label(LanguageService.get("history.reason")), reason);
        dialog.getDialogPane().setContent(grid);
        dialog.setResultConverter(button -> {
            if (button != ButtonType.OK) {
                return null;
            }
            try {
                int newQuantity = Integer.parseInt(quantity.getText().trim());
                if (newQuantity < 0 || reason.getText().isBlank()) {
                    throw new NumberFormatException();
                }
                return new StockAdjustment(newQuantity, type.getValue(), reason.getText().trim());
            } catch (NumberFormatException ex) {
                AlertService.error(LanguageService.get("validation.numeric"));
                return null;
            }
        });
        dialog.showAndWait().ifPresent(adjustment -> saveStockAdjustment(selected, adjustment));
    }

    @FXML
    private void exportPdf() {
        try {
            AlertService.info(LanguageService.get("export.created") + " " + exportService.exportTextPdf());
        } catch (Exception ex) {
            AlertService.error(LanguageService.get("export.failed"));
        }
    }

    @FXML
    private void exportExcel() {
        try {
            AlertService.info(LanguageService.get("export.created") + " " + exportService.exportExcel());
        } catch (Exception ex) {
            AlertService.error(LanguageService.get("export.failed"));
        }
    }

    @FXML
    private void toggleTheme() {
        boolean darkMode = !preferences.getBoolean("darkMode", false);
        preferences.putBoolean("darkMode", darkMode);
        applyTheme(darkMode);
    }