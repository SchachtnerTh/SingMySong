package de.schachtnerth.singmysongs;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import de.schachtnerth.singmysongs.data.AppDatabase;
import de.schachtnerth.singmysongs.data.DatabaseClient;
import de.schachtnerth.singmysongs.data.Song;

public class EditSongActivity extends AppCompatActivity {
    EditText editTitle, editText;
    AppDatabase db;
    int songId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_song);

        editTitle = findViewById(R.id.editTitle);
        editText = findViewById(R.id.editText);

        db = DatabaseClient.getInstance(this).getAppDatabase();

        Intent intent = getIntent();
        songId = intent.getIntExtra("id", -1);

        editTitle.setText(intent.getStringExtra("title"));
        editText.setText(intent.getStringExtra("text"));

        findViewById(R.id.btnSave).setOnClickListener(v -> saveSong());
    }

    private void saveSong() {
        Song song = new Song();
        song.setId(songId);
        song.setTitle(editTitle.getText().toString());
        song.setText(editText.getText().toString());

        db.songDao().updateSong(song);

        finish(); // zurück zur Liste
    }
}
