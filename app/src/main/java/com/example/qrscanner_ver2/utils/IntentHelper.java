package com.example.qrscanner_ver2.utils;

import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.ContactsContract;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import com.example.qrscanner_ver2.R;

import java.io.File;

/**
 * Helper that builds and fires Android Intents for each QR content type.
 */
public class IntentHelper {

    // ─── URL ──────────────────────────────────────────────────────────────────

    public static void openUrl(Context ctx, String url) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            ctx.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(ctx, R.string.error_no_app, Toast.LENGTH_SHORT).show();
        }
    }

    // ─── Email ────────────────────────────────────────────────────────────────

    public static void openEmail(Context ctx, String raw) {
        try {
            // Strip "mailto:" prefix if present
            String address = raw.toLowerCase().startsWith("mailto:")
                    ? raw.substring(7) : raw;
            // Handle optional subject/body params
            Intent intent = new Intent(Intent.ACTION_SENDTO,
                    Uri.parse("mailto:" + address));
            ctx.startActivity(Intent.createChooser(intent, "Send Email"));
        } catch (ActivityNotFoundException e) {
            Toast.makeText(ctx, R.string.error_no_app, Toast.LENGTH_SHORT).show();
        }
    }

    // ─── Phone ────────────────────────────────────────────────────────────────

    public static void openPhone(Context ctx, String raw) {
        try {
            String number = raw.toLowerCase().startsWith("tel:")
                    ? raw.substring(4) : raw;
            Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + number));
            ctx.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(ctx, R.string.error_no_app, Toast.LENGTH_SHORT).show();
        }
    }

    // ─── Location ─────────────────────────────────────────────────────────────

    public static void openMaps(Context ctx, String raw) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(raw));
            intent.setPackage("com.google.android.apps.maps");
            if (intent.resolveActivity(ctx.getPackageManager()) != null) {
                ctx.startActivity(intent);
            } else {
                // Fall back to any maps app
                intent.setPackage(null);
                ctx.startActivity(intent);
            }
        } catch (ActivityNotFoundException e) {
            Toast.makeText(ctx, R.string.error_no_app, Toast.LENGTH_SHORT).show();
        }
    }

    // ─── Contact ──────────────────────────────────────────────────────────────

    public static void addContact(Context ctx, String vcard) {
        try {
            Intent intent = new Intent(Intent.ACTION_INSERT);
            intent.setType(ContactsContract.RawContacts.CONTENT_TYPE);
            // Pass the raw vCard for apps that support it
            intent.putExtra("VCARD_DATA", vcard);
            ctx.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(ctx, R.string.error_no_app, Toast.LENGTH_SHORT).show();
        }
    }

    // ─── Text / Copy ──────────────────────────────────────────────────────────

    public static void copyToClipboard(Context ctx, String text) {
        ClipboardManager cm = (ClipboardManager) ctx.getSystemService(Context.CLIPBOARD_SERVICE);
        if (cm != null) {
            cm.setPrimaryClip(ClipData.newPlainText("QR Content", text));
            Toast.makeText(ctx, R.string.copied_to_clipboard, Toast.LENGTH_SHORT).show();
        }
    }

    public static void shareText(Context ctx, String text) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, text);
        ctx.startActivity(Intent.createChooser(intent, "Share QR Content"));
    }

    // ─── Image ────────────────────────────────────────────────────────────────

    public static void shareImage(Context ctx, File imageFile) {
        try {
            Uri uri = FileProvider.getUriForFile(
                    ctx,
                    ctx.getPackageName() + ".fileprovider",
                    imageFile
            );
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("image/png");
            intent.putExtra(Intent.EXTRA_STREAM, uri);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            ctx.startActivity(Intent.createChooser(intent, "Share QR Image"));
        } catch (Exception e) {
            Toast.makeText(ctx, R.string.error_no_app, Toast.LENGTH_SHORT).show();
        }
    }
}
