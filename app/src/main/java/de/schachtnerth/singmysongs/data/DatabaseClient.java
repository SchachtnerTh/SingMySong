package de.schachtnerth.singmysongs.data;

import android.content.Context;

import androidx.room.Room;

public class DatabaseClient {

    private static AppDatabase instance;

    public static AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            "songs_db"
                    ).allowMainThreadQueries() // nur für Entwicklung!
                    .build();
        }
        return instance;
    }
}
