package com.tuneupv2;

import javafx.beans.property.SimpleStringProperty;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.concurrent.atomic.AtomicBoolean;

public class View {
    private Controller controller;
    private Label currentlyPlayingLabel = new Label("No song playing");

    public View(Controller controller) {
        this.controller = controller;
    }

    public Scene createScene(Stage primaryStage) {
        HBox topControls = createTopControls(primaryStage);
        TableView<Song> tableView = createTableView();
        HBox bottomControls = createBottomControls();

        BorderPane root = new BorderPane();
        root.setTop(topControls);
        root.setCenter(tableView);
        root.setBottom(bottomControls);

        return new Scene(root, 800, 600);
    }

    private HBox createTopControls(Stage primaryStage) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select Music Folder");

        Button selectFolderButton = new Button();
        selectFolderButton.setGraphic(new ImageView(getClass().getResource("/folder-icon.png").toString()));
        selectFolderButton.setOnAction(e -> {
            File selectedDirectory = directoryChooser.showDialog(primaryStage);
            if (selectedDirectory != null) {
                controller.loadSongs(selectedDirectory);
            }
        });

        Label folderLabel = new Label("MusicFolder");

        HBox topControls = new HBox(10, selectFolderButton, folderLabel);
        topControls.setStyle("-fx-padding: 10; -fx-alignment: center;");
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

        // Use a boolean flag to track the current state (playing or paused)
        AtomicBoolean isPlaying = new AtomicBoolean(false);

        // Set the initial button state and action
        playPauseButton.setOnAction(event -> {
            if (isPlaying.get()) {
                controller.pause();
                playPauseButton.setText("⏵"); // Change button text to play icon
            } else {
                controller.play();
                playPauseButton.setText("⏸"); // Change button text to pause icon
            }
            // Toggle the flag
            isPlaying.set(!isPlaying.get());
        });

        return playPauseButton;
    }
    private HBox createBottomControls() {
        Button playPauseButton = createPlayPauseButton();
        Button nextButton = new Button("⏭");
        Button previousButton = new Button("⏮");
        Button shuffleButton = new Button("🔀");

        // Set actions for other buttons (next, previous, shuffle)
        nextButton.setOnAction(event -> controller.next());
        previousButton.setOnAction(event -> controller.previous());
        shuffleButton.setOnAction(event -> controller.shuffle());
        Slider volumeSlider = new Slider();
        volumeSlider.setValue(50); // Set default volume to 50%
        volumeSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            controller.setVolume(newVal.doubleValue() / 100);
        });

        HBox bottomControls = new HBox(10, previousButton, playPauseButton, nextButton, shuffleButton, volumeSlider);
        bottomControls.setStyle("-fx-padding: 10; -fx-alignment: center;");

        return bottomControls;
    }
}
