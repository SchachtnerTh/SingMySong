package de.schachtnerth.singmysongs.data;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface SongDao {

    @Insert
    void insert(Song song);

    @Query("SELECT * FROM Song")
    List<Song> getAllSongs();

    @Query("SELECT * FROM Song WHERE title LIKE '%' || :search || '%'")
    List<Song> search(String search);

    @Query("SELECT * FROM Song WHERE title = :title LIMIT 1")
    Song findByTitle(String title);

    @Delete
    void deleteSong(Song song);

    @Query("DELETE FROM Song WHERE id = :id")
    void deleteById(int id);

    @Query("SELECT * FROM Song WHERE id = :id")
    Song getSongById(int id);


    @Update
    void updateSong(Song song);
}
