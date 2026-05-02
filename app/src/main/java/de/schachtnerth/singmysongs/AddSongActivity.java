package de.schachtnerth.singmysongs;

import android.os.Bundle;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import de.schachtnerth.singmysongs.data.AppDatabase;
import de.schachtnerth.singmysongs.data.DatabaseClient;
import de.schachtnerth.singmysongs.data.Song;

public class AddSongActivity extends AppCompatActivity {

    EditText editTitle, editText;
    AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_song);

        editTitle = findViewById(R.id.editTitle);
        editText = findViewById(R.id.editText);

        db = DatabaseClient.getInstance(this).getAppDatabase();

        findViewById(R.id.btnSave).setOnClickListener(v -> saveSong());
    }

    private void saveSong() {
        Song song = new Song();
        song.title = editTitle.getText().toString();
        song.text = editText.getText().toString();

        db.songDao().insert(song);

        finish(); // zurück zur Liste
    }
}