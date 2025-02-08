package application;

import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.sql.SQLException;
import databasePart1.DatabaseHelper;
import application.User;
import application.UserLoginPage;

/**
 * The AdminSetupPage class handles the setup process for creating an administrator account.
 * This is intended to be used by the first user to initialize the system with admin credentials.
 */
public class AdminSetupPage {

    private final DatabaseHelper databaseHelper;
    Label userNameErrorLabel = new Label();
    Label passwordErrorLabel = new Label();

    public AdminSetupPage(DatabaseHelper databaseHelper) {
        this.databaseHelper = databaseHelper;
        userNameErrorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 12px;");
        passwordErrorLabel.setStyle("-fx-text-fill: red; -fx-fon	t-size: 12px;");
    }

    public void show(Stage primaryStage) {
        // Input fields for userName and password
        TextField userNameField = new TextField();
        userNameField.setPromptText("Enter Admin UserName");
        userNameField.setMaxWidth(250);

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Enter Password");
        passwordField.setMaxWidth(250);

        Button setupButton = new Button("Setup");

        setupButton.setOnAction(a -> {
            // Retrieve user input
            String userName = userNameField.getText();
            String password = passwordField.getText();

            // Validate UserName and display any error
            String userNameError = UserNameRecognizer.checkForValidUserName(userName);
            userNameErrorLabel.setText(userNameError);

            // Validate Password and display any error
            String passwordError = PasswordEvaluator.evaluatePassword(password);
            passwordErrorLabel.setText(passwordError);

            // If there are any validation errors, do not proceed
            if (!userNameError.isEmpty() || !passwordError.isEmpty()) {
                return;
            }

            try {
                // Check if the user already exists to prevent duplicate insertion
                if (databaseHelper.doesUserExist(userName)) {
                    userNameErrorLabel.setText("Username already exists. Please choose another.");
                    return;
                }

                // Create a new User object with admin role and register in the database
                User user = new User(userName, password, "admin");
                databaseHelper.register(user);
                System.out.println("Administrator setup completed.");

                // Set additional roles for the user
                databaseHelper.setUserRole(userName, "admin", true);
                databaseHelper.setUserRole(userName, "student", false);
                databaseHelper.setUserRole(userName, "instructor", false);
                databaseHelper.setUserRole(userName, "staff", false);
                databaseHelper.setUserRole(userName, "reviewer", false);

                System.out.println("Administrator setup completed. Please log in with your new admin credentials.");

                // Navigate to the login page
                new UserLoginPage(databaseHelper).show(primaryStage);
            } catch (SQLException e) {
                System.err.println("Database error: " + e.getMessage());
                e.printStackTrace();
            }
        });

        VBox layout = new VBox(10, userNameField, passwordField, setupButton, userNameErrorLabel, passwordErrorLabel);
        layout.setStyle("-fx-padding: 20; -fx-alignment: center;");

        primaryStage.setScene(new Scene(layout, 800, 400));
        primaryStage.setTitle("Administrator Setup");
        primaryStage.show();
    }
}
