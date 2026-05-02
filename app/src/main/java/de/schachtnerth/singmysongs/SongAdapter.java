package de.schachtnerth.singmysongs;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;

import androidx.appcompat.view.menu.MenuView;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import de.schachtnerth.singmysongs.data.Song;


public class SongAdapter extends RecyclerView.Adapter<SongAdapter.ViewHolder> {
    private List<Song> songs;
    private OnItemClickListener listener;
    private OnItemContextMenuClickListener contextMenuClickListener;

    public void updateList(List<Song> newList) {
        this.songs = newList;
        notifyDataSetChanged();
    }

    public interface OnItemClickListener {
        void onClick(Song song);
    }

    public interface OnItemContextMenuClickListener {
        void onContextMenuEdit(Song song);
        void onContextMenuDelete(Song song);
    }

    public SongAdapter(List<Song> songs,
                       OnItemClickListener listener,
                       OnItemContextMenuClickListener contextMenuClickListener) {
        this.songs = songs;
        this.listener = listener;
        this.contextMenuClickListener = contextMenuClickListener;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title;

        public ViewHolder(View view) {
            super(view);
            title = view.findViewById(R.id.title);
        }

        public void bind(Song song, OnItemClickListener listener, OnItemContextMenuClickListener contextMenuClickListener) {
            title.setText(song.title);
            itemView.setOnClickListener(v -> listener.onClick(song));
            itemView.setOnLongClickListener(v -> {
                PopupMenu popup = new PopupMenu(v.getContext(), v);

                popup.getMenu().add("Bearbeiten");
                popup.getMenu().add("Löschen");

                popup.setOnMenuItemClickListener(item -> {

                    if (item.getTitle().equals("Bearbeiten")) {
                        contextMenuClickListener.onContextMenuEdit(song);
                        return true;
                    }

                    if (item.getTitle().equals("Löschen")) {
                        contextMenuClickListener.onContextMenuDelete(song);
                        return true;
                    }

                    return false;
                });

                popup.show();
                return true;
            });
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_song, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.bind(songs.get(position), listener, contextMenuClickListener);
    }

    @Override
    public int getItemCount() {
        return songs.size();
    }

}
