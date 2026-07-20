package com.example.qrscanner_ver2.ui;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.qrscanner_ver2.R;
import com.example.qrscanner_ver2.utils.IntentHelper;
import com.example.qrscanner_ver2.utils.QrGenerator;

import java.io.File;

/**
 * Lets the user type any text and generate a QR code Bitmap.
 * Supports saving to the gallery and sharing via FileProvider.
 */
public class GenerateActivity extends AppCompatActivity {

    private EditText etInput;
    private ImageView ivQrCode;
    private Button btnSave, btnShare;
    private Bitmap currentBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generate);

        // Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.generate_title);
        }

        etInput = findViewById(R.id.etInput);
        ivQrCode = findViewById(R.id.ivQrCode);
        btnSave = findViewById(R.id.btnSaveQr);
        btnShare = findViewById(R.id.btnShareQr);

        // Generate button
        findViewById(R.id.btnGenerate).setOnClickListener(v -> generateQr());

        // Save button
        btnSave.setOnClickListener(v -> saveQr());

        // Share button
        btnShare.setOnClickListener(v -> shareQr());
    }

    // ─── Generate ────────────────────────────────────────────────────────────

    private void generateQr() {
        String text = etInput.getText().toString().trim();
        if (TextUtils.isEmpty(text)) {
            Toast.makeText(this, R.string.enter_text_first, Toast.LENGTH_SHORT).show();
            return;
        }

        currentBitmap = QrGenerator.generate(text, 800);
        if (currentBitmap != null) {
            ivQrCode.setImageBitmap(currentBitmap);
            ivQrCode.setVisibility(View.VISIBLE);
            btnSave.setVisibility(View.VISIBLE);
            btnShare.setVisibility(View.VISIBLE);
        } else {
            Toast.makeText(this, R.string.error_decode_failed, Toast.LENGTH_SHORT).show();
        }
    }

    // ─── Save ────────────────────────────────────────────────────────────────

    private void saveQr() {
        if (currentBitmap == null) return;
        File saved = QrGenerator.saveBitmapToGallery(this, currentBitmap);
        if (saved != null) {
            Toast.makeText(this, R.string.qr_saved_success, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, R.string.qr_save_failed, Toast.LENGTH_SHORT).show();
        }
    }

    // ─── Share ───────────────────────────────────────────────────────────────

    private void shareQr() {
        if (currentBitmap == null) return;
        File cacheFile = QrGenerator.saveToCacheForShare(this, currentBitmap);
        if (cacheFile != null) {
            IntentHelper.shareImage(this, cacheFile);
        } else {
            Toast.makeText(this, R.string.error_no_app, Toast.LENGTH_SHORT).show();
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
