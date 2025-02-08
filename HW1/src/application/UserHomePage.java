package application;

import java.sql.SQLException;

import databasePart1.DatabaseHelper;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * UserHomePage - Displays the user home page.
 * If logged in with a temporary (one-time) password, forces a password reset.
 * Otherwise, shows a welcome message.
 * A logout button is always provided to return to the login/setup selection screen.
 */
public class UserHomePage {
    private final DatabaseHelper databaseHelper;
    private final String username;
    private final boolean isTemporaryPassword;

    public UserHomePage(DatabaseHelper databaseHelper, String username, boolean isTemporaryPassword) {
        this.databaseHelper = databaseHelper;
        this.username = username;
        this.isTemporaryPassword = isTemporaryPassword;
    }

    public void show(Stage primaryStage) {
        VBox layout = new VBox(10);
        layout.setStyle("-fx-alignment: center; -fx-padding: 20;");

        // Welcome label
        Label welcomeLabel = new Label("Hello, " + username + "!");
        welcomeLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        layout.getChildren().add(welcomeLabel);

        // If the user logged in with a temporary password, force a password reset.
        if (isTemporaryPassword) {
            Label resetInfoLabel = new Label("You must reset your password before continuing.");
            resetInfoLabel.setStyle("-fx-text-fill: red; -fx-font-size: 14px;");
            layout.getChildren().add(resetInfoLabel);

            PasswordField newPasswordField = new PasswordField();
            newPasswordField.setPromptText("Enter new password");
            layout.getChildren().add(newPasswordField);

            Button resetPasswordButton = new Button("Reset Password");
            layout.getChildren().add(resetPasswordButton);

            Label resetErrorLabel = new Label();
            resetErrorLabel.setStyle("-fx-text-fill: red;");
            layout.getChildren().add(resetErrorLabel);

            resetPasswordButton.setOnAction(event -> {
                String newPassword = newPasswordField.getText();
                if (newPassword.isEmpty() || newPassword.length() < 8) {
                    resetErrorLabel.setText("Password must be at least 8 characters.");
                    return;
                }
                try {
                    databaseHelper.updateUserPassword(username, newPassword, false);
                    databaseHelper.clearTemporaryPassword(username);

                    Alert alert = new Alert(Alert.AlertType.INFORMATION,
                            "Password reset successful. Please log in again.",
                            ButtonType.OK);
                    alert.showAndWait();

                    // After successful reset, return to the login/setup selection page.
                    new SetupLoginSelectionPage(databaseHelper).show(primaryStage);
                } catch (SQLException e) {
                    resetErrorLabel.setText("Error resetting password: " + e.getMessage());
                    e.printStackTrace();
                }
            });
        }

        // Logout button (available regardless of temporary status)
        Button logoutButton = new Button("Logout");
        logoutButton.setOnAction(e -> {
            new SetupLoginSelectionPage(databaseHelper).show(primaryStage);
        });
        layout.getChildren().add(logoutButton);

        Scene userScene = new Scene(layout, 800, 400);
        primaryStage.setScene(userScene);
        primaryStage.setTitle("User Page");
        primaryStage.show();
    }
}
