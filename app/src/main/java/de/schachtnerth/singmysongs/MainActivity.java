package de.schachtnerth.singmysongs;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import de.schachtnerth.singmysongs.data.AppDatabase;
import de.schachtnerth.singmysongs.data.DatabaseClient;
import de.schachtnerth.singmysongs.data.Song;

public class MainActivity extends AppCompatActivity {

    private ListView listView;
    private ArrayList<Song> songList;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        AppDatabase db = DatabaseClient.getInstance(this);

        Song s = new Song();
        s.title = "Alle meine Entchen";
        s.text = "Alle meine Entchen\nschwimmen auf dem See\nschwimmen auf dem See\nKöpfchen in das" +
                " Wasser\nSchwänzchen in die Höh.";
        s.isPrivate = false;
        db.songDao().insert(s);

        List<Song> songList = db.songDao().getAllSongs();


        SongAdapter adapter = new SongAdapter(songList, song -> {
            Intent intent = new Intent(MainActivity.this, SongActivity.class);
            intent.putExtra("title", song.title);
            intent.putExtra("text", song.text);
            startActivity(intent);
        });

        recyclerView.setAdapter(adapter);
    }

    private ArrayList<String> getTitles(List<Song> songs) {
        ArrayList<String> titles = new ArrayList<>();
        for (Song s : songs) {
            titles.add(s.title);
        }
        return titles;
    }
}