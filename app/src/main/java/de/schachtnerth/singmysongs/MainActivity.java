package de.schachtnerth.singmysongs;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import androidx.appcompat.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.List;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import de.schachtnerth.singmysongs.data.AppDatabase;
import de.schachtnerth.singmysongs.data.DatabaseClient;
import de.schachtnerth.singmysongs.data.Song;

import com.google.zxing.BarcodeFormat;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import android.graphics.Bitmap;
import android.widget.ImageView;

public class MainActivity extends AppCompatActivity {

    private WebSocketClient client;
    private String currentGroupId;

    SharedPreferences prefs;

    private final Handler pingHandler = new Handler(Looper.getMainLooper());
    private Runnable pingRunnable;

    private void startPing(String groupId, WebSocketClient webSocket) {

        pingRunnable = new Runnable() {
            @Override
            public void run() {
                try {
                    JSONObject msg = new JSONObject();
                    msg.put("type", "ping");
                    msg.put("groupId", groupId);

                    webSocket.send(msg.toString());

                } catch (Exception e) {
                    e.printStackTrace();
                }

                pingHandler.postDelayed(this, 60 * 1000); // jede 60 Sekunden
            }
        };

        pingHandler.post(pingRunnable);
    }

    private void stopPing() {
        if (pingHandler != null && pingRunnable != null) {
            pingHandler.removeCallbacks(pingRunnable);
        }
    }

    private void connectToServer() {
        try {
            //URI uri = new URI("ws://rootserver.eltheim.de:8087");
            URI uri = new URI("ws://46.38.254.173:8087"); // TODO: IP-Adresse durch Hostnamen ersetzen

            client = new WebSocketClient(uri) {

                @Override
                public void onOpen(ServerHandshake handshakedata) {
                    runOnUiThread(() ->
                            Toast.makeText(MainActivity.this, "Verbunden!", Toast.LENGTH_SHORT).show()
                    );


                }

                @Override
                public void onMessage(String message) {
                    runOnUiThread(() -> {
                        Log.d("WS", message);
                        handleServerMessage(message);
                    });
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    Log.d("WS", "CLOSED: " + reason);
                }

                @Override
                public void onError(Exception ex) {
                    ex.printStackTrace();
                }
            };

            Log.d("WS", "before connect");
            client.connect();
            Log.d("WS", "after connect");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

//    private void showGroupDialog(String groupId) {
//        new AlertDialog.Builder(this)
//                .setTitle("Gruppe erstellt")
//                .setMessage("Dein Gruppen-Code:\n\n" + groupId)
//                .setPositiveButton("OK", null)
//                .show();
//    }

    private void showGroupDialog(String groupId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_group, null);
        ImageView qrImage = view.findViewById(R.id.qrImage);

        try {
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            Bitmap bitmap = barcodeEncoder.encodeBitmap(
                    groupId,
                    BarcodeFormat.QR_CODE,
                    400,
                    400
            );
            qrImage.setImageBitmap(bitmap);

        } catch (Exception e) {
            e.printStackTrace();
        }

        builder.setView(view);
        builder.setPositiveButton("OK", null);
        builder.show();
    }

    private void handleServerMessage(String message) {
        try {
            JSONObject obj = new JSONObject(message);
            String type = obj.getString("type");

            switch (type) {

                case "group_created":
                    String groupId = obj.getString("groupId");

                    currentGroupId = groupId;
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString("groupId", currentGroupId);
                    editor.apply();

                    // Optionsmenü anpassen, dass die Session nicht zweimal erzeugt werden kann
                    invalidateOptionsMenu();

                    // keepalive Ping starten
                    startPing(currentGroupId, client);

                    runOnUiThread(() ->
                            showGroupDialog(currentGroupId)
                    );
                    break;

                case "joined":
                    runOnUiThread(() ->
                            Toast.makeText(this, "Gruppe beigetreten", Toast.LENGTH_SHORT).show()
                    );
                    break;

                case "song":
                    String title = obj.getString("title");
                    String text = obj.getString("text");

                    runOnUiThread(() ->
                            Toast.makeText(this, "Song empfangen: " + title, Toast.LENGTH_SHORT).show()
                    );
                    break;

                case "error":
                    String msg = obj.getString("message");

                    runOnUiThread(() ->
                            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
                    );
                    break;

                case "group_status":
                    boolean exists = obj.getBoolean("exists");
                    if (!exists) {
                        currentGroupId = null;
                        prefs.edit().putString("groupId", null).apply();
                        stopPing();
                    } else {
                        startPing(currentGroupId, client);
                    }
                    invalidateOptionsMenu();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem createItem = menu.findItem(R.id.create_group);
        MenuItem qrItem = menu.findItem(R.id.show_group);

        boolean hasGroup = currentGroupId != null;

        if (hasGroup) {
            createItem.setVisible(false);
            qrItem.setVisible(true);
        } else {
            createItem.setVisible(true);
            qrItem.setVisible(false);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    private void clearLocalGroup() {
        prefs.edit().remove("groupId").apply();
    }

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
        if (item.getItemId() == R.id.create_group) {

            if (client == null) {
                connectToServer();
                runOnUiThread(() ->
                        Toast.makeText(MainActivity.this, "Nicht mit Server verbunden. Bitte in Kürze noch einmal versuchen.", Toast.LENGTH_SHORT).show()
                );
            } else {
                if (client.isOpen()) {
                    try {
                        JSONObject obj = new JSONObject();
                        obj.put("type", "create_group");

                        client.send(obj.toString());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    runOnUiThread(() ->
                            Toast.makeText(MainActivity.this, "Nicht verbunden", Toast.LENGTH_SHORT).show()
                    );
                }
            }
        }
        if (item.getItemId() == R.id.show_group) {
            showGroupDialog(currentGroupId);
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
                    .append(s.getTitle())
                    .append("\n");

            builder.append(s.getText())
                    .append("\n");

            // Trennzeichen nur zwischen Songs
            if (i < songs.size() - 1) {
                builder.append("\n---\n");
            }
        }

        try {
            File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "songs.txt");
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

        prefs = getSharedPreferences("app", MODE_PRIVATE);

        db = DatabaseClient.getInstance(this).getAppDatabase();
        songList = db.songDao().getAllSongs();


        adapter = new SongAdapter(songList, song -> {
            Intent intent = new Intent(MainActivity.this, SongActivity.class);
            intent.putExtra("song_id", song.getId());
            startActivity(intent);
        }, new SongAdapter.OnItemContextMenuClickListener() {
            @Override
            public void onContextMenuEdit(Song song) {
                Intent intent = new Intent(MainActivity.this, EditSongActivity.class);
                intent.putExtra("id", song.getId());
                startActivity(intent);
            }

            @Override
            public void onContextMenuDelete(Song song) {
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Lied löschen")
                        .setMessage("Möchten Sie '" + song.getTitle() + "' wirklich löschen?")
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

        currentGroupId = prefs.getString("groupId", null);

        // prüfen, ob Gruppe lokal bekannt ist und ob Gruppe am Server besteht
        if (currentGroupId != null) {
            checkGroupOnServer(currentGroupId);
        } else {
            stopPing();
            invalidateOptionsMenu();
        }


    }

    private void checkGroupOnServer(String groupId) {
        JSONObject msg = new JSONObject();
        try {
            msg.put("type", "check_group");
            msg.put("groupId", groupId);

            if (client != null && client.isOpen())
            {
                client.send(msg.toString());
            } else {
                Toast.makeText(this,"Client nicht verbunden. Könnte ein Timing-Issue sein.", Toast.LENGTH_LONG).show();
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }


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