package com.example.qrscanner_ver2.utils;

import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Generates QR codes as Bitmap using ZXing's MultiFormatWriter.
 */
public class QrGenerator {

    /**
     * Generate a square QR code Bitmap from the given text.
     *
     * @param text Content to encode
     * @param size Width and height in pixels (e.g. 512)
     * @return Bitmap or null on failure
     */
    public static Bitmap generate(String text, int size) {
        if (text == null || text.trim().isEmpty()) return null;

        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        hints.put(EncodeHintType.MARGIN, 1);

        try {
            MultiFormatWriter writer = new MultiFormatWriter();
            BitMatrix matrix = writer.encode(text, BarcodeFormat.QR_CODE, size, size, hints);

            int[] pixels = new int[size * size];
            for (int y = 0; y < size; y++) {
                for (int x = 0; x < size; x++) {
                    pixels[y * size + x] = matrix.get(x, y) ? Color.BLACK : Color.WHITE;
                }
            }

            Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
            bitmap.setPixels(pixels, 0, size, 0, 0, size, size);
            return bitmap;

        } catch (WriterException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Save a Bitmap to the device gallery (Pictures/QRScanner/).
     * Returns the saved File or null on failure.
     */
    public static File saveBitmapToGallery(Context ctx, Bitmap bitmap) {
        String fileName = "QR_" + System.currentTimeMillis() + ".png";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // MediaStore API (API 29+)
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName);
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
            values.put(MediaStore.Images.Media.RELATIVE_PATH,
                    Environment.DIRECTORY_PICTURES + "/QRScanner");
            values.put(MediaStore.Images.Media.IS_PENDING, 1);

            Uri uri = ctx.getContentResolver()
                    .insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            if (uri == null) return null;

            try (OutputStream os = ctx.getContentResolver().openOutputStream(uri)) {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, os);
                values.clear();
                values.put(MediaStore.Images.Media.IS_PENDING, 0);
                ctx.getContentResolver().update(uri, values, null, null);
                // Return a temp file for sharing via FileProvider
                return saveToCacheForShare(ctx, bitmap);
            } catch (IOException e) {
                ctx.getContentResolver().delete(uri, null, null);
                return null;
            }

        } else {
            // Legacy path
            File dir = new File(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_PICTURES), "QRScanner");
            if (!dir.exists()) dir.mkdirs();
            File file = new File(dir, fileName);
            try (FileOutputStream fos = new FileOutputStream(file)) {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
                return file;
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }
    }

    /**
     * Save bitmap to app cache directory for sharing via FileProvider.
     */
    public static File saveToCacheForShare(Context ctx, Bitmap bitmap) {
        File dir = new File(ctx.getCacheDir(), "qr_images");
        if (!dir.exists()) dir.mkdirs();
        File file = new File(dir, "qr_share_" + System.currentTimeMillis() + ".png");
        try (FileOutputStream fos = new FileOutputStream(file)) {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            return file;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
