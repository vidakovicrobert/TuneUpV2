package com.tuneupv2;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.images.Artwork;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.Collections;

public class Controller {
    private ObservableList<Song> songs = FXCollections.observableArrayList();
    private MediaPlayer mediaPlayer;
    private int currentSongIndex = 0;
    private Label currentlyPlayingLabel;
    private ImageView albumArtView;
    private BooleanProperty isPlayingProperty = new SimpleBooleanProperty(false);
    private SimpleDoubleProperty volume = new SimpleDoubleProperty(1); // Default volume 100%
    private StringProperty currentlyPlaying = new SimpleStringProperty("No song playing");
    private DoubleProperty currentTime = new SimpleDoubleProperty();
    private DoubleProperty totalDuration = new SimpleDoubleProperty();
    private BooleanProperty isMutedProperty = new SimpleBooleanProperty(false);
    public Controller(Label currentlyPlayingLabel) {
        this.currentlyPlayingLabel = currentlyPlayingLabel;
    }

    public ObservableList<Song> getSongs() {
        return songs;
    }

    public void loadSongs(File folder) {
        songs.clear();
        for (File file : folder.listFiles()) {
            if (file.getName().endsWith(".mp3")) {
                try {
                    AudioFile audioFile = AudioFileIO.read(file);
                    Tag tag = audioFile.getTag();
                    String songName = tag.getFirst(FieldKey.TITLE);
                    String artistName = tag.getFirst(FieldKey.ARTIST);
                    String album = tag.getFirst(FieldKey.ALBUM);
                    String length = String.valueOf(audioFile.getAudioHeader().getTrackLength());

                    Image albumArt = extractAlbumArt(file);

                    Song song = new Song(
                            file.getName(), // Assuming file name as ID
                            artistName,
                            songName,
                            length,
                            length, // Assuming duration is the same as length
                            album,
                            file.toURI().toString(),
                            albumArt
                    );

                    if (!songs.isEmpty()) {
                        currentlyPlaying.set("No song playing");
                    }
                    songs.add(song);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        if (!songs.isEmpty()) {
            playSong(songs.get(currentSongIndex));
        }
    }

    public void playSong(Song song) {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
        }
        Media media = new Media(song.getUrl());
        mediaPlayer = new MediaPlayer(media);
        mediaPlayer.setVolume(volume.get()); // Set the volume to the stored value
        mediaPlayer.play();
        updateNowPlayingInfo(song);
        isPlayingProperty.set(true);

        // Concatenate song name and artist name
        currentlyPlaying.set(song.getSongName() + "\nby " + song.getArtistName());

        // Bind mediaPlayer's current time to currentTime property
        mediaPlayer.currentTimeProperty().addListener((obs, oldTime, newTime) ->
                currentTime.set(newTime.toSeconds()));
        mediaPlayer.setOnReady(() -> totalDuration.set(mediaPlayer.getTotalDuration().toSeconds()));

        // Set the onEndOfMedia event handler to play the next song
        mediaPlayer.setOnEndOfMedia(this::next);

        // Mute state
        mediaPlayer.muteProperty().bind(isMutedProperty);
    }

    private void updateNowPlayingInfo(Song song) {
        currentlyPlayingLabel.setText("Now playing: " + song.getSongName() + "by " + song.getArtistName());
        System.out.println("Now playing: " + song.getSongName() + " by " + song.getArtistName()); // Debug statement
        if (albumArtView != null) {
            albumArtView.setImage(song.getAlbumArt());
        }
    }

    public void play() {
        if (mediaPlayer != null) {
            mediaPlayer.play();
            isPlayingProperty.set(true);
        }
    }

    public void pause() {
        if (mediaPlayer != null) {
            mediaPlayer.pause();
            isPlayingProperty.set(false);
        }
    }

    public void next() {
        if (!songs.isEmpty()) {
            currentSongIndex = (currentSongIndex + 1) % songs.size();
            playSong(songs.get(currentSongIndex));
        }
    }

    public void previous() {
        if (!songs.isEmpty()) {
            currentSongIndex = (currentSongIndex - 1 + songs.size()) % songs.size();
            playSong(songs.get(currentSongIndex));
        }
    }

    public void shuffle() {
        if (!songs.isEmpty()) {
            Collections.shuffle(songs);
            currentSongIndex = 0;
            playSong(songs.get(currentSongIndex));
        }
    }

    public StringProperty currentlyPlayingProperty() {
        currentlyPlaying.addListener((obs, oldVal, newVal) -> {
            System.out.println("Currently playing updated: " + newVal); // Debug statement
        });
        return currentlyPlaying;
    }

    private Slider createVolumeSlider() {
        Slider volumeSlider = new Slider(0, 100, volume.get() * 100); // Set default value to the stored volume

        // Add listener to update the volume when slider value changes
        volumeSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            volume.set(newVal.doubleValue() / 100); // Update the stored volume
            if (mediaPlayer != null) {
                mediaPlayer.setVolume(volume.get());
            }
        });

        return volumeSlider;
    }

    public Slider getVolumeSlider() {
        Slider volumeSlider = new Slider(0, 1, 0.5);
        volumeSlider.valueProperty().bindBidirectional(volumeProperty());
        volumeSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (mediaPlayer != null) {
                mediaPlayer.setVolume(newValue.doubleValue());
            }
        });
        return volumeSlider;
    }

    public BooleanProperty isPlayingProperty() {
        return isPlayingProperty;
    }

    public DoubleProperty currentTimeProperty() {
        return currentTime;
    }

    public DoubleProperty totalDurationProperty() {
        return totalDuration;
    }

    public void seek(double seconds) {
        if (mediaPlayer != null) {
            mediaPlayer.seek(Duration.seconds(seconds));
        }
    }

    private Image extractAlbumArt(File file) {
        try {
            AudioFile audioFile = AudioFileIO.read(file);
            Tag tag = audioFile.getTag();
            Artwork artwork = tag.getFirstArtwork();
            if (artwork != null) {
                byte[] imageData = artwork.getBinaryData();
                return new Image(new ByteArrayInputStream(imageData));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Return default image if no artwork found
        return new Image(getClass().getResource("/albumArt2.png").toString());
    }

    public DoubleProperty volumeProperty() {
        return volume;
    }

    public BooleanProperty isMutedProperty() {
        return isMutedProperty;
    }

    public void toggleMute() {
        isMutedProperty.set(!isMutedProperty.get());
    }
}
