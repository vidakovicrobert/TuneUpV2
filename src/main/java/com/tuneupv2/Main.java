package com.tuneupv2;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        Label currentlyPlayingLabel = new Label("No song playing");

        Controller controller = new Controller(currentlyPlayingLabel);
        View view = new View(controller);

        Scene scene = view.createScene(primaryStage);
        primaryStage.setTitle("TuneupV2");
        primaryStage.setScene(scene);
        primaryStage.show();
        primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/icon.png")));

    }

    public static void main(String[] args) {
        launch(args);
    }
}
