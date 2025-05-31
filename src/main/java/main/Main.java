package main;

import javafx.application.Application;
import javafx.stage.Stage;
import vue.VueAccueil;

public class Main extends Application {
    @Override
    public void start(Stage stage) {
        VueAccueil vue = new VueAccueil();
        vue.start(stage);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
