package application;

import databasePart1.DatabaseHelper;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ManageUserRolesPage {
    private final DatabaseHelper databaseHelper;
    private final String adminUsername; // current admin's username

    public ManageUserRolesPage(DatabaseHelper databaseHelper, String adminUsername) {
        this.databaseHelper = databaseHelper;
        this.adminUsername = adminUsername;
    }

    public void show(Stage primaryStage) {
        VBox layout = new VBox(10);
        layout.setStyle("-fx-padding: 20; -fx-alignment: center;");
        Label headerLabel = new Label("Manage User Roles");
        headerLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        
        Label promptLabel = new Label("Enter username of the account to manage:");
        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");

        // Checkboxes for roles
        CheckBox adminCheckBox = new CheckBox("admin");
        CheckBox studentCheckBox = new CheckBox("student");
        CheckBox instructorCheckBox = new CheckBox("instructor");
        CheckBox staffCheckBox = new CheckBox("staff");
        CheckBox reviewerCheckBox = new CheckBox("reviewer");

        Button loadButton = new Button("Load User Roles");
        Button saveButton = new Button("Save Changes");
        Button cancelButton = new Button("Cancel");
        Label messageLabel = new Label();

        loadButton.setOnAction(e -> {
            String username = usernameField.getText().trim();
            if (username.isEmpty()) {
                messageLabel.setText("Please enter a username.");
                return;
            }
            try {
                ArrayList<String> roles = databaseHelper.getUserRoles(username);
                // Clear checkboxes
                adminCheckBox.setSelected(false);
                studentCheckBox.setSelected(false);
                instructorCheckBox.setSelected(false);
                staffCheckBox.setSelected(false);
                reviewerCheckBox.setSelected(false);

                for (String role : roles) {
                    if (role.equalsIgnoreCase("admin"))
                        adminCheckBox.setSelected(true);
                    if (role.equalsIgnoreCase("student"))
                        studentCheckBox.setSelected(true);
                    if (role.equalsIgnoreCase("instructor"))
                        instructorCheckBox.setSelected(true);
                    if (role.equalsIgnoreCase("staff"))
                        staffCheckBox.setSelected(true);
                    if (role.equalsIgnoreCase("reviewer"))
                        reviewerCheckBox.setSelected(true);
                }
                messageLabel.setText("User roles loaded.");
            } catch (SQLException ex) {
                messageLabel.setText("Error loading roles: " + ex.getMessage());
                ex.printStackTrace();
            }
        });

        saveButton.setOnAction(e -> {
            String username = usernameField.getText().trim();
            if (username.isEmpty()) {
                messageLabel.setText("Please enter a username.");
                return;
            }
            // Prevent an admin from removing their own admin role.
            if (username.equals(adminUsername) && !adminCheckBox.isSelected()) {
                messageLabel.setText("You cannot remove admin role from your own account.");
                return;
            }
            List<String> selectedRoles = new ArrayList<>();
            if (adminCheckBox.isSelected()) selectedRoles.add("admin");
            if (studentCheckBox.isSelected()) selectedRoles.add("student");
            if (instructorCheckBox.isSelected()) selectedRoles.add("instructor");
            if (staffCheckBox.isSelected()) selectedRoles.add("staff");
            if (reviewerCheckBox.isSelected()) selectedRoles.add("reviewer");

            if (selectedRoles.isEmpty()) {
                messageLabel.setText("User must have at least one role.");
                return;
            }

            String rolesString = String.join(",", selectedRoles);
            try {
                // Update user roles in the database; assumes DatabaseHelper.updateUserRoles is implemented.
                databaseHelper.updateUserRoles(username, rolesString);
                messageLabel.setText("Roles updated successfully.");
            } catch (SQLException ex) {
                messageLabel.setText("Error updating roles: " + ex.getMessage());
                ex.printStackTrace();
            }
        });

        cancelButton.setOnAction(e -> {
            new AdminCommandsPage(databaseHelper, adminUsername).show(primaryStage);
        });

        layout.getChildren().addAll(headerLabel, promptLabel, usernameField, loadButton,
                adminCheckBox, studentCheckBox, instructorCheckBox, staffCheckBox, reviewerCheckBox,
                saveButton, cancelButton, messageLabel);

        Scene scene = new Scene(layout, 400, 500);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Manage User Roles");
        primaryStage.show();
    }
}