package de.schachtnerth.singmysongs;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import androidx.appcompat.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import de.schachtnerth.singmysongs.data.AppDatabase;
import de.schachtnerth.singmysongs.data.DatabaseClient;
import de.schachtnerth.singmysongs.data.Song;

public class MainActivity extends AppCompatActivity {
    //region Options Menu
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_import) {
            openFilePicker();
            return true;
        }
        if ( item.getItemId() == R.id.action_add) {
            Intent intent = new Intent(MainActivity.this, AddSongActivity.class);
            startActivity(intent);
            return true;
        }
        if (item.getItemId() == R.id.action_export) {
            exportSongs();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                filterSongs(s);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                filterSongs(s);
                return true;
            }
        });
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 100 && resultCode == RESULT_OK) {
            Uri uri = data.getData();
            importSongsFromFile(uri);
        }
    }
    //endregion

    @SuppressLint("NotifyDataSetChanged")
    private void importSongsFromFile(Uri uri) {
            try {
                InputStream inputStream = getContentResolver().openInputStream(uri);
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                String currentTitle = null;
                StringBuilder currentText = new StringBuilder();
                int importedCount = 0;

                AppDatabase db = DatabaseClient.getInstance(this).getAppDatabase();

                while ((line = reader.readLine()) != null) {

                    if (line.startsWith("TITLE:")) {
                        currentTitle = line.replace("TITLE:", "").trim();
                        currentText = new StringBuilder();
                    }
                    else if (line.equals("---")) {
                        if (checkAndInsertSong(currentTitle, db, currentText) == true)
                            importedCount++;
                    }
                    else {
                        currentText.append(line).append("\n");
                    }
                }

                // falls am Ende der Datei der Marker --- fehlt, müssen wir am Schluss nochmal
                // schauen, ob noch ein Lied vorhanden ist.
                if (checkAndInsertSong(currentTitle, db, currentText))
                    importedCount++;

                reader.close();

                Toast.makeText(this,
                        getResources().getQuantityString(R.plurals.import_success,
                                importedCount,
                                importedCount),
                        Toast.LENGTH_SHORT).show();

                songList.clear();
                songList.addAll(db.songDao().getAllSongs());
                adapter.notifyDataSetChanged();

            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Fehler beim Import", Toast.LENGTH_SHORT).show();
            }
    }

    private void exportSongs() {

        List<Song> songs = db.songDao().getAllSongs();

        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < songs.size(); i++) {
            Song s = songs.get(i);

            builder.append("TITLE: ")
                    .append(s.title)
                    .append("\n");

            builder.append(s.text)
                    .append("\n");

            // Trennzeichen nur zwischen Songs
            if (i < songs.size() - 1) {
                builder.append("\n---\n");
            }
        }

        try {
            File file = new File(getExternalFilesDir(null), "songs.txt");
            FileWriter writer = new FileWriter(file);

            writer.write(builder.toString());
            writer.close();

            Toast.makeText(this, "Export erfolgreich", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Export fehlgeschlagen", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean checkAndInsertSong(
            String currentTitle,
            AppDatabase db,
            StringBuilder currentText) {
        if (currentTitle != null) { // es ist noch ein Song in der Datei zum Einlesen
            Song existing = db.songDao().findByTitle(currentTitle); // Ist der Song schon da?
            if (existing == null) { // nein, dann neu eintragen
                Song song = new Song(currentTitle, currentText.toString());
                db.songDao().insert(song);
                return true;
            } else { // ja, schon da, dann nicht eintragen, um Duplikate zu vermeiden
                Toast.makeText(
                        this,
                        getString(R.string.song_exists, currentTitle),
                        Toast.LENGTH_LONG).show();
                return false;
            }
        }
        return false; // Es ist kein Song mehr vorhanden
    }


    private ListView listView;
    private List<Song> songList;
    private SongAdapter adapter;
    private AppDatabase db;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        db = DatabaseClient.getInstance(this).getAppDatabase();
        songList = db.songDao().getAllSongs();


        adapter = new SongAdapter(songList, song -> {
            Intent intent = new Intent(MainActivity.this, SongActivity.class);
            intent.putExtra("title", song.title);
            intent.putExtra("text", song.text);
            startActivity(intent);
        }, new SongAdapter.OnItemContextMenuClickListener() {
            @Override
            public void onContextMenuEdit(Song song) {
                Intent intent = new Intent(MainActivity.this, EditSongActivity.class);
                intent.putExtra("id", song.id);
                intent.putExtra("title", song.title);
                intent.putExtra("text", song.text);
                startActivity(intent);
            }

            @Override
            public void onContextMenuDelete(Song song) {
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Lied löschen")
                        .setMessage("Möchten Sie '" + song.title + "' wirklich löschen?")
                        .setPositiveButton("Löschen", (dialog, which) -> {
                            db.songDao().deleteSong(song);
                            songList.remove(song);
                            adapter.notifyDataSetChanged();
                        })
                        .setNegativeButton("Abbrechen", null)
                        .show();
            }
        });

        recyclerView.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        songList = db.songDao().getAllSongs();
        adapter.updateList(songList);
}



private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType(getString(R.string.type_str));
        startActivityForResult(intent, 100);
    }

    private void filterSongs(String query) {
        List<Song> filtered = db.songDao().search(query);
        adapter.updateList(filtered);
    }
}