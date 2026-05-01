package de.schachtnerth.singmysongs.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface SongDao {

    @Insert
    void insert(Song song);

    @Query("SELECT * FROM Song")
    List<Song> getAllSongs();

    @Query("SELECT * FROM Song WHERE title LIKE '%' || :search || '%'")
    List<Song> search(String search);
}
