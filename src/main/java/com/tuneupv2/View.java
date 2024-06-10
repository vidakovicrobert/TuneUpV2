package com.tuneupv2;

import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import java.io.File;

public class View {
    private Controller controller;
    private Label currentlyPlayingLabel = new Label("No song playing");
    private ImageView albumArtView = new ImageView();

    public View(Controller controller) {
        this.controller = controller;
        // Set the default album art image
        albumArtView.setImage(new Image(getClass().getResource("/albumArt2.png").toString()));
    }

    public Scene createScene(Stage primaryStage) {
        HBox topControls = createTopControls(primaryStage);
        TableView<Song> tableView = createTableView();
        VBox bottomControls = createBottomControls();

        BorderPane root = new BorderPane();
        root.setTop(topControls);
        root.setCenter(tableView);
        root.setBottom(bottomControls);

        Scene scene = new Scene(root, 650, 600);

        primaryStage.setScene(scene);  // Ensure the scene is set to the primary stage

        return scene;
    }

    private HBox createTopControls(Stage primaryStage) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select the music folder");

        // Define two ImageView objects for the two icons
        // Theme buttons
        ImageView icon1 = new ImageView(getClass().getResource("/theme-icon.png").toString());
        ImageView icon2 = new ImageView(getClass().getResource("/theme-icon2.png").toString());

        ImageView folderIcon1 = new ImageView(getClass().getResource("/folder-icon.png").toString());
        ImageView folderIcon2 = new ImageView(getClass().getResource("/folder-icon2.png").toString());

        Button selectFolderButton = new Button();
        selectFolderButton.setGraphic(folderIcon1); // Set the initial icon

        // Set the initial icon for the toggle button
        ToggleButton toggleButton = new ToggleButton();
        toggleButton.setGraphic(icon1);

        // Add event handler to the button
        toggleButton.setOnAction(e -> {
            Scene scene = primaryStage.getScene();
            if (scene.getStylesheets().contains(getClass().getResource("/style.css").toExternalForm())) {
                scene.getStylesheets().remove(getClass().getResource("/style.css").toExternalForm());
            } else {
                scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
            }

            // Toggle between the two icons
            if (selectFolderButton.getGraphic() == folderIcon1) {
                selectFolderButton.setGraphic(folderIcon2);
            } else {
                selectFolderButton.setGraphic(folderIcon1);
            }

            if (toggleButton.isSelected()) {
                toggleButton.setGraphic(icon2);
            } else {
                toggleButton.setGraphic(icon1);
            }
        });

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

        HBox topControls = new HBox(5, selectFolderButton, folderSelectionBox, toggleButton);
        topControls.setStyle("-fx-padding: 5; -fx-alignment: center;");

        // Adjust the position of the buttons to be closer to the middle
        HBox.setMargin(selectFolderButton, new Insets(0, 10, 0, 10)); // Add margin to separate buttons
        HBox.setMargin(toggleButton, new Insets(0, 10, 0, 20)); // Add margin to separate buttons

        // Add constraints to position selectFolderButton on the left and toggleButton on the right
        HBox.setHgrow(folderSelectionBox, Priority.ALWAYS);
        HBox.setHgrow(selectFolderButton, Priority.NEVER);
        HBox.setHgrow(toggleButton, Priority.NEVER);

        return topControls;
    }

    private TableCell<Song, String> createCenteredCell() {
        return new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item);
                    setAlignment(Pos.CENTER); // Center the content
                }
            }
        };
    }

    private TableView<Song> createTableView() {
        TableView<Song> tableView = new TableView<>();
        tableView.setItems(controller.getSongs());

        TableColumn<Song, String> numberColumn = new TableColumn<>("#");
        numberColumn.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(controller.getSongs().indexOf(data.getValue()) + 1)));
        numberColumn.setPrefWidth(30);
        numberColumn.setCellFactory(column -> createCenteredCell());

        TableColumn<Song, String> titleColumn = new TableColumn<>("Title");
        titleColumn.setCellValueFactory(data -> data.getValue().songNameProperty());
        titleColumn.setPrefWidth(120);

        TableColumn<Song, String> artistColumn = new TableColumn<>("Artist");
        artistColumn.setCellValueFactory(data -> data.getValue().artistNameProperty());
        artistColumn.setPrefWidth(120);

        TableColumn<Song, String> albumColumn = new TableColumn<>("Album");
        albumColumn.setCellValueFactory(data -> data.getValue().formatProperty());
        albumColumn.setPrefWidth(120);

        TableColumn<Song, String> durationColumn = createDurationColumn();

        tableView.getColumns().addAll(numberColumn, titleColumn, artistColumn, albumColumn, durationColumn);
        return tableView;
    }

    private TableCell<Song, String> createFormattedDurationCell() {
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
                    setAlignment(Pos.CENTER); // Center the content
                }
            }
        };
    }

    private TableColumn<Song, String> createDurationColumn() {
        TableColumn<Song, String> durationColumn = new TableColumn<>("Duration");
        durationColumn.setCellValueFactory(data -> data.getValue().durationProperty());
        durationColumn.setPrefWidth(100);
        durationColumn.setCellFactory(column -> createFormattedDurationCell());
        return durationColumn;
    }

    private Button createPlayPauseButton() {
        Button playPauseButton = new Button("⏵");
        playPauseButton.setPrefSize(30, 30); // Set preferred size

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

    private HBox createCurrentlyPlayingBox() {
        currentlyPlayingLabel.textProperty().bind(controller.currentlyPlayingProperty());
        currentlyPlayingLabel.setWrapText(true); // Enable text wrapping for multiline text
        albumArtView.setFitWidth(60);
        albumArtView.setFitHeight(60);
        albumArtView.setPreserveRatio(true); // Preserve aspect ratio
        albumArtView.setSmooth(true); // Enable smooth scaling

        // Create an HBox to hold the album art view and label
        HBox albumArtLabelBox = new HBox(10, albumArtView, currentlyPlayingLabel);
        albumArtLabelBox.setAlignment(Pos.CENTER_LEFT); // Align items to the left

        return albumArtLabelBox;
    }

    private HBox createControlButtons() {
        Button playPauseButton = createPlayPauseButton();
        playPauseButton.setPrefSize(30, 30); // Set preferred size

        Button nextButton = new Button("⏭");
        nextButton.setPrefSize(30, 30); // Set preferred size

        Button previousButton = new Button("⏮");
        previousButton.setPrefSize(30, 30); // Set preferred size

        Button shuffleButton = new Button("🔀");
        shuffleButton.setPrefSize(30, 30); // Set preferred size

        // Set actions for other buttons (next, previous, shuffle)
        nextButton.setOnAction(event -> controller.next());
        previousButton.setOnAction(event -> controller.previous());
        shuffleButton.setOnAction(event -> controller.shuffle());

        // Create the volume slider
        Slider volumeSlider = controller.getVolumeSlider();
        volumeSlider.setPrefWidth(100); // Set preferred width for the volume slider

        // Create the mute button
        Button muteButton = new Button("🔊");
        muteButton.setPrefSize(30, 30); // Set preferred size
        muteButton.setOnAction(event -> controller.toggleMute());

        // Update the mute button text when the mute state changes
        controller.isMutedProperty().addListener((obs, wasMuted, isNowMuted) -> {
            if (isNowMuted) {
                muteButton.setText("🔇"); // Change button text to mute icon
            } else {
                muteButton.setText("🔊"); // Change button text to unmute icon
            }
        });

        HBox controlButtons = new HBox(10, previousButton, playPauseButton, nextButton, shuffleButton, muteButton, volumeSlider);
        controlButtons.setAlignment(Pos.CENTER_RIGHT); // Align items to the right

        return controlButtons;
    }

    private VBox createBottomControls() {
        HBox currentlyPlayingBox = createCurrentlyPlayingBox();
        HBox controlButtons = createControlButtons();
        Slider progressBar = createProgressBar();

        // Create a wrapper HBox to align currentlyPlayingBox to the left and controlButtons to the right
        HBox bottomHBox = new HBox(currentlyPlayingBox, controlButtons);
        HBox.setHgrow(controlButtons, Priority.ALWAYS); // Make control buttons take available space to the right
        bottomHBox.setAlignment(Pos.CENTER_LEFT);
        bottomHBox.setStyle("-fx-padding: 10;");

        VBox bottomControls = new VBox(bottomHBox);
        bottomControls.setStyle("-fx-padding: 15; -fx-alignment: center;");

        // Create a separate HBox for the progressBar to control its width independently
        HBox progressBarBox = new HBox(progressBar);
        progressBarBox.setAlignment(Pos.CENTER_LEFT);
        progressBarBox.setStyle("-fx-padding: 10; -fx-alignment: center;");

        // Create labels for time elapsed and total duration
        Label timeElapsedLabel = new Label();
        Label totalDurationLabel = new Label();

        // Bind the labels to the current time and total duration properties
        timeElapsedLabel.textProperty().bind(controller.currentTimeProperty().asString("%.0f").concat(" s"));
        totalDurationLabel.textProperty().bind(controller.totalDurationProperty().asString("%.0f").concat(" s"));

        // Convert seconds to minutes:seconds format
        timeElapsedLabel.textProperty().bind(Bindings.createStringBinding(() -> {
            int totalSeconds = (int) controller.currentTimeProperty().get();
            int minutes = totalSeconds / 60;
            int seconds = totalSeconds % 60;
            return String.format("%02d:%02d", minutes, seconds);
        }, controller.currentTimeProperty()));

        totalDurationLabel.textProperty().bind(Bindings.createStringBinding(() -> {
            int totalSeconds = (int) controller.totalDurationProperty().get();
            int minutes = totalSeconds / 60;
            int seconds = totalSeconds % 60;
            return String.format("%02d:%02d", minutes, seconds);
        }, controller.totalDurationProperty()));

        // Create an HBox to hold the time labels and progress bar
        HBox timeBox = new HBox(10, timeElapsedLabel, progressBar, totalDurationLabel);
        timeBox.setAlignment(Pos.CENTER);
        timeBox.setStyle("-fx-padding: 10; -fx-alignment: center;");

        bottomControls.getChildren().add(timeBox);

        return bottomControls;
    }

    private Slider createProgressBar() {
        Slider progressBar = new Slider();
        progressBar.setPrefWidth(400); // Set preferred width for the progress bar
        progressBar.setMax(100); // Set maximum value (this will represent 100% of the song's duration)

        // Bind the slider's value to the current position of the song
        controller.currentTimeProperty().addListener((obs, oldTime, newTime) -> {
            if (!progressBar.isValueChanging()) { // If user is not dragging the slider
                double total = controller.totalDurationProperty().get();
                if (total > 0) {
                    progressBar.setValue(newTime.doubleValue() / total * 100);
                }
            }
        });

        // Allow the user to seek by dragging the slider
        progressBar.valueChangingProperty().addListener((obs, wasChanging, isChanging) -> {
            if (!isChanging) { // When user finishes dragging the slider
                double total = controller.totalDurationProperty().get();
                if (total > 0) {
                    controller.seek(progressBar.getValue() / 100 * total);
                }
            }
        });

        progressBar.setOnMouseReleased(event -> { // When user clicks on the slider
            double total = controller.totalDurationProperty().get();
            if (total > 0) {
                controller.seek(progressBar.getValue() / 100 * total);
            }
        });

        return progressBar;
    }
}
