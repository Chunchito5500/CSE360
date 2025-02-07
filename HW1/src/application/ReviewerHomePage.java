package application;

import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import databasePart1.DatabaseHelper;

public class ReviewerHomePage {
    private final DatabaseHelper databaseHelper;
    
    public ReviewerHomePage(DatabaseHelper databaseHelper) {
        this.databaseHelper = databaseHelper;
    }
    
    public void show(Stage primaryStage) {
        VBox layout = new VBox(10);
        layout.setStyle("-fx-alignment: center; -fx-padding: 20;");
        Label label = new Label("Reviewer Home Page");
        layout.getChildren().add(label);
        
        Scene scene = new Scene(layout, 800, 400);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Reviewer Home Page");
        primaryStage.show();
    }
}