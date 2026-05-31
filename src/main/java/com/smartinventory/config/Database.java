package com.smartinventory.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public final class Database {
    private static final Properties CONFIG = AppConfig.load("database.properties");
    private static final String HOST = value("db.host", "localhost");
    private static final String PORT = value("db.port", "3307");
    private static final String NAME = value("db.name", "smart_inventory");
    private static final String URL = System.getProperty("db.url", "jdbc:mysql://" + HOST + ":" + PORT + "/" + NAME + "?useSSL=false&serverTimezone=UTC");
    private static final String USER = value("db.user", "root");
    private static final String PASSWORD = value("db.password", "");

    private Database() {
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    public static boolean isAvailable() {
        try (Connection ignored = getConnection()) {
            return true;
        } catch (SQLException ex) {
            return false;
        }
    }

    private static String value(String key, String fallback) {
        return System.getProperty(key, CONFIG.getProperty(key, fallback));
    }
}
