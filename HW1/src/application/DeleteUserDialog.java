package application;

import databasePart1.DatabaseHelper;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.sql.SQLException;

public class DeleteUserDialog {
    private final DatabaseHelper databaseHelper;
    private final String adminUsername; // current admin's username

    public DeleteUserDialog(DatabaseHelper databaseHelper, String adminUsername) {
        this.databaseHelper = databaseHelper;
        this.adminUsername = adminUsername;
    }

    public void show(Stage primaryStage) {
        VBox layout = new VBox(10);
        layout.setStyle("-fx-padding: 20; -fx-alignment: center;");

        Label promptLabel = new Label("Enter the username of the account to delete:");
        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");

        Button deleteButton = new Button("Delete");
        Button cancelButton = new Button("Cancel");
        Label messageLabel = new Label();

        deleteButton.setOnAction(e -> {
            String usernameToDelete = usernameField.getText().trim();
            if (usernameToDelete.isEmpty()) {
                messageLabel.setText("Please enter a username.");
                return;
            }
            if (usernameToDelete.equals(adminUsername)) {
                messageLabel.setText("You cannot delete your own account.");
                return;
            }
            // Confirmation dialog
            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmAlert.setTitle("Confirm Deletion");
            confirmAlert.setHeaderText(null);
            confirmAlert.setContentText("Are you sure you want to delete user: " + usernameToDelete + "?");
            ButtonType yesButton = new ButtonType("Yes");
            ButtonType noButton = new ButtonType("No", ButtonBar.ButtonData.CANCEL_CLOSE);
            confirmAlert.getButtonTypes().setAll(yesButton, noButton);

            confirmAlert.showAndWait().ifPresent(response -> {
                if (response == yesButton) {
                    try {
                        boolean deleted = databaseHelper.deleteUser(usernameToDelete);
                        if (deleted) {
                            messageLabel.setText("User deleted successfully.");
                        } else {
                            messageLabel.setText("Unable to delete user. It might be an admin account or does not exist.");
                        }
                    } catch (SQLException ex) {
                        messageLabel.setText("Database error: " + ex.getMessage());
                        ex.printStackTrace();
                    }
                }
            });
        });

        cancelButton.setOnAction(e -> {
            new AdminCommandsPage(databaseHelper, adminUsername).show(primaryStage);
        });

        layout.getChildren().addAll(promptLabel, usernameField, deleteButton, cancelButton, messageLabel);
        Scene scene = new Scene(layout, 400, 300);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Delete User");
        primaryStage.show();
    }
}