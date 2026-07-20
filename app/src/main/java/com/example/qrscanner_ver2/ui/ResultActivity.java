package com.example.qrscanner_ver2.ui;

import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import com.example.qrscanner_ver2.R;
import com.example.qrscanner_ver2.db.DBHelper;
import com.example.qrscanner_ver2.model.HistoryItem;
import com.example.qrscanner_ver2.model.QrType;
import com.example.qrscanner_ver2.utils.IntentHelper;
import com.example.qrscanner_ver2.utils.QrParser;

/**
 * Displays the decoded QR content, type badge, and context-appropriate action buttons.
 * Automatically saves the result to SQLite history when first opened from a scan.
 */
public class ResultActivity extends AppCompatActivity {

    public static final String EXTRA_CONTENT = "extra_content";
    /** Pass true to skip saving (e.g. when opened from History). */
    public static final String EXTRA_FROM_HISTORY = "extra_from_history";

    private String content;
    private QrType type;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        // Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.result_title);
        }

        content = getIntent().getStringExtra(EXTRA_CONTENT);
        boolean fromHistory = getIntent().getBooleanExtra(EXTRA_FROM_HISTORY, false);

        if (content == null || content.isEmpty()) {
            finish();
            return;
        }

        type = QrParser.parse(content);

        bindViews();
        setupActionButtons();

        // Save to history only when coming from a fresh scan
        if (!fromHistory) {
            saveToHistory();
        }
    }

    // ─── View Binding ────────────────────────────────────────────────────────

    private void bindViews() {
        // Content text
        TextView tvContent = findViewById(R.id.tvContent);
        tvContent.setText(content);

        // Type badge
        TextView tvType = findViewById(R.id.tvTypeBadge);
        tvType.setText(QrParser.getIcon(type) + "  " + QrParser.getLabel(type));
        tvType.setBackgroundColor(getTypeColor(type));
    }

    private void setupActionButtons() {
        // Hide all action buttons first
        Button btnPrimary = findViewById(R.id.btnPrimaryAction);
        Button btnCopy = findViewById(R.id.btnCopy);
        Button btnShare = findViewById(R.id.btnShare);

        // Always show copy & share
        btnCopy.setOnClickListener(v -> IntentHelper.copyToClipboard(this, content));
        btnShare.setOnClickListener(v -> IntentHelper.shareText(this, content));

        // Primary action depends on type
        switch (type) {
            case URL:
                btnPrimary.setText(R.string.btn_open_url);
                btnPrimary.setVisibility(View.VISIBLE);
                btnPrimary.setOnClickListener(v -> IntentHelper.openUrl(this, content));
                break;
            case EMAIL:
                btnPrimary.setText(R.string.btn_open_email);
                btnPrimary.setVisibility(View.VISIBLE);
                btnPrimary.setOnClickListener(v -> IntentHelper.openEmail(this, content));
                break;
            case PHONE:
                btnPrimary.setText(R.string.btn_call);
                btnPrimary.setVisibility(View.VISIBLE);
                btnPrimary.setOnClickListener(v -> IntentHelper.openPhone(this, content));
                break;
            case LOCATION:
                btnPrimary.setText(R.string.btn_open_map);
                btnPrimary.setVisibility(View.VISIBLE);
                btnPrimary.setOnClickListener(v -> IntentHelper.openMaps(this, content));
                break;
            case CONTACT:
                btnPrimary.setText(R.string.btn_add_contact);
                btnPrimary.setVisibility(View.VISIBLE);
                btnPrimary.setOnClickListener(v -> IntentHelper.addContact(this, content));
                break;
            case TEXT:
            default:
                btnPrimary.setVisibility(View.GONE);
                break;
        }
    }

    // ─── Persistence ─────────────────────────────────────────────────────────

    private void saveToHistory() {
        HistoryItem item = new HistoryItem(content, type.name(), System.currentTimeMillis());
        DBHelper.getInstance(this).insertHistory(item);
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private int getTypeColor(QrType type) {
        switch (type) {
            case URL:      return getResources().getColor(R.color.color_url, getTheme());
            case EMAIL:    return getResources().getColor(R.color.color_email, getTheme());
            case PHONE:    return getResources().getColor(R.color.color_phone, getTheme());
            case LOCATION: return getResources().getColor(R.color.color_location, getTheme());
            case CONTACT:  return getResources().getColor(R.color.color_contact, getTheme());
            default:       return getResources().getColor(R.color.color_text, getTheme());
        }
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
