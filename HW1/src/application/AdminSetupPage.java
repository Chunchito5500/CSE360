package application;

import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.sql.SQLException;
import databasePart1.DatabaseHelper;
import userNameRecognizerTestbed.UserNameRecognizer;
import passwordEvaluationTestbed.PasswordEvaluator;
import application.User;
import application.UserLoginPage;

/**
 * The AdminSetupPage class handles the setup process for creating an administrator account.
 * This is intended to be used by the first user to initialize the system with admin credentials.
 */
public class AdminSetupPage {

    private final DatabaseHelper databaseHelper;

    public AdminSetupPage(DatabaseHelper databaseHelper) {
        this.databaseHelper = databaseHelper;
    }

    public void show(Stage primaryStage) {
        // Input fields for userName and password
        TextField userNameField = new TextField();
        userNameField.setPromptText("Enter Admin UserName");
        userNameField.setMaxWidth(250);

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Enter Password");
        passwordField.setMaxWidth(250);

        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: red;");

        Button setupButton = new Button("Setup");

        setupButton.setOnAction(a -> {
            // Retrieve user input
            String userName = userNameField.getText();
            String password = passwordField.getText();

            // Collect errors
            StringBuilder errors = new StringBuilder();

            // Validate UserName
            String userNameError = UserNameRecognizer.checkForValidUserName(userName);
            if (!userNameError.isEmpty()) {
                errors.append("UserName Error: ").append(userNameError).append("\n");
                System.err.println("UserName Error: " + userNameError);
            } else {
                System.out.println("UserName is valid.");
            }

            // Validate Password
            String passwordError = PasswordEvaluator.evaluatePassword(password);
            if (!passwordError.isEmpty()) {
                errors.append("Password Error:\n").append(passwordError).append("\n");
                System.err.println("Password Error:\n" + passwordError);
            } else {
                System.out.println("Password is valid.");
            }

            // If errors exist, display them in the GUI and return
            if (errors.length() > 0) {
                errorLabel.setText(errors.toString().trim());
                return;
            }

            try {
                // Create a new User object with admin role and register in the database
                User user = new User(userName, password, "admin");
                databaseHelper.register(user);
                System.out.println("Administrator registered successfully.");

                // Set all roles for the admin (for example purposes, the admin gets all roles)
                databaseHelper.setUserRole(userName, "admin", true);
                databaseHelper.setUserRole(userName, "student", true);
                databaseHelper.setUserRole(userName, "instructor", true);
                databaseHelper.setUserRole(userName, "staff", true);
                databaseHelper.setUserRole(userName, "reviewer", true);

                System.out.println("Administrator setup completed. Please log in with your new admin credentials.");

                // Navigate to the login page
                new UserLoginPage(databaseHelper).show(primaryStage);
            } catch (SQLException e) {
                String dbErrorMessage = "Database error: " + e.getMessage();
                System.err.println(dbErrorMessage);
                e.printStackTrace();
                errorLabel.setText(dbErrorMessage);
            }
        });

        VBox layout = new VBox(10, userNameField, passwordField, setupButton, errorLabel);
        layout.setStyle("-fx-padding: 20; -fx-alignment: center;");

        primaryStage.setScene(new Scene(layout, 800, 400));
        primaryStage.setTitle("Administrator Setup");
        primaryStage.show();
    }
}