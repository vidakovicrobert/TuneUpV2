package com.tuneupv2;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Label;
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

                    Song song = new Song(
                            file.getName(), // Assuming file name as ID
                            artistName,
                            songName,
                            length,
                            length, // Assuming duration is the same as length
                            album,
                            file.toURI().toString()
                    );

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
        mediaPlayer.play();
        currentlyPlayingLabel.setText("Now playing: " + song.getSongName());
    }

    public void play() {
        if (mediaPlayer != null) {
            mediaPlayer.play();
        }
    }

    public void pause() {
        if (mediaPlayer != null) {
            mediaPlayer.pause();
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

    public void setVolume(double volume) {
        if (mediaPlayer != null) {
            mediaPlayer.setVolume(volume);
        }
    }

}
