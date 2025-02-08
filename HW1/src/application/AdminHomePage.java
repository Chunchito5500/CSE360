package application;

import databasePart1.DatabaseHelper;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.sql.SQLException;

/**
 * AdminHomePage - Admin dashboard for managing users.
 */
public class AdminHomePage {
    private final DatabaseHelper databaseHelper;
    private final String username;

    public AdminHomePage(DatabaseHelper databaseHelper, String username) {
        this.databaseHelper = databaseHelper;
        this.username = username;
    }

    public void show(Stage primaryStage) {
        System.out.println("✅ AdminHomePage.show() was called!");

        VBox layout = new VBox(10);
        layout.setStyle("-fx-alignment: center; -fx-padding: 20;");

        // Admin Welcome Label
        Label adminLabel = new Label("Hello, Admin " + username + "!");
        adminLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        // "Continue" Button to Navigate to AdminCommandsPage
        Button continueButton = new Button("Continue");
        continueButton.setOnAction(e -> {
            System.out.println("🟡 Navigating to AdminCommandsPage...");
            new AdminCommandsPage(databaseHelper, username).show(primaryStage);
        });

        // --- Invitation Code Generation Section ---
        TextField roleField = new TextField();
        roleField.setPromptText("Enter Role (admin, student, instructor, staff, reviewer)");

        TextField hoursValidField = new TextField();
        hoursValidField.setPromptText("Valid Hours");

        Button generateInviteButton = new Button("Generate Invitation Code");
        Label inviteCodeLabel = new Label();

        generateInviteButton.setOnAction(a -> {
            String role = roleField.getText().trim();
            try {
                int hours = Integer.parseInt(hoursValidField.getText().trim());
                // Check that the entered role is one of the allowed roles.
                if (!(role.equalsIgnoreCase("admin") || role.equalsIgnoreCase("student") ||
                      role.equalsIgnoreCase("instructor") || role.equalsIgnoreCase("staff") ||
                      role.equalsIgnoreCase("reviewer"))) {
                    inviteCodeLabel.setText("⚠️ Invalid role! Must be one of: admin, student, instructor, staff, reviewer.");
                    return;
                }
                String invitationCode = databaseHelper.generateInvitationCode(role, hours);
                inviteCodeLabel.setText("✅ Invite Code: " + invitationCode);
            } catch (NumberFormatException ex) {
                inviteCodeLabel.setText("⚠️ Please enter a valid number for hours.");
            }
        });

        // --- Reset User Password Section ---
        TextField usernameField = new TextField();
        usernameField.setPromptText("Enter Username for Password Reset");
        usernameField.setMaxWidth(250);

        Button resetPasswordButton = new Button("Generate One-Time Password");
        Label resetPasswordLabel = new Label();

        resetPasswordButton.setOnAction(a -> {
            String userToReset = usernameField.getText().trim();
            if (userToReset.isEmpty()) {
                resetPasswordLabel.setText("⚠️ Username cannot be empty!");
                return;
            }
            try {
                if (!databaseHelper.doesUserExist(userToReset)) {
                    resetPasswordLabel.setText("⚠️ User does not exist!");
                    return;
                }
                String oneTimePassword = databaseHelper.generateOneTimePassword(userToReset);
                resetPasswordLabel.setText("✅ OTP for " + userToReset + ": " + oneTimePassword);
            } catch (SQLException e) {
                resetPasswordLabel.setText("⚠️ Database error: " + e.getMessage());
                e.printStackTrace();
            }
        });

        // --- Logout Button ---
        Button logoutButton = new Button("Logout");
        logoutButton.setOnAction(a -> {
            System.out.println("🔴 Logging out...");
            new SetupLoginSelectionPage(databaseHelper).show(primaryStage);
        });

        // Add all elements to layout
        layout.getChildren().addAll(
                adminLabel, continueButton,
                roleField, hoursValidField, generateInviteButton, inviteCodeLabel,
                usernameField, resetPasswordButton, resetPasswordLabel,
                logoutButton
        );

        Scene adminScene = new Scene(layout, 800, 400);
        primaryStage.setScene(adminScene);
        primaryStage.setTitle("Admin Dashboard");
        primaryStage.show();

        System.out.println("✅ AdminHomePage UI should now be visible.");
    }
}