package de.schachtnerth.singmysongs;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import de.schachtnerth.singmysongs.data.Song;


public class SongAdapter extends RecyclerView.Adapter<SongAdapter.ViewHolder> {
    private List<Song> songs;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onClick(Song song);
    }

    public SongAdapter(List<Song> songs, OnItemClickListener listener) {
        this.songs = songs;
        this.listener = listener;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title;

        public ViewHolder(View view) {
            super(view);
            title = view.findViewById(R.id.title);
        }

        public void bind(Song song, OnItemClickListener listener) {
            title.setText(song.title);
            itemView.setOnClickListener(v -> listener.onClick(song));
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
        holder.bind(songs.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return songs.size();
    }

}
