package com.tuneupv2;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        Label currentlyPlayingLabel = new Label("No song playing");
        Controller controller = new Controller(currentlyPlayingLabel);
        View view = new View(controller);

        Scene scene = view.createScene(primaryStage);

        primaryStage.setTitle("TuneUp V2");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
