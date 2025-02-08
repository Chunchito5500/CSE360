package application;

import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.util.ArrayList;

import databasePart1.DatabaseHelper;

/**
 * The UserLoginPage class provides a login interface for users.
 * It validates credentials and then:
 * - Automatically logs in users with only one role.
 * - Prompts users with multiple roles to select one.
 */
public class UserLoginPage {

    private final DatabaseHelper databaseHelper;

    public UserLoginPage(DatabaseHelper databaseHelper) {
        this.databaseHelper = databaseHelper;
    }

    public void show(Stage primaryStage) {
        // Create the initial login form
        VBox loginLayout = new VBox(10);
        loginLayout.setStyle("-fx-padding: 20; -fx-alignment: center;");

        TextField userNameField = new TextField();
        userNameField.setPromptText("Enter userName");
        userNameField.setMaxWidth(250);

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Enter Password");
        passwordField.setMaxWidth(250);

        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 12px;");

        Button loginButton = new Button("Login");

        loginButton.setOnAction(a -> {
            String userName = userNameField.getText();
            String password = passwordField.getText();

            try {
                // Create User object (role is blank initially)
                User user = new User(userName, password, "");

                // Retrieve the user's roles from the database
                ArrayList<String> roles = databaseHelper.getUserRoles(userName);

                if (roles.isEmpty()) {
                    errorLabel.setText("User account does not exist or does not have assigned roles.");
                } else {
                    // First, verify credentials (username and password only)
                    boolean credentialsValid = databaseHelper.login(user);
                    if (!credentialsValid) {
                        errorLabel.setText("Error logging in (invalid credentials).");
                        return;
                    }
                    
                    System.out.println("Roles retrieved: " + roles);

                    // If only one role exists, automatically navigate to that role's home page.
                    if (roles.size() == 1) {
                        user.setRole(roles.get(0));
                        System.out.println("Only one role found: " + roles.get(0) + ". Logging in automatically.");
                        navigateToRoleHome(primaryStage, user, roles.get(0));
                    } else {
                        // More than one role: prompt the user to select one.
                        displayRoleSelection(primaryStage, user, roles);
                    }
                }
            } catch (SQLException e) {
                System.err.println("Database error: " + e.getMessage());
                e.printStackTrace();
            }
        });

        loginLayout.getChildren().addAll(userNameField, passwordField, loginButton, errorLabel);

        Scene loginScene = new Scene(loginLayout, 800, 400);
        primaryStage.setScene(loginScene);
        primaryStage.setTitle("User Login");
        primaryStage.show();
    }

    /**
     * Prompts the user to select their role before proceeding.
     */
    private void displayRoleSelection(Stage primaryStage, User user, ArrayList<String> roles) {
        VBox roleLayout = new VBox(10);
        roleLayout.setStyle("-fx-padding: 20; -fx-alignment: center;");

        Label selectRoleLabel = new Label("Select a role to log in as:");

        ComboBox<String> roleComboBox = new ComboBox<>();
        roleComboBox.getItems().addAll(roles);

        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 12px;");

        Button confirmRoleButton = new Button("Confirm");
        confirmRoleButton.setOnAction(e -> {
            String selectedRole = roleComboBox.getValue();
            if (selectedRole == null) {
                errorLabel.setText("Please select a role.");
                return;
            }

            try {
                // Update the user's role in the database and in the user object
                databaseHelper.switchRole(user.getUserName(), selectedRole);
                user.setRole(selectedRole);
                System.out.println("User role switched to: " + selectedRole);
                navigateToRoleHome(primaryStage, user, selectedRole);
            } catch (SQLException ex) {
                System.err.println("Error switching roles: " + ex.getMessage());
                ex.printStackTrace();
            }
        });

        roleLayout.getChildren().addAll(selectRoleLabel, roleComboBox, confirmRoleButton, errorLabel);

        Scene roleScene = new Scene(roleLayout, 800, 400);
        primaryStage.setScene(roleScene);
        primaryStage.setTitle("Select Role");
        primaryStage.show();
    }

    /**
     * Navigates the user to the correct home page based on their role.
     */
    private void navigateToRoleHome(Stage primaryStage, User user, String role) {
        // Normalize the role: trim whitespace and convert to lower case.
        String normalizedRole = role.trim().toLowerCase();
        System.out.println("Navigating to role: " + normalizedRole);
        switch (normalizedRole) {
            case "admin":
                new AdminHomePage(databaseHelper, user.getUserName()).show(primaryStage);
                break;
            case "user":
                try {
                    new UserHomePage(databaseHelper, user.getUserName(),
                        databaseHelper.isTemporaryPassword(user.getUserName(), user.getPassword())
                    ).show(primaryStage);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                break;
            case "instructor":
                new InstructorHomePage(databaseHelper).show(primaryStage);
                break;
            case "staff":
                new StaffHomePage(databaseHelper).show(primaryStage);
                break;
            case "reviewer":
                new ReviewerHomePage(databaseHelper).show(primaryStage);
                break;
            case "student":
                new StudentPage(databaseHelper, user.getUserName()).show(primaryStage);
                break;
            default:
                System.err.println("Unknown role: " + normalizedRole);
        }
    }
}