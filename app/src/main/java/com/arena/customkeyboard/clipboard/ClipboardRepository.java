package com.arena.customkeyboard.clipboard;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

/** Local clipboard history controlled by the user. */
public final class ClipboardRepository {
    private static final String PREFS = "keyboard_clipboard";
    private static final String KEY_HISTORY = "history";
    private static final String KEY_PINNED = "pinned";
    private static final int MAX_ITEMS = 30;

    private ClipboardRepository() {}

    public static void captureSystemClipboard(Context context) {
        ClipboardManager manager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        if (manager == null || !manager.hasPrimaryClip()) return;
        ClipData clip = manager.getPrimaryClip();
        if (clip == null || clip.getItemCount() == 0) return;
        CharSequence text = clip.getItemAt(0).coerceToText(context);
        if (text != null) addHistory(context, text.toString());
    }

    public static void copyToSystem(Context context, String text) {
        if (TextUtils.isEmpty(text)) return;
        ClipboardManager manager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        if (manager != null) manager.setPrimaryClip(ClipData.newPlainText("keyboard", text));
        addHistory(context, text);
    }

    public static void addHistory(Context context, String text) {
        String cleaned = clean(text);
        if (TextUtils.isEmpty(cleaned)) return;
        ArrayList<String> current = new ArrayList<>(history(context));
        current.remove(cleaned);
        current.add(0, cleaned);
        while (current.size() > MAX_ITEMS) current.remove(current.size() - 1);
        saveList(context, KEY_HISTORY, current);
    }

    public static List<String> history(Context context) {
        return loadList(context, KEY_HISTORY);
    }

    public static List<String> pinned(Context context) {
        return loadList(context, KEY_PINNED);
    }

    public static void pin(Context context, String text) {
        String cleaned = clean(text);
        if (TextUtils.isEmpty(cleaned)) return;
        LinkedHashSet<String> set = new LinkedHashSet<>(pinned(context));
        if (set.contains(cleaned)) set.remove(cleaned);
        else set.add(cleaned);
        saveList(context, KEY_PINNED, new ArrayList<>(set));
    }

    public static void remove(Context context, String text) {
        ArrayList<String> h = new ArrayList<>(history(context));
        h.remove(text);
        saveList(context, KEY_HISTORY, h);
        ArrayList<String> p = new ArrayList<>(pinned(context));
        p.remove(text);
        saveList(context, KEY_PINNED, p);
    }

    public static void clear(Context context) {
        prefs(context).edit().remove(KEY_HISTORY).apply();
    }

    private static SharedPreferences prefs(Context context) {
        return context.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    private static String clean(String text) {
        if (text == null) return "";
        String cleaned = text.replace('\u0000', ' ').trim();
        if (cleaned.length() > 10000) cleaned = cleaned.substring(0, 10000);
        return cleaned;
    }

    private static ArrayList<String> loadList(Context context, String key) {
        ArrayList<String> result = new ArrayList<>();
        String raw = prefs(context).getString(key, "[]");
        try {
            JSONArray array = new JSONArray(raw);
            for (int i = 0; i < array.length(); i++) result.add(array.getString(i));
        } catch (JSONException ignored) {}
        return result;
    }

    private static void saveList(Context context, String key, List<String> list) {
        JSONArray array = new JSONArray();
        for (String item : list) array.put(item);
        prefs(context).edit().putString(key, array.toString()).apply();
    }
}
