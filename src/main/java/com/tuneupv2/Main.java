package com.tuneupv2;

import javafx.application.Application;
import javafx.stage.Stage;
import javafx.stage.DirectoryChooser;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;


import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Main extends Application {
    private List<Song> songs;
    private MediaPlayer mediaPlayer;
    private int currentSongIndex = 0;

    @Override
    public void start(Stage primaryStage) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select Music Folder");

        Button selectFolderButton = new Button("Select Folder");
        selectFolderButton.setOnAction(e -> {
            File selectedDirectory = directoryChooser.showDialog(primaryStage);
            if (selectedDirectory != null) {
                loadSongs(selectedDirectory);
            }
        });

        Button playButton = new Button("Play");
        Button pauseButton = new Button("Pause");
        Button nextButton = new Button("Next");
        Button previousButton = new Button("Previous");
        Button shuffleButton = new Button("Shuffle");

        playButton.setOnAction(e -> play());
        pauseButton.setOnAction(e -> pause());
        nextButton.setOnAction(e -> next());
        previousButton.setOnAction(e -> previous());
        shuffleButton.setOnAction(e -> shuffle());

        HBox controls = new HBox(10, playButton, pauseButton, nextButton, previousButton, shuffleButton);
        VBox root = new VBox(10, selectFolderButton, controls);

        Scene scene = new Scene(root, 800, 600);

        primaryStage.setTitle("TuneUpV2");
        primaryStage.setScene(scene);
        primaryStage.setResizable(true);
        primaryStage.show();
    }

    private void loadSongs(File folder) {
        songs = new ArrayList<>();
        for (File file : Objects.requireNonNull(folder.listFiles())) {
            if (file.getName().endsWith(".mp3")) {
                Song song = new Song(file.toURI().toString());
                songs.add(song);
            }
        }
        if (!songs.isEmpty()) {
            playSong(songs.get(currentSongIndex));
        }
    }

    private void playSong(Song song) {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
        }
        Media media = new Media(song.getUrl());
        mediaPlayer = new MediaPlayer(media);
        mediaPlayer.play();
    }

    private void play() {
        if (mediaPlayer != null) {
            mediaPlayer.play();
        }
    }

    private void pause() {
        if (mediaPlayer != null) {
            mediaPlayer.pause();
        }
    }

    private void next() {
        if (!songs.isEmpty()) {
            currentSongIndex = (currentSongIndex + 1) % songs.size();
            playSong(songs.get(currentSongIndex));
        }
    }

    private void previous() {
        if (!songs.isEmpty()) {
            currentSongIndex = (currentSongIndex - 1 + songs.size()) % songs.size();
            playSong(songs.get(currentSongIndex));
        }
    }

    private void shuffle() {
        if (!songs.isEmpty()) {
            Collections.shuffle(songs);
            currentSongIndex = 0;
            playSong(songs.get(currentSongIndex));
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
