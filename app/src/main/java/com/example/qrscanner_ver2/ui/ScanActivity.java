package com.example.qrscanner_ver2.ui;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.qrscanner_ver2.R;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import java.io.InputStream;
import java.util.EnumMap;
import java.util.Map;

/**
 * Main / launcher screen. Offers camera scan, gallery scan, generate QR, and view history.
 */
public class ScanActivity extends AppCompatActivity {

    private static final int REQ_CAMERA_PERMISSION = 101;
    private static final int REQ_STORAGE_PERMISSION = 102;

    // ZXing Embedded – camera scanner launcher
    private final ActivityResultLauncher<ScanOptions> cameraLauncher =
            registerForActivityResult(new ScanContract(), result -> {
                if (result.getContents() != null) {
                    openResult(result.getContents());
                }
            });

    // Gallery image picker launcher
    private final ActivityResultLauncher<String> galleryLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    decodeQrFromUri(uri);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Buttons
        findViewById(R.id.btnScanCamera).setOnClickListener(v -> checkCameraAndScan());
        findViewById(R.id.btnScanGallery).setOnClickListener(v -> checkStorageAndPickImage());
        findViewById(R.id.btnGenerate).setOnClickListener(v ->
                startActivity(new Intent(this, GenerateActivity.class)));
        findViewById(R.id.btnHistory).setOnClickListener(v ->
                startActivity(new Intent(this, HistoryActivity.class)));
    }

    // ─── Camera ──────────────────────────────────────────────────────────────

    private void checkCameraAndScan() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            launchCameraScanner();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA}, REQ_CAMERA_PERMISSION);
        }
    }

    private void launchCameraScanner() {
        ScanOptions options = new ScanOptions();
        options.setDesiredBarcodeFormats(ScanOptions.QR_CODE);
        options.setPrompt("Point camera at QR code");
        options.setCameraId(0);
        options.setBeepEnabled(true);
        options.setBarcodeImageEnabled(false);
        options.setOrientationLocked(true);
        cameraLauncher.launch(options);
    }

    // ─── Gallery ─────────────────────────────────────────────────────────────

    private void checkStorageAndPickImage() {
        // On API 33+ we use READ_MEDIA_IMAGES; below that READ_EXTERNAL_STORAGE.
        // GetContent contract handles the picker — no permission needed on API 30+.
        galleryLauncher.launch("image/*");
    }

    private void decodeQrFromUri(Uri uri) {
        try {
            InputStream is = getContentResolver().openInputStream(uri);
            if (is == null) {
                Toast.makeText(this, R.string.error_decode_failed, Toast.LENGTH_SHORT).show();
                return;
            }
            Bitmap bitmap = BitmapFactory.decodeStream(is);
            is.close();

            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            int[] pixels = new int[width * height];
            bitmap.getPixels(pixels, 0, width, 0, 0, width, height);

            RGBLuminanceSource source = new RGBLuminanceSource(width, height, pixels);
            BinaryBitmap binary = new BinaryBitmap(new HybridBinarizer(source));

            Map<DecodeHintType, Object> hints = new EnumMap<>(DecodeHintType.class);
            hints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);

            Result result = new MultiFormatReader().decode(binary, hints);
            openResult(result.getText());

        } catch (NotFoundException e) {
            Toast.makeText(this, R.string.error_no_qr_found, Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(this, R.string.error_decode_failed, Toast.LENGTH_SHORT).show();
        }
    }

    // ─── Navigate to Result ──────────────────────────────────────────────────

    private void openResult(String content) {
        Intent intent = new Intent(this, ResultActivity.class);
        intent.putExtra(ResultActivity.EXTRA_CONTENT, content);
        startActivity(intent);
    }

    // ─── Permissions ─────────────────────────────────────────────────────────

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                launchCameraScanner();
            } else {
                Toast.makeText(this, R.string.permission_required, Toast.LENGTH_SHORT).show();
            }
        }
    }
}
