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
            // Launch the ListUsersPage (you need to implement this class)
            new ListUsersPage(databaseHelper).show(primaryStage);
        });

        // Delete User Button
        Button deleteUserButton = new Button("Delete User");
        deleteUserButton.setOnAction(e -> {
            // Launch the DeleteUserDialog (or page) to handle deletion with confirmation
            new DeleteUserDialog(databaseHelper, username).show(primaryStage);
        });
        
        // Manage User Roles Button
        Button manageRolesButton = new Button("Manage User Roles");
        manageRolesButton.setOnAction(e -> {
            // Launch the ManageUserRolesPage (you need to implement this class)
            new ManageUserRolesPage(databaseHelper, username).show(primaryStage);
        });

        // Back Button to AdminHomePage
        Button backButton = new Button("Back");
        backButton.setOnAction(e -> {
            new AdminHomePage(databaseHelper, username).show(primaryStage);
        });

        layout.getChildren().addAll(headerLabel, listAllUsersButton, deleteUserButton, manageRolesButton, backButton);

        Scene scene = new Scene(layout, 800, 400);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Admin Commands");
        primaryStage.show();
    }
}