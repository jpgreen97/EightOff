package ui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        Scene scene = new Scene(FXMLLoader.load(getClass().getResource("/ui/board.fxml")));
        stage.setTitle("Solitario - JavaFX");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) { launch(args); }
}