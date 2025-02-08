package application;

import databasePart1.DatabaseHelper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.sql.*;
import java.util.ArrayList;

public class ListUsersPage {
    private final DatabaseHelper databaseHelper;

    public ListUsersPage(DatabaseHelper databaseHelper) {
        this.databaseHelper = databaseHelper;
    }

    public void show(Stage primaryStage) {
        VBox layout = new VBox(10);
        layout.setStyle("-fx-padding: 20; -fx-alignment: center;");

        Label header = new Label("List of All Users");
        header.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        TableView<User> tableView = new TableView<>();

        // Define columns based on the existing fields in your User class
        TableColumn<User, String> userNameCol = new TableColumn<>("Username");
        userNameCol.setCellValueFactory(new PropertyValueFactory<>("userName"));

        TableColumn<User, String> roleCol = new TableColumn<>("Roles");
        roleCol.setCellValueFactory(new PropertyValueFactory<>("role"));

        tableView.getColumns().addAll(userNameCol, roleCol);

        ObservableList<User> userData = FXCollections.observableArrayList();
        try {
            // Use the updated query in DatabaseHelper.getAllUsers() that selects only userName and role
            ArrayList<User> users = databaseHelper.getAllUsers();
            userData.addAll(users);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        tableView.setItems(userData);

        Button backButton = new Button("Back");
        backButton.setOnAction(e -> {
            // Adjust this to use the actual admin username if needed
            new AdminCommandsPage(databaseHelper, "admin").show(primaryStage);
        });

        layout.getChildren().addAll(header, tableView, backButton);
        Scene scene = new Scene(layout, 800, 400);
        primaryStage.setScene(scene);
        primaryStage.setTitle("List All Users");
        primaryStage.show();
    }
}