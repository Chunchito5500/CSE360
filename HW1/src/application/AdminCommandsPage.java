package application;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class AdminCommandsPage {

	public void show(Stage primaryStage) {
        VBox layout = new VBox(20);
        layout.setStyle("-fx-alignment: center; -fx-padding: 20;");
        Label headerLabel = new Label("Admin Main Functions");
        headerLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        
        Button listAllUsersButton = new Button("List All Users");
        listAllUsersButton.setOnAction(e -> {
            // Put code here to show a list of users
            // e.g., new AdminListUsersPage().show(primaryStage);
        });

        Button deleteUserButton = new Button("Delete User");
        deleteUserButton.setOnAction(e -> {
            // Put code here for delete user functionality
        });

        // A back button to return to the AdminHomePage,
        Button backButton = new Button("Back");
        backButton.setOnAction(e -> {
            // Return to the home page
            new AdminHomePage().show(primaryStage);
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
    }
}
