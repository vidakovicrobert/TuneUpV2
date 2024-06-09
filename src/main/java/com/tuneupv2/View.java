package com.tuneupv2;

import javafx.beans.property.SimpleStringProperty;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import javax.swing.*;
import java.io.File;

public class View {
    private Controller controller;
    private Label currentlyPlayingLabel = new Label("No song playing");
    private ImageView albumArtView = new ImageView();

    public View(Controller controller) {
        this.controller = controller;
    }

    public Scene createScene(Stage primaryStage) {
        HBox topControls = createTopControls(primaryStage);
        TableView<Song> tableView = createTableView();
        VBox bottomControls = createBottomControls();

        BorderPane root = new BorderPane();
        root.setTop(topControls);
        root.setCenter(tableView);
        root.setBottom(bottomControls);

        return new Scene(root, 650, 600);
    }

    private HBox createTopControls(Stage primaryStage) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select the music folder");

        Button selectFolderButton = new Button();
        selectFolderButton.setGraphic(new ImageView(getClass().getResource("/folder-icon.png").toString()));

        Label folderLabel = new Label("Select the music folder");
        Label songsDetectedLabel = new Label("Songs detected: 0");
        songsDetectedLabel.setStyle("-fx-font-size: 10px;"); // Set the font size to be smaller

        selectFolderButton.setOnAction(e -> {
            File selectedDirectory = directoryChooser.showDialog(primaryStage);
            if (selectedDirectory != null) {
                controller.loadSongs(selectedDirectory);
                folderLabel.setText("Currently playing from: " + selectedDirectory.getName());

                // Update the number of detected songs
                int songCount = controller.getSongs().size();
                songsDetectedLabel.setText("Songs detected: " + songCount);
            }
        });

        VBox folderSelectionBox = new VBox(0, folderLabel, songsDetectedLabel);
        folderSelectionBox.setStyle("-fx-alignment: center;");

        HBox topControls = new HBox(5, selectFolderButton, folderSelectionBox);
        topControls.setStyle("-fx-padding: 5; -fx-alignment: center;");
        return topControls;
    }

    private TableView<Song> createTableView() {
        TableView<Song> tableView = new TableView<>();
        tableView.setItems(controller.getSongs());

        TableColumn<Song, String> numberColumn = new TableColumn<>("#");
        numberColumn.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(controller.getSongs().indexOf(data.getValue()) + 1)));
        numberColumn.setPrefWidth(30);

        TableColumn<Song, String> titleColumn = new TableColumn<>("Title");
        titleColumn.setCellValueFactory(data -> data.getValue().songNameProperty());

        TableColumn<Song, String> artistColumn = new TableColumn<>("Artist");
        artistColumn.setCellValueFactory(data -> data.getValue().artistNameProperty());

        TableColumn<Song, String> albumColumn = new TableColumn<>("Album");
        albumColumn.setCellValueFactory(data -> data.getValue().formatProperty());

        TableColumn<Song, String> durationColumn = createDurationColumn();

        tableView.getColumns().addAll(numberColumn, titleColumn, artistColumn, albumColumn, durationColumn);
        return tableView;
    }

    private TableColumn<Song, String> createDurationColumn() {
        TableColumn<Song, String> durationColumn = new TableColumn<>("Duration");
        durationColumn.setCellValueFactory(data -> data.getValue().durationProperty());

        // Set the cell factory to display duration in minutes and seconds format
        durationColumn.setCellFactory(column -> {
            return new TableCell<>() {
                @Override
                protected void updateItem(String duration, boolean empty) {
                    super.updateItem(duration, empty);
                    if (empty || duration == null) {
                        setText(null);
                    } else {
                        // Convert duration from seconds to minutes and seconds
                        int totalSeconds = Integer.parseInt(duration);
                        int minutes = totalSeconds / 60;
                        int seconds = totalSeconds % 60;
                        setText(String.format("%02d:%02d", minutes, seconds));
                    }
                }
            };
        });

        return durationColumn;
    }

    private Button createPlayPauseButton() {
        Button playPauseButton = new Button("⏵");
        playPauseButton.setPrefSize(40, 40); // Set preferred size

        // Set the initial button state and action
        playPauseButton.setOnAction(event -> {
            if (controller.isPlayingProperty().get()) {
                controller.pause();
            } else {
                controller.play();
            }
        });

        // Update button state when playback status changes
        controller.isPlayingProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                playPauseButton.setText("⏸"); // Change button text to pause icon
            } else {
                playPauseButton.setText("⏵"); // Change button text to play icon
            }
        });

        return playPauseButton;
    }

    private VBox createCurrentlyPlayingBox() {
        currentlyPlayingLabel.textProperty().bind(controller.currentlyPlayingProperty());
        VBox currentlyPlayingBox = new VBox(10, albumArtView, currentlyPlayingLabel);
        currentlyPlayingBox.setStyle("-fx-padding: 10; -fx-alignment: center;");
        albumArtView.setFitWidth(64); // Set preferred width for the album art
        albumArtView.setFitHeight(64); // Set preferred height for the album art

        return currentlyPlayingBox;
    }

    private VBox createBottomControls() {
        Button playPauseButton = createPlayPauseButton();
        playPauseButton.setPrefSize(40, 40); // Set preferred size

        Button nextButton = new Button("⏭");
        nextButton.setPrefSize(40, 40); // Set preferred size

        Button previousButton = new Button("⏮");
        previousButton.setPrefSize(40, 40); // Set preferred size

        Button shuffleButton = new Button("🔀");
        shuffleButton.setPrefSize(40, 40); // Set preferred size

        // Set actions for other buttons (next, previous, shuffle)
        nextButton.setOnAction(event -> controller.next());
        previousButton.setOnAction(event -> controller.previous());
        shuffleButton.setOnAction(event -> controller.shuffle());

        // Create the volume slider
        Slider volumeSlider = controller.getVolumeSlider();
        volumeSlider.setPrefWidth(100); // Set preferred width for the volume slider

        HBox controlButtons = new HBox(10, previousButton, playPauseButton, nextButton, shuffleButton, volumeSlider);
        controlButtons.setStyle("-fx-padding: 10; -fx-alignment: center;");

        VBox bottomControls = new VBox(10, createCurrentlyPlayingBox(), controlButtons);
        bottomControls.setStyle("-fx-padding: 10; -fx-alignment: center;");

        return bottomControls;
    }
}
