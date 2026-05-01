package de.schachtnerth.singmysongs.data;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {Song.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract SongDao songDao();
}
