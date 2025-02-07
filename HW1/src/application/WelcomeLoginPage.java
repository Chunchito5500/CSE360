package application;

import databasePart1.DatabaseHelper;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.SQLException;

/**
 * The WelcomeLoginPage class displays a welcome screen for authenticated users.
 * It allows the user to navigate to their respective page (based on their role) or log out.
 */
public class WelcomeLoginPage {
    
    private final DatabaseHelper databaseHelper;

    public WelcomeLoginPage(DatabaseHelper databaseHelper) {
        this.databaseHelper = databaseHelper;
    }

    /**
     * Displays the welcome page.
     * @param primaryStage The primary stage where the scene is displayed.
     * @param user The authenticated user.
     */
    public void show(Stage primaryStage, User user) {
        VBox layout = new VBox(10);
        layout.setStyle("-fx-alignment: center; -fx-padding: 20;");
        
        // Welcome message includes the user's name
        Label welcomeLabel = new Label("Welcome, " + user.getUserName() + "!");
        welcomeLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        
        // Retrieve and trim the user's role (which may be a comma-separated string)
        final String userRole = (user.getRole() != null) ? user.getRole().trim() : "";
        
        System.out.println("WelcomeLoginPage Loaded");
        System.out.println("User Role: " + userRole);
        
        // Button to navigate to the user's respective home page based on role
        Button continueButton = new Button("Continue to your Page");
        continueButton.setOnAction(a -> {
            System.out.println("Navigating to user page...");
            if (userRole.equalsIgnoreCase("admin")) {
                System.out.println("Loading Admin Home Page...");
                new AdminHomePage(databaseHelper, user.getUserName()).show(primaryStage);
            } else if (userRole.equalsIgnoreCase("user")) {
                try {
                    boolean isTemporaryPassword = databaseHelper.isTemporaryPassword(user.getUserName(), user.getPassword());
                    System.out.println("Loading User Home Page...");
                    new UserHomePage(databaseHelper, user.getUserName(), isTemporaryPassword).show(primaryStage);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            } else {
                System.err.println("❌ ERROR: Unknown role '" + userRole + "'");
            }
        });
        
        // Logout button returns the user to the Setup/Login Selection page
        Button logoutButton = new Button("Logout");
        logoutButton.setOnAction(a -> {
            new SetupLoginSelectionPage(databaseHelper).show(primaryStage);
        });
        
        layout.getChildren().addAll(welcomeLabel, continueButton, logoutButton);
        Scene welcomeScene = new Scene(layout, 800, 400);
        
        primaryStage.setScene(welcomeScene);
        primaryStage.setTitle("Welcome Page");
        primaryStage.show();
    }
}