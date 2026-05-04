package de.schachtnerth.singmysongs.data;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Song {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private String title;
    private String text;

    @ColumnInfo(name = "zoom")
    private float zoom = 1.0f;

    public boolean isPrivate;

    public Song() {}

    public Song(String title, String lyrics) {
        this.title = title;
        this.text = lyrics;
        this.isPrivate = true;
    }

    public float getZoom() {
        return zoom;
    }

    public void setZoom(float zoom) {
        this.zoom = zoom;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

}
