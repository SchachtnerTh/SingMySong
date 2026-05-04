package de.schachtnerth.singmysongs;

import android.content.Intent;
import android.os.Bundle;
import android.view.ScaleGestureDetector;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import de.schachtnerth.singmysongs.data.AppDatabase;
import de.schachtnerth.singmysongs.data.DatabaseClient;
import de.schachtnerth.singmysongs.data.Song;

public class SongActivity extends AppCompatActivity {

    private float scaleFactor = 16f;
    private ScaleGestureDetector scaleDetector;
    private TextView text;

    private Song song;
    private AppDatabase db;

    @Override
    protected void onPause() {
        super.onPause();

        new Thread(() -> {
            song.setZoom(scaleFactor);
            db.songDao().updateSong(song);
        }).start();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_song);

        TextView title = findViewById(R.id.title);
        text = findViewById(R.id.text);

        Intent intent = getIntent();

        int songId = intent.getIntExtra("song_id", -1);
        db = DatabaseClient.getInstance(this).getAppDatabase();

        new Thread(() -> {
            song = db.songDao().getSongById(songId);

            runOnUiThread(() -> {
                title.setText(song.getTitle());
                text.setText(song.getText());
                scaleFactor = song.getZoom();
                text.setTextSize(16 * scaleFactor);
            });
        }).start();

        title.setText(intent.getStringExtra("title"));

        text.setOnTouchListener((v, event) -> {
                    scaleDetector.onTouchEvent(event);
                    return true;
                });

        text.setText(intent.getStringExtra("text"));

        scaleDetector = new ScaleGestureDetector(this,
                new ScaleGestureDetector.SimpleOnScaleGestureListener() {
                    @Override
                    public boolean onScale(@NonNull ScaleGestureDetector detector) {
                        scaleFactor *= detector.getScaleFactor();

                        // Begrenzen (sonst wird es unbenutzbar)
                        scaleFactor = Math.max(0.5f, Math.min(scaleFactor, 3.0f));

                        text.setTextSize(16 * scaleFactor);
                        return true;
                    }
                });
    }
}