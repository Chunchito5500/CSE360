package application;

import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.SQLException;

import databasePart1.*;
import userNameRecognizerTestbed.UserNameRecognizer;
import passwordEvaluationTestbed.PasswordEvaluator;

/**
 * The SetupAdmin class handles the setup process for creating an administrator account.
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
                System.out.println("Administrator setup completed successfully.");

                // Navigate to the Welcome Login Page
                new WelcomeLoginPage(databaseHelper).show(primaryStage, user);
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