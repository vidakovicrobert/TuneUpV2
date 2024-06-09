package com.tuneupv2;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.ImageView;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;

import java.io.File;
import java.util.Collections;

public class Controller {
    private ObservableList<Song> songs = FXCollections.observableArrayList();
    private MediaPlayer mediaPlayer;
    private int currentSongIndex = 0;
    private Label currentlyPlayingLabel;
    private ImageView albumArtView;
    private BooleanProperty isPlayingProperty = new SimpleBooleanProperty(false);
    private SimpleDoubleProperty volume = new SimpleDoubleProperty(0.25); // Default volume 25%
    private StringProperty currentlyPlaying = new SimpleStringProperty("No song playing");

    public Controller(Label currentlyPlayingLabel, ImageView albumArtView) {
        this.currentlyPlayingLabel = currentlyPlayingLabel;
        this.albumArtView = albumArtView;
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

                    Song song = new Song(
                            file.getName(), // Assuming file name as ID
                            artistName,
                            songName,
                            length,
                            length, // Assuming duration is the same as length
                            album,
                            file.toURI().toString()
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
        currentlyPlaying.set(song.getSongName());
    }

    private void updateNowPlayingInfo(Song song) {
        currentlyPlayingLabel.setText("Now playing: " + song.getSongName());
        if (song.getImage() != null) {
            albumArtView.setImage(song.getImage());
        } else {
            albumArtView.setImage(null); // Clear image if not available
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
        return createVolumeSlider();
    }

    public BooleanProperty isPlayingProperty() {
        return isPlayingProperty;
    }

}
