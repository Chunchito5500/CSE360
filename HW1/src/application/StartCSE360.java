package application;

import javafx.application.Application;
import javafx.stage.Stage;
import java.sql.SQLException;
import databasePart1.DatabaseHelper;

public class StartCSE360 extends Application {

    // The existing DatabaseHelper instance
    private static final DatabaseHelper databaseHelper = new DatabaseHelper();
    
    // NEW: A static getter to allow other classes to access the DatabaseHelper
    public static DatabaseHelper getDatabaseHelper() {
        return databaseHelper;
    }
    
    public static void main(String[] args) {
        launch(args);
    }
    
    @Override
    public void start(Stage primaryStage) {
        try {
            databaseHelper.connectToDatabase(); // Connect to the database
            if (databaseHelper.isDatabaseEmpty()) {
                new FirstPage(databaseHelper).show(primaryStage);
            } else {
                new SetupLoginSelectionPage(databaseHelper).show(primaryStage);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
}