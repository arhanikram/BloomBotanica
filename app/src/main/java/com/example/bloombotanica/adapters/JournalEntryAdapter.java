package com.example.bloombotanica.adapters;

import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bloombotanica.R;
import com.example.bloombotanica.models.JournalEntry;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class JournalEntryAdapter extends RecyclerView.Adapter<JournalEntryAdapter.ViewHolder> {
    private List<JournalEntry> entries;

    public JournalEntryAdapter(List<JournalEntry> entries) {
        this.entries = entries;
    }

    public void updateEntries(List<JournalEntry> newEntries) {
        this.entries = newEntries;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_journal_entry, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        JournalEntry entry = entries.get(position);
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        String formattedDate = sdf.format(entry.getTimestamp());
        holder.timestamp.setText(formattedDate);

        if (entry.getCareType() != null) {
            //we will clean this string up after, it works now
            holder.note.setText("Care: " + entry.getCareType());
            holder.image.setVisibility(View.GONE); // No image for care logs
        } else {
            holder.note.setText(entry.getNote());
            if (entry.getImagePath() != null) {
                holder.image.setImageBitmap(BitmapFactory.decodeFile(entry.getImagePath()));
            } else {
                holder.image.setVisibility(View.GONE);
            }
        }
    }


    @Override
    public int getItemCount() {
        return entries.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView timestamp, note;
        ImageView image;

        ViewHolder(View view) {
            super(view);
            timestamp = view.findViewById(R.id.journal_entry_timestamp);
            note = view.findViewById(R.id.journal_entry_note);
            image = view.findViewById(R.id.journal_entry_image);
        }
    }
}
