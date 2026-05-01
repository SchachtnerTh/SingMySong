package de.schachtnerth.singmysongs;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class SongActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_song);

        TextView title = findViewById(R.id.title);
        TextView text = findViewById(R.id.text);

        Intent intent = getIntent();

        title.setText(intent.getStringExtra("title"));
        text.setText(intent.getStringExtra("text"));
    }
}