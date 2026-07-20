package com.example.qrscanner_ver2.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.qrscanner_ver2.R;
import com.example.qrscanner_ver2.db.DBHelper;
import com.example.qrscanner_ver2.model.HistoryItem;

import java.util.List;

/**
 * Shows all saved QR scan/generate entries from SQLite.
 * Tap to open ResultActivity; long-press to delete.
 */
public class HistoryActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TextView tvEmpty;
    private HistoryAdapter adapter;
    private List<HistoryItem> items;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        // Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.history_title);
        }

        recyclerView = findViewById(R.id.recyclerHistory);
        tvEmpty = findViewById(R.id.tvEmpty);

        loadHistory();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh list when returning from ResultActivity
        loadHistory();
    }

    // ─── Load Data ───────────────────────────────────────────────────────────

    private void loadHistory() {
        items = DBHelper.getInstance(this).getAllHistory();

        if (items.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            tvEmpty.setVisibility(View.VISIBLE);
        } else {
            tvEmpty.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }

        if (adapter == null) {
            adapter = new HistoryAdapter(items,
                    this::onItemClick,
                    this::onItemLongClick);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            recyclerView.setAdapter(adapter);
        } else {
            adapter.updateItems(items);
        }
    }

    // ─── Callbacks ───────────────────────────────────────────────────────────

    private void onItemClick(HistoryItem item) {
        Intent intent = new Intent(this, ResultActivity.class);
        intent.putExtra(ResultActivity.EXTRA_CONTENT, item.getContent());
        intent.putExtra(ResultActivity.EXTRA_FROM_HISTORY, true);
        startActivity(intent);
    }

    private void onItemLongClick(HistoryItem item) {
        new AlertDialog.Builder(this)
                .setMessage(R.string.delete_confirm)
                .setPositiveButton(R.string.delete, (d, w) -> {
                    DBHelper.getInstance(this).deleteById(item.getId());
                    loadHistory();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
