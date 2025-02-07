package application;

import databasePart1.DatabaseHelper;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * AdminCommandsPage - Provides main admin functions.
 */
public class AdminCommandsPage {
    private final DatabaseHelper databaseHelper;
    private final String username;

    // ✅ Constructor to receive databaseHelper & username
    public AdminCommandsPage(DatabaseHelper databaseHelper, String username) {
        this.databaseHelper = databaseHelper;
        this.username = username;
    }

    public void show(Stage primaryStage) {
        VBox layout = new VBox(20);
        layout.setStyle("-fx-alignment: center; -fx-padding: 20;");

        Label headerLabel = new Label("Admin Main Functions");
        headerLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        // List Users Button
        Button listAllUsersButton = new Button("List All Users");
        listAllUsersButton.setOnAction(e -> {
            // TODO: Implement user listing functionality
            System.out.println("🟢 List All Users button clicked!");
        });

        // Delete User Button
        Button deleteUserButton = new Button("Delete User");
        deleteUserButton.setOnAction(e -> {
            // TODO: Implement delete user functionality
            System.out.println("🟠 Delete User button clicked!");
        });

        // Back Button to AdminHomePage
        Button backButton = new Button("Back");
        backButton.setOnAction(e -> {
            System.out.println("🔵 Navigating back to AdminHomePage...");
            new AdminHomePage(databaseHelper, username).show(primaryStage); // ✅ Pass required arguments
        });

        layout.getChildren().addAll(
            headerLabel,
            listAllUsersButton,
            deleteUserButton,
            backButton
        );

        Scene scene = new Scene(layout, 800, 400);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Admin Commands");
        primaryStage.show();
    }
}