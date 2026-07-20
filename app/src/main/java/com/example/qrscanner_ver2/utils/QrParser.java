package com.example.qrscanner_ver2.utils;

import com.example.qrscanner_ver2.model.QrType;

/**
 * Parses a QR code content string and returns the corresponding QrType.
 */
public class QrParser {

    /**
     * Detect the type of QR content.
     *
     * @param content Raw string decoded from QR code
     * @return QrType enum value
     */
    public static QrType parse(String content) {
        if (content == null || content.trim().isEmpty()) {
            return QrType.TEXT;
        }

        String lower = content.toLowerCase().trim();

        if (lower.startsWith("http://") || lower.startsWith("https://")) {
            return QrType.URL;
        }
        if (lower.startsWith("mailto:")) {
            return QrType.EMAIL;
        }
        if (lower.startsWith("tel:") || lower.startsWith("phone:")) {
            return QrType.PHONE;
        }
        if (lower.startsWith("geo:") || lower.startsWith("maps:")) {
            return QrType.LOCATION;
        }
        if (lower.startsWith("begin:vcard")) {
            return QrType.CONTACT;
        }
        // Plain phone-number heuristic: digits, +, -, spaces, parens only
        if (content.trim().matches("[+\\d][\\d\\s\\-().+]{5,20}")) {
            return QrType.PHONE;
        }

        return QrType.TEXT;
    }

    /**
     * Returns a human-readable label for the given type.
     */
    public static String getLabel(QrType type) {
        switch (type) {
            case URL:      return "URL";
            case EMAIL:    return "Email";
            case PHONE:    return "Phone";
            case LOCATION: return "Location";
            case CONTACT:  return "Contact";
            default:       return "Text";
        }
    }

    /**
     * Returns the emoji icon for the type badge.
     */
    public static String getIcon(QrType type) {
        switch (type) {
            case URL:      return "🌐";
            case EMAIL:    return "✉️";
            case PHONE:    return "📞";
            case LOCATION: return "📍";
            case CONTACT:  return "👤";
            default:       return "📝";
        }
    }
}
