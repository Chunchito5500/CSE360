package databasePart1;

import java.sql.*;
import java.util.UUID;
import java.time.LocalDateTime;
import application.User;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * The DatabaseHelper class is responsible for managing the connection to the database,
 * performing operations such as user registration, login validation, and handling invitation codes.
 */
public class DatabaseHelper {

    // JDBC driver name and database URL
    private static final String JDBC_DRIVER = "org.h2.Driver";
    private static final String DB_URL = "jdbc:h2:~/FoundationDatabase";

    // Database credentials
    private static final String USER = "sa";
    private static final String PASS = "";

    private Connection connection = null;

    /**
     * Connects to the database and initializes tables.
     */
    public void connectToDatabase() throws SQLException {
        try {
            // Load the JDBC driver (optional for H2, but good for clarity)
            Class.forName(JDBC_DRIVER);
        } catch (ClassNotFoundException e) {
            System.err.println("Error loading JDBC driver");
            e.printStackTrace();
        }

        connection = DriverManager.getConnection(DB_URL, USER, PASS);
        System.out.println("Connected to database...");

        // Create necessary tables
        createTables();
    }

    /**
     * Creates the necessary tables if they do not exist.
     */
    private void createTables() throws SQLException {
        // Create cse360users table
        String userTable = "CREATE TABLE IF NOT EXISTS cse360users ("
                + "id INT AUTO_INCREMENT PRIMARY KEY, "
                + "userName VARCHAR(255) UNIQUE, "
                + "password VARCHAR(255), "
                + "isTemporary BOOLEAN DEFAULT FALSE, "
                + "role VARCHAR(100))";  // Increased to 100 characters for multiple roles
        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate(userTable);
        }

        // Create InvitationCodes table
        String invitationCodesTable = "CREATE TABLE IF NOT EXISTS InvitationCodes ("
                + "code VARCHAR(50) PRIMARY KEY, "
                + "role VARCHAR(20), "
                + "expiration TIMESTAMP, "
                + "isUsed BOOLEAN DEFAULT FALSE)";
        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate(invitationCodesTable);
        }
    }

    /**
     * Checks if the database is empty.
     */
    public boolean isDatabaseEmpty() throws SQLException {
        String query = "SELECT COUNT(*) AS count FROM cse360users";
        try (Statement stmt = connection.createStatement();
             ResultSet resultSet = stmt.executeQuery(query)) {
            return resultSet.next() && resultSet.getInt("count") == 0;
        }
    }

    /**
     * Checks if a user exists in the database.
     */
    public boolean doesUserExist(String username) throws SQLException {
        String query = "SELECT COUNT(*) FROM cse360users WHERE userName = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    /**
     * Retrieves the role of a user.
     */
    public String getUserRole(String userName) {
        String query = "SELECT role FROM cse360users WHERE userName = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, userName);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String role = rs.getString("role");
                    System.out.println("Database returned role: " + role); // Debugging line
                    return role;
                } else {
                    System.err.println("ERROR: No role found for user " + userName);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null; // If no user exists or an error occurs
    }

    /**
     * Registers a new user.
     */
    public void register(User user) throws SQLException {
        String insertUser = "INSERT INTO cse360users (userName, password, role) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(insertUser)) {
            stmt.setString(1, user.getUserName());
            stmt.setString(2, user.getPassword());
            stmt.setString(3, user.getRole());
            stmt.executeUpdate();
        }
    }

    /**
     * Validates user login.
     */
    public boolean login(User user) throws SQLException {
        String query = "SELECT * FROM cse360users WHERE userName = ? AND password = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, user.getUserName());
            stmt.setString(2, user.getPassword());
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    /**
     * Generates a one-time password for a user.
     */
    public String generateOneTimePassword(String username) throws SQLException {
        String otp = UUID.randomUUID().toString().substring(0, 6);
        String query = "UPDATE cse360users SET password = ?, isTemporary = TRUE WHERE userName = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, otp);
            pstmt.setString(2, username);
            pstmt.executeUpdate();
        }
        return otp;
    }

    /**
     * Updates user password.
     */
    public void updateUserPassword(String username, String newPassword, boolean isTemporary) throws SQLException {
        String query = "UPDATE cse360users SET password = ?, isTemporary = ? WHERE userName = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, newPassword);
            pstmt.setBoolean(2, isTemporary);
            pstmt.setString(3, username);
            pstmt.executeUpdate();
        }
    }

    /**
     * Checks if a password is a one-time password.
     */
    public boolean isTemporaryPassword(String username, String password) throws SQLException {
        String query = "SELECT isTemporary FROM cse360users WHERE userName = ? AND password = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, username);
            stmt.setString(2, password);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() && rs.getBoolean("isTemporary");
            }
        }
    }

    /**
     * Clears a one-time password.
     */
    public void clearTemporaryPassword(String username) throws SQLException {
        String query = "UPDATE cse360users SET isTemporary = FALSE WHERE userName = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, username);
            pstmt.executeUpdate();
        }
    }

    /**
     * Retrieves the user roles as a list (splitting a comma-separated string).
     */
    public ArrayList<String> getUserRoles(String userName) throws SQLException {
        ArrayList<String> roles = new ArrayList<>();
        String query = "SELECT role FROM cse360users WHERE userName = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, userName);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String roleField = rs.getString("role");
                    if (roleField != null && !roleField.trim().isEmpty()) {
                        String[] splitRoles = roleField.split(",");
                        for (String r : splitRoles) {
                            roles.add(r.trim());
                        }
                    }
                }
            }
        }
        return roles;
    }

    /**
     * Updates the user's role field to contain only the selected role.
     * (Used when the user chooses a role during login.)
     */
    public void switchRole(String userName, String role) throws SQLException {
        String query = "UPDATE cse360users SET role = ? WHERE userName = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, role);
            pstmt.setString(2, userName);
            pstmt.executeUpdate();
        }
    }

    /**
     * Sets (or adds) a role for a user.
     * If 'add' is true, the method appends the role to the existing list (if not already present);
     * otherwise, it replaces any existing roles with the new role.
     */
    public void setUserRole(String userName, String role, boolean add) throws SQLException {
        ArrayList<String> roles = getUserRoles(userName);
        if (add) {
            if (!roles.contains(role)) {
                roles.add(role);
            }
        } else {
            roles.clear();
            roles.add(role);
        }
        String rolesString = String.join(",", roles);
        String query = "UPDATE cse360users SET role = ? WHERE userName = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, rolesString);
            pstmt.setString(2, userName);
            pstmt.executeUpdate();
        }
    }

    /**
     * Generates a new invitation code.
     */
    public String generateInvitationCode(String role, int validHours) {
        String code = UUID.randomUUID().toString().substring(0, 6);
        String insertCode = "INSERT INTO InvitationCodes (code, role, expiration) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(insertCode)) {
            pstmt.setString(1, code);
            pstmt.setString(2, role);
            pstmt.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now().plusHours(validHours)));
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return code;
    }

    /**
     * Validates an invitation code.
     */
    public boolean validateInvitationCode(String code) {
        String query = "SELECT role FROM InvitationCodes WHERE code = ? AND isUsed = FALSE AND expiration > CURRENT_TIMESTAMP";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, code);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    markInvitationCodeAsUsed(code);
                    return true;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Marks an invitation code as used.
     */
    private void markInvitationCodeAsUsed(String code) {
        String query = "UPDATE InvitationCodes SET isUsed = TRUE WHERE code = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, code);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Closes the database connection.
     */
    public void closeConnection() {
        try {
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Retrieves all users from the cse360users table.
     * (This query selects only userName and role since fullName and email are not part of the schema.)
     */
    public ArrayList<User> getAllUsers() throws SQLException {
        ArrayList<User> users = new ArrayList<>();
        String query = "SELECT userName, role FROM cse360users";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                String userName = rs.getString("userName");
                String roles = rs.getString("role");
                // Create a User object (assuming the User constructor accepts userName, password, role).
                users.add(new User(userName, "", roles));
            }
        }
        return users;
    }

    /**
     * Deletes a user account.
     * An account is not deleted if its role contains "admin".
     */
    public boolean deleteUser(String userName) throws SQLException {
        String role = getUserRole(userName);
        if (role != null && role.toLowerCase().contains("admin")) {
            // Prevent deletion if the user is an admin.
            return false;
        }
        String query = "DELETE FROM cse360users WHERE userName = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, userName);
            return pstmt.executeUpdate() > 0;
        }
    }

    /**
     * Removes a specific role from a user's role list.
     * Returns false if attempting to remove "admin".
     */
    public boolean removeRole(String userName, String roleToRemove) throws SQLException {
        ArrayList<String> roles = getUserRoles(userName);
        if (roleToRemove.equalsIgnoreCase("admin")) {
            return false;
        }
        if (roles.contains(roleToRemove)) {
            roles.remove(roleToRemove);
            String updatedRoles = String.join(",", roles);
            String query = "UPDATE cse360users SET role = ? WHERE userName = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(query)) {
                pstmt.setString(1, updatedRoles);
                pstmt.setString(2, userName);
                return pstmt.executeUpdate() > 0;
            }
        }
        return false;
    }

    /**
     * Updates a user's roles with the provided comma-separated roles string.
     */
    public void updateUserRoles(String userName, String roles) throws SQLException {
        String query = "UPDATE cse360users SET role = ? WHERE userName = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, roles);
            pstmt.setString(2, userName);
            pstmt.executeUpdate();
        }
    }
}