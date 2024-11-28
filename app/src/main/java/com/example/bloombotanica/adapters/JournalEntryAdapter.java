package com.example.bloombotanica.adapters;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bloombotanica.R;
import com.example.bloombotanica.models.JournalEntry;
import com.example.bloombotanica.ui.LogDetailActivity;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class JournalEntryAdapter extends RecyclerView.Adapter<JournalEntryAdapter.ViewHolder> {
    private List<JournalEntry> entries;
    private onLogLongClickListener longClickListener;

    public interface onLogLongClickListener {
        void onLogLongClick(View view, int position);
    }

    public JournalEntryAdapter(List<JournalEntry> entries, onLogLongClickListener longClickListener) {
        this.entries = entries;
        this.longClickListener = longClickListener;
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
            holder.title.setText(entry.getTitle());
        } else {
            holder.title.setText(entry.getTitle());
        }
        holder.itemView.setOnClickListener(v -> {
            // Handle item click
            Intent intent = new Intent(v.getContext(), LogDetailActivity.class);
            intent.putExtra("entryId", entry.getId());
            intent.putExtra("plantId", entry.getPlantId());
            v.getContext().startActivity(intent);
        });

        holder.itemView.setOnLongClickListener(v -> {
            if(longClickListener != null) {
                longClickListener.onLogLongClick(v, position);
            }
            return true;
        });
    }


    @Override
    public int getItemCount() {
        return entries.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView timestamp, title;

        public ViewHolder(View view) {
            super(view);
            timestamp = view.findViewById(R.id.journal_entry_timestamp);
            title = view.findViewById(R.id.journal_entry_title);
        }
    }
}
