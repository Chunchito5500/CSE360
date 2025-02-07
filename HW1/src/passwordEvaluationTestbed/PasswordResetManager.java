package passwordEvaluationTestbed;

import databasePart1.DatabaseHelper;
import java.sql.SQLException;
import java.util.UUID;

/**
 * Handles password reset requests by generating and validating one-time passwords.
 */
public class PasswordResetManager {

    private final DatabaseHelper databaseHelper;

    public PasswordResetManager(DatabaseHelper databaseHelper) {
        this.databaseHelper = databaseHelper;
    }

    /**
     * Generates a one-time password (OTP) and stores it in the database.
     */
    public String generateOneTimePassword(String username) throws SQLException {
        return databaseHelper.generateOneTimePassword(username);
    }

    /**
     * Checks if the given password is a one-time password.
     */
    public boolean isOneTimePassword(String username, String password) throws SQLException {
        return databaseHelper.isTemporaryPassword(username, password);
    }

    /**
     * Invalidates a one-time password after it is used.
     */
    public void invalidateOneTimePassword(String username) throws SQLException {
        databaseHelper.clearTemporaryPassword(username);
    }
}