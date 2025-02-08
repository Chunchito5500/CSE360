package databasePart1;

import java.sql.*;
import java.util.*;
import java.util.UUID;
import java.time.LocalDateTime;
import application.User;

public class DatabaseHelper {

	// JDBC driver name and database URL 
	static final String JDBC_DRIVER = "org.h2.Driver";   
	static final String DB_URL = "jdbc:h2:~/FoundationDatabase";  

	// Database credentials 
	static final String USER = "sa"; 
	static final String PASS = ""; 

	private Connection connection = null;
	private Statement statement = null; 

	// --- Methods from the first code block ---

	public void connectToDatabase() throws SQLException {
		try {
			Class.forName(JDBC_DRIVER); // Load the JDBC driver
			System.out.println("Connecting to database...");
			connection = DriverManager.getConnection(DB_URL, USER, PASS);
			statement = connection.createStatement();
			createTables();  // Create the necessary tables if they don't exist
		} catch (ClassNotFoundException e) {
			System.err.println("JDBC Driver not found: " + e.getMessage());
		}
	}

	private void createTables() throws SQLException {
		// Merged table creation to support extra functionalities from the second code block
		String userTable = "CREATE TABLE IF NOT EXISTS cse360users ("
				+ "id INT AUTO_INCREMENT PRIMARY KEY, "
				+ "userName VARCHAR(255) UNIQUE, "
				+ "password VARCHAR(255), "
                + "isTemporary BOOLEAN DEFAULT FALSE, "
				+ "role VARCHAR(20), "
				+ "name VARCHAR(255), "
			    + "email VARCHAR(255))";
		statement.execute(userTable);
		
		String invitationCodesTable = "CREATE TABLE IF NOT EXISTS InvitationCodes ("
				+ "code VARCHAR(10) PRIMARY KEY, "
                + "role VARCHAR(20), "
                + "expiration TIMESTAMP, "
				+ "isUsed BOOLEAN DEFAULT FALSE)";
		statement.execute(invitationCodesTable);
	}

	public boolean isDatabaseEmpty() throws SQLException {
		String query = "SELECT COUNT(*) AS count FROM cse360users";
		ResultSet resultSet = statement.executeQuery(query);
		if (resultSet.next()) {
			return resultSet.getInt("count") == 0;
		}
		return true;
	}

	public void register(User user) throws SQLException {
		String insertUser = "INSERT INTO cse360users (userName, password, role, name, email) VALUES (?, ?, ?, ?, ?)";
		try (PreparedStatement pstmt = connection.prepareStatement(insertUser)) {
			pstmt.setString(1, user.getUserName());
			pstmt.setString(2, user.getPassword());
			pstmt.setString(3, user.getRole());
			pstmt.setString(4, user.getName());
			pstmt.setString(5, user.getEmail());
			pstmt.executeUpdate();
		}
	}

	public boolean login(User user) throws SQLException {
		String query = "SELECT * FROM cse360users WHERE userName = ? AND password = ? AND role = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, user.getUserName());
			pstmt.setString(2, user.getPassword());
			pstmt.setString(3, user.getRole());
			try (ResultSet rs = pstmt.executeQuery()) {
				return rs.next();
			}
		}
	}
	
	public boolean doesUserExist(String userName) {
	    String query = "SELECT COUNT(*) FROM cse360users WHERE userName = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, userName);
	        ResultSet rs = pstmt.executeQuery();
	        if (rs.next()) {
	            return rs.getInt(1) > 0;
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    return false;
	}
	
	public int getAdminCount() throws SQLException {
	    String query = "SELECT COUNT(*) AS adminCount FROM cse360users WHERE role = 'admin'";
	    try (Statement stmt = connection.createStatement();
	         ResultSet rs = stmt.executeQuery(query)) {
	        if (rs.next()) {
	            return rs.getInt("adminCount");
	        }
	    }
	    return 0; 
	}

	public List<User> getAllUsers() throws SQLException {
	    List<User> users = new ArrayList<>();
	    String sql = "SELECT userName, password, role, name, email FROM cse360users";
	    try (PreparedStatement pstmt = connection.prepareStatement(sql);
	         ResultSet rs = pstmt.executeQuery()) {
	        while (rs.next()) {
	            String username = rs.getString("userName");
	            String password = rs.getString("password");
	            String role     = rs.getString("role");
	            String name     = rs.getString("name"); 
	            String email    = rs.getString("email");
	            User user = new User(username, password, role, name, email);
	            users.add(user);
	        }
	    }
	    return users;
	}
	
	public String getUserRole(String userName) {
	    String query = "SELECT role FROM cse360users WHERE userName = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, userName);
	        ResultSet rs = pstmt.executeQuery();
	        if (rs.next()) {
	            return rs.getString("role");
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    return null;
	}
	
	public void updateUserRole(String username, String newRole) throws SQLException {
	    String sql = "UPDATE cse360users SET role = ? WHERE userName = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
	        pstmt.setString(1, newRole);
	        pstmt.setString(2, username);
	        pstmt.executeUpdate();
	    }
	}
	
	public String generateInvitationCode() {
	    String code = UUID.randomUUID().toString().substring(0, 4);
	    String query = "INSERT INTO InvitationCodes (code) VALUES (?)";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, code);
	        pstmt.executeUpdate();
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    return code;
	}
	
	public boolean validateInvitationCode(String code) {
	    String query = "SELECT * FROM InvitationCodes WHERE code = ? AND isUsed = FALSE";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, code);
	        ResultSet rs = pstmt.executeQuery();
	        if (rs.next()) {
	            markInvitationCodeAsUsed(code);
	            return true;
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    return false;
	}
	
	private void markInvitationCodeAsUsed(String code) {
	    String query = "UPDATE InvitationCodes SET isUsed = TRUE WHERE code = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, code);
	        pstmt.executeUpdate();
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	}
	
	public void deleteUser(String userName) throws SQLException {
	    String sql = "DELETE FROM cse360users WHERE userName = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
	        pstmt.setString(1, userName);
	        pstmt.executeUpdate();
	    }
	}
	
	public void closeConnection() {
		try { 
			if(statement != null) statement.close(); 
		} catch(SQLException se2) { 
			se2.printStackTrace();
		} 
		try { 
			if(connection != null) connection.close(); 
		} catch(SQLException se){ 
			se.printStackTrace(); 
		} 
	}
	
	// --- Additional methods from the second code block ---
	
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
	
	public void updateUserPassword(String username, String newPassword, boolean isTemporary) throws SQLException {
	    String query = "UPDATE cse360users SET password = ?, isTemporary = ? WHERE userName = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, newPassword);
	        pstmt.setBoolean(2, isTemporary);
	        pstmt.setString(3, username);
	        pstmt.executeUpdate();
	    }
	}
	
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
	
	public void clearTemporaryPassword(String username) throws SQLException {
	    String query = "UPDATE cse360users SET isTemporary = FALSE WHERE userName = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, username);
	        pstmt.executeUpdate();
	    }
	}
	
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
	
	public void switchRole(String userName, String role) throws SQLException {
	    String query = "UPDATE cse360users SET role = ? WHERE userName = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, role);
	        pstmt.setString(2, userName);
	        pstmt.executeUpdate();
	    }
	}
	
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
	
	public void updateUserRoles(String userName, String roles) throws SQLException {
	    String query = "UPDATE cse360users SET role = ? WHERE userName = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, roles);
	        pstmt.setString(2, userName);
	        pstmt.executeUpdate();
	    }
	}
	
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
}
