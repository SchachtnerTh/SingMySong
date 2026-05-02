package de.schachtnerth.singmysongs.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Song {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public String title;
    public String text;

    public boolean isPrivate;

    public Song() {}

    public Song(String title, String lyrics) {
        this.title = title;
        this.text = lyrics;
        this.isPrivate = true;
    }
}
