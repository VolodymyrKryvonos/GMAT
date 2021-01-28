package com.deepinspire.gmatclub.utils;

import android.content.Context;

public class StringUtils {
    public static boolean copyToClipboard(Context context, String text) {
        android.content.ClipboardManager clipboard = (android.content.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard == null)
            return false;
        android.content.ClipData clip = android.content.ClipData.newPlainText("Copied Text", text);
        clipboard.setPrimaryClip(clip);
        return true;
    }
}
