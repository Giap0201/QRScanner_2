package com.example.qrscanner_ver2.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.qrscanner_ver2.R;
import com.example.qrscanner_ver2.model.HistoryItem;
import com.example.qrscanner_ver2.model.QrType;
import com.example.qrscanner_ver2.utils.QrParser;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * RecyclerView adapter for the history list.
 */
public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {

    public interface OnItemClickListener { void onClick(HistoryItem item); }
    public interface OnItemLongClickListener { void onLongClick(HistoryItem item); }

    private List<HistoryItem> items;
    private final OnItemClickListener clickListener;
    private final OnItemLongClickListener longClickListener;
    private final SimpleDateFormat sdf =
            new SimpleDateFormat("dd/MM/yyyy  HH:mm", Locale.getDefault());

    public HistoryAdapter(List<HistoryItem> items,
                          OnItemClickListener clickListener,
                          OnItemLongClickListener longClickListener) {
        this.items = items;
        this.clickListener = clickListener;
        this.longClickListener = longClickListener;
    }

    public void updateItems(List<HistoryItem> newItems) {
        this.items = newItems;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        HistoryItem item = items.get(position);
        QrType type = QrType.valueOf(item.getType());

        // Icon + type label
        holder.tvIcon.setText(QrParser.getIcon(type));
        holder.tvType.setText(QrParser.getLabel(type));

        // Content preview (max 60 chars)
        String preview = item.getContent();
        if (preview.length() > 60) preview = preview.substring(0, 57) + "…";
        holder.tvContent.setText(preview);

        // Timestamp
        holder.tvTime.setText(sdf.format(new Date(item.getTimestamp())));

        // Type badge background color
        int color = getColorForType(holder, type);
        holder.tvType.setBackgroundColor(color);

        holder.itemView.setOnClickListener(v -> clickListener.onClick(item));
        holder.itemView.setOnLongClickListener(v -> {
            longClickListener.onLongClick(item);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    private int getColorForType(ViewHolder holder, QrType type) {
        int colorRes;
        switch (type) {
            case URL:      colorRes = R.color.color_url; break;
            case EMAIL:    colorRes = R.color.color_email; break;
            case PHONE:    colorRes = R.color.color_phone; break;
            case LOCATION: colorRes = R.color.color_location; break;
            case CONTACT:  colorRes = R.color.color_contact; break;
            default:       colorRes = R.color.color_text; break;
        }
        return holder.itemView.getContext().getResources().getColor(colorRes, null);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvIcon, tvType, tvContent, tvTime;

        ViewHolder(View view) {
            super(view);
            tvIcon    = view.findViewById(R.id.tvHistoryIcon);
            tvType    = view.findViewById(R.id.tvHistoryType);
            tvContent = view.findViewById(R.id.tvHistoryContent);
            tvTime    = view.findViewById(R.id.tvHistoryTime);
        }
    }
}
