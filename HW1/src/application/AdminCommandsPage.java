package application;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import java.util.*;
import java.sql.*;
import databasePart1.DatabaseHelper;
import javafx.geometry.*;

/**
 * AdminCommandsPage - Provides main admin functions.
 */
public class AdminCommandsPage {
    private final DatabaseHelper databaseHelper;
    private final User user;

    private static final Set<String> VALID_ROLES = new HashSet<>(
		    Arrays.asList("admin", "student", "staff", "instructor", "reviewer")
		);

    public AdminCommandsPage(DatabaseHelper databaseHelper, User user) {
        this.databaseHelper = databaseHelper;
        this.user = user;
    }

    public void show(Stage primaryStage) {
        VBox layout = new VBox(20);
        layout.setStyle("-fx-alignment: center; -fx-padding: 20;");

        Label headerLabel = new Label("Admin Main Functions");
        headerLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        // List Users Button
        Button listAllUsersButton = new Button("List All Users");
        listAllUsersButton.setOnAction(e -> {
            usersList(primaryStage);
        });
        
        Button backButton = new Button("Back");
        backButton.setOnAction(e -> {
            new AdminHomePage(databaseHelper, user).show(primaryStage);
        });

        layout.getChildren().addAll(headerLabel, listAllUsersButton, backButton);

        Scene scene = new Scene(layout, 800, 400);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Admin Commands");
        
    }
    private void usersList(Stage primaryStage) {
		VBox listLayout = new VBox(10);
        listLayout.setStyle("-fx-padding: 20; -fx-alignment: center;");

        Label titleLabel = new Label("All User Accounts");
        titleLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        listLayout.getChildren().add(titleLabel);


        // Retrieve all users from DatabaseHelper
        List<User> allUsers = null;
        try {
            allUsers = databaseHelper.getAllUsers();
        } catch (SQLException ex) {
            ex.printStackTrace();
            listLayout.getChildren().add(new Label("Error retrieving users"));
            
            // Show this partial scene
            Scene errorScene = new Scene(listLayout, 800, 400);
            primaryStage.setScene(errorScene);
            return;
        }

        // If none found, or if list is empty
        if (allUsers == null || allUsers.isEmpty()) {
            listLayout.getChildren().add(new Label("(No users found)"));
        } else {
            // Display each user's info
            for (User u : allUsers) {
            	
            	HBox userRow = new HBox(10);
                userRow.setAlignment(Pos.CENTER);
                
            	String username = u.getUserName();
                String nameOrBlank  = (u.getName()  != null) ? u.getName()  : "";
                String emailOrBlank = (u.getEmail() != null) ? u.getEmail() : "";
                String role     = u.getRole();

                String userInfo = String.format(
                    "Username: %s | Name: %s | Email: %s | Role: %s",
                    u.getUserName(),
                    nameOrBlank,
                    emailOrBlank,
                    u.getRole()
                );
                Label userInfoLabel = new Label(userInfo);
                
                Button deleteBtn = new Button("Delete");
                deleteBtn.setOnAction(e -> {
                    //Check if user is admin. If so, disallow
                    if (role.equalsIgnoreCase("admin")) {
                        showErrorAlert("Error","Cannot Delete Admin");
                        return;
                    }

                    // Show confirmation alert
                    showDeleteConfirmation(username);

                    
                    usersList(primaryStage);
                });
                
                Button changeRoleBtn = new Button("Change Role");
                changeRoleBtn.setOnAction(e -> {
                    try {
                        handleChangeRole(u, primaryStage);
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                        showErrorAlert("Error Changing Role", ex.getMessage());
                    }
                    
                    usersList(primaryStage);
                });
                
                userRow.getChildren().addAll(userInfoLabel, deleteBtn, changeRoleBtn);
                listLayout.getChildren().add(userRow);
            }
        }

        // "Back" button to return to the main admin commands
        Button backBtn = new Button("Back");
        backBtn.setOnAction(e -> show(primaryStage));
        listLayout.getChildren().add(backBtn);	

        // Replace the scene on the same stage with our user list
        Scene listScene = new Scene(listLayout, 800, 400);
        primaryStage.setScene(listScene);
        primaryStage.setTitle("All Users");
	}
	
	private void handleChangeRole(User u, Stage primaryStage) throws SQLException {
        // If user is admin but not the same as currentAdmin => disallow
        if ("admin".equalsIgnoreCase(u.getRole()) 
            && !u.getUserName().equals(user.getUserName())) {
            
            showErrorAlert("Cannot Change Role", 
                "You cannot change another admin's role.");
            return;
        }

        // If user is the current admin (the one logged in) and is the only admin => can't remove admin
        if ("admin".equalsIgnoreCase(u.getRole()) 
            && u.getUserName().equals(user.getUserName())) {

            int adminCount = databaseHelper.getAdminCount();
            if (adminCount == 1) {
                showErrorAlert("Cannot Change Role", 
                    "You are the only admin; you cannot revoke your own admin privileges.");
                return;
            }
        }

        // choose the new role
        TextInputDialog dialog = new TextInputDialog(u.getRole());
        dialog.setTitle("Change Role");
        dialog.setHeaderText("Change role for " + u.getUserName());
        dialog.setContentText("Enter new role (e.g. admin, student, staff, instructor, reviewer):");

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            String newRole = result.get().trim().toLowerCase();
            if (newRole.isEmpty()) {
                showErrorAlert("Invalid Role", "Role cannot be empty.");
                return;
            }
            if (!VALID_ROLES.contains(newRole)) {
                showErrorAlert("Invalid Role", 
                    "Please enter a valid role: " + VALID_ROLES);
                return;
            }

            databaseHelper.updateUserRole(u.getUserName(), newRole);
            usersList(primaryStage);
        }
    }

private void showDeleteConfirmation(String username) {
    Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
    confirmAlert.setTitle("Confirm Deletion");
    confirmAlert.setHeaderText(null);
    confirmAlert.setContentText("Are you sure you want to delete user: " + username + "?");

    confirmAlert.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);

    Optional<ButtonType> result = confirmAlert.showAndWait();
    if (result.isPresent() && result.get() == ButtonType.YES) {
        // If yes, delete
        try {
            databaseHelper.deleteUser(username);
        } catch (SQLException e) {
            e.printStackTrace();
            showErrorAlert("Error Deleting User", e.getMessage());
        }
    }
}


private void showErrorAlert(String title, String message) {
    Alert errorAlert = new Alert(Alert.AlertType.ERROR);
    errorAlert.setTitle(title);
    errorAlert.setHeaderText(null);
    errorAlert.setContentText(message);
    errorAlert.showAndWait();
}
}
