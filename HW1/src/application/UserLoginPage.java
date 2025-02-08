package application;

import java.sql.SQLException;
import java.util.ArrayList;

import databasePart1.DatabaseHelper;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;


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

            User user = new User(userName, password, "");

            // Call the asynchronous login method
            databaseHelper.loginAsync(user, new LoginCallback() {
                @Override
                public void onLoginSuccess(User user) {
                    // UI updates must run on the JavaFX Application Thread
                    Platform.runLater(() -> {
                        // Retrieve roles and continue to the role selection or home page
                        try {
                            ArrayList<String> roles = databaseHelper.getUserRoles(user.getUserName());
                            if (roles.isEmpty()) {
                                errorLabel.setText("User account does not have assigned roles.");
                            } else if (roles.size() == 1) {
                                user.setRole(roles.get(0));
                                navigateToRoleHome(primaryStage, user, roles.get(0));
                            } else {
                                displayRoleSelection(primaryStage, user, roles);
                            }
                        } catch (SQLException ex) {
                            errorLabel.setText("Error retrieving roles: " + ex.getMessage());
                            ex.printStackTrace();
                        }
                    });
                }
                @Override
                public void onLoginFailure(String errorMessage) {
                    // UI updates must run on the JavaFX Application Thread
                    Platform.runLater(() -> errorLabel.setText(errorMessage));
                }
            });
        });


        loginLayout.getChildren().addAll(userNameField, passwordField, loginButton, errorLabel);

        Scene loginScene = new Scene(loginLayout, 800, 400);
        primaryStage.setScene(loginScene);
        primaryStage.setTitle("User Login");
        primaryStage.show();
    }

    
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

    
    private void navigateToRoleHome(Stage primaryStage, User user, String role) {
        // Normalize the role: trim whitespace and convert to lower case.
        String normalizedRole = role.trim().toLowerCase();
        System.out.println("Navigating to role: " + normalizedRole);
        switch (normalizedRole) {
            case "admin":
                new AdminHomePage(databaseHelper, user).show(primaryStage);
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
         //       new InstructorHomePage(databaseHelper).show(primaryStage);
                break;
            case "staff":
          //      new StaffHomePage(databaseHelper).show(primaryStage);
                break;
            case "reviewer":
        //        new ReviewerHomePage(databaseHelper).show(primaryStage);
                break;
            case "student":
        //        new StudentPage(databaseHelper, user.getUserName()).show(primaryStage);
                break;
            default:
                System.err.println("Unknown role: " + normalizedRole);
        }
    }
}
