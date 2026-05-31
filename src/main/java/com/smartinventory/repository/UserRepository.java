package com.smartinventory.repository;

import com.smartinventory.config.Database;
import com.smartinventory.model.Role;
import com.smartinventory.model.User;
import com.smartinventory.service.PasswordService;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserRepository {
    public Optional<User> authenticate(String username, String password) throws SQLException {
        String sql = "SELECT id, username, password_hash, role, failed_attempts, locked_until FROM users WHERE username = ?";
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);
            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) {
                    return Optional.empty();
                }
                Timestamp lockedUntil = result.getTimestamp("locked_until");
                if (lockedUntil != null && lockedUntil.toLocalDateTime().isAfter(LocalDateTime.now())) {
                    return Optional.empty();
                }
                int userId = result.getInt("id");
                if (PasswordService.verify(password, result.getString("password_hash"))) {
                    resetFailures(userId);
                    return Optional.of(new User(userId, result.getString("username"), Role.valueOf(result.getString("role"))));
                }
                registerFailure(userId, result.getInt("failed_attempts") + 1);
            }
        }
        return Optional.empty();
    }

    private void registerFailure(int userId, int attempts) throws SQLException {
        String sql = attempts >= 5
                ? "UPDATE users SET failed_attempts = ?, locked_until = DATE_ADD(NOW(), INTERVAL 10 MINUTE) WHERE id = ?"
                : "UPDATE users SET failed_attempts = ?, locked_until = NULL WHERE id = ?";
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, attempts);
            statement.setInt(2, userId);
            statement.executeUpdate();
        }
    }

    private void resetFailures(int userId) throws SQLException {
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement("UPDATE users SET failed_attempts = 0, locked_until = NULL WHERE id = ?")) {
            statement.setInt(1, userId);
            statement.executeUpdate();
        }
    }

    public List<User> findAll() throws SQLException {
        List<User> users = new ArrayList<>();
        String sql = "SELECT id, username, role FROM users ORDER BY username";
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {
            while (result.next()) {
                users.add(new User(result.getInt("id"), result.getString("username"), Role.valueOf(result.getString("role"))));
            }
        }
        return users;
    }

    public User create(String username, String password, Role role) throws SQLException {
        String sql = "INSERT INTO users (username, password_hash, role) VALUES (?, ?, ?)";
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, username);
            statement.setString(2, PasswordService.hash(password));
            statement.setString(3, role.name());
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    return new User(keys.getInt(1), username, role);
                }
            }
        }
        throw new SQLException("User was created but no id was returned");
    }

    public void update(User user, String newPassword) throws SQLException {
        boolean changePassword = newPassword != null && !newPassword.isBlank();
        String sql = changePassword
                ? "UPDATE users SET username = ?, role = ?, password_hash = ? WHERE id = ?"
                : "UPDATE users SET username = ?, role = ? WHERE id = ?";
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, user.username());
            statement.setString(2, user.role().name());
            if (changePassword) {
                statement.setString(3, PasswordService.hash(newPassword));
                statement.setInt(4, user.id());
            } else {
                statement.setInt(3, user.id());
            }
            statement.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM users WHERE id = ?";
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            statement.executeUpdate();
        }
    }

    public boolean existsByUsername(String username, int exceptId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM users WHERE LOWER(username) = LOWER(?) AND id <> ?";
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);
            statement.setInt(2, exceptId);
            try (ResultSet result = statement.executeQuery()) {
                return result.next() && result.getInt(1) > 0;
            }
        }
    }
}
