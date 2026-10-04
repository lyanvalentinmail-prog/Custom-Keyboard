package com.arena.customkeyboard.suggestions;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONException;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Local-only suggestion and autocorrection engine.
 * It intentionally has no network code and does not log user input.
 */
public final class SuggestionEngine {
    private static final String PREFS = "suggestions_local_dictionary";
    private static final String KEY_PERSONAL = "personal_words";

    private static final List<String> ES = Arrays.asList(
            "hola", "gracias", "buenos", "días", "buenas", "noches", "vale", "perfecto", "claro", "sí", "también",
            "porque", "entonces", "mañana", "hoy", "ayer", "ahora", "luego", "mensaje", "teclado", "personalizar",
            "trabajo", "reunión", "familia", "amigo", "amiga", "comida", "café", "casa", "tiempo", "rápido",
            "configuración", "privacidad", "portapapeles", "emojis", "símbolos", "programación", "buscar", "traducir",
            "estoy", "tengo", "quiero", "puedo", "vamos", "bien", "mal", "genial", "saludos", "por favor"
    );

    private static final List<String> EN = Arrays.asList(
            "hello", "thanks", "thank", "you", "please", "good", "morning", "night", "keyboard", "custom",
            "settings", "privacy", "clipboard", "emoji", "symbols", "programming", "search", "translate", "work",
            "meeting", "family", "friend", "today", "tomorrow", "yesterday", "quick", "perfect", "message",
            "because", "maybe", "right", "great", "awesome", "later", "now", "home", "project", "profile"
    );

    private static final Map<String, String> CORRECTIONS = new LinkedHashMap<>();
    static {
        CORRECTIONS.put("ola", "hola");
        CORRECTIONS.put("grasias", "gracias");
        CORRECTIONS.put("tambien", "también");
        CORRECTIONS.put("manana", "mañana");
        CORRECTIONS.put("configuracion", "configuración");
        CORRECTIONS.put("tecldo", "teclado");
        CORRECTIONS.put("privasidad", "privacidad");
        CORRECTIONS.put("recibir", "recibir");
        CORRECTIONS.put("teh", "the");
        CORRECTIONS.put("adress", "address");
        CORRECTIONS.put("recieve", "receive");
        CORRECTIONS.put("thanks", "thanks");
    }

    private final SharedPreferences prefs;

    public SuggestionEngine(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public List<String> suggest(String composing, String language, int max) {
        LinkedHashSet<String> result = new LinkedHashSet<>();
        if (max <= 0) return new ArrayList<>();
        String prefix = composing == null ? "" : composing.trim();
        if (prefix.length() == 0) {
            if (isSpanish(language)) result.addAll(Arrays.asList("hola", "gracias", "vale"));
            else result.addAll(Arrays.asList("hello", "thanks", "ok"));
            return limit(result, max);
        }
        String lower = prefix.toLowerCase(Locale.ROOT);
        String normalized = normalize(lower);
        String correction = CORRECTIONS.get(lower);
        if (correction != null && !correction.equals(lower)) result.add(matchCase(prefix, correction));
        for (String word : personalWords()) addIfMatches(result, prefix, normalized, word);
        for (String word : dictionary(language)) addIfMatches(result, prefix, normalized, word);
        if (prefix.length() > 1) {
            result.add(prefix);
            result.add(prefix + "!");
        }
        return limit(result, max);
    }

    public String autoCorrect(String word, String language) {
        if (TextUtils.isEmpty(word) || word.length() < 2) return word;
        String lower = word.toLowerCase(Locale.ROOT);
        String correction = CORRECTIONS.get(lower);
        if (correction != null && !correction.equals(lower)) return matchCase(word, correction);
        return word;
    }

    public void learnWord(String word) {
        if (TextUtils.isEmpty(word)) return;
        String cleaned = word.trim();
        if (cleaned.length() < 2 || cleaned.length() > 32) return;
        if (!cleaned.matches("[\\p{L}][\\p{L}'’\u00f1\u00d1-]*")) return;
        LinkedHashSet<String> set = new LinkedHashSet<>(personalWords());
        set.add(cleaned);
        while (set.size() > 500) {
            String first = set.iterator().next();
            set.remove(first);
        }
        JSONArray array = new JSONArray();
        for (String item : set) array.put(item);
        prefs.edit().putString(KEY_PERSONAL, array.toString()).apply();
    }

    private List<String> personalWords() {
        ArrayList<String> result = new ArrayList<>();
        String raw = prefs.getString(KEY_PERSONAL, "[]");
        try {
            JSONArray array = new JSONArray(raw);
            for (int i = 0; i < array.length(); i++) result.add(array.getString(i));
        } catch (JSONException ignored) {}
        return result;
    }

    private List<String> dictionary(String language) {
        return isSpanish(language) ? ES : EN;
    }

    private boolean isSpanish(String language) {
        return language == null || language.toLowerCase(Locale.US).startsWith("es");
    }

    private void addIfMatches(LinkedHashSet<String> result, String originalPrefix, String normalizedPrefix, String word) {
        String lowerWord = word.toLowerCase(Locale.ROOT);
        if (lowerWord.startsWith(originalPrefix.toLowerCase(Locale.ROOT)) || normalize(lowerWord).startsWith(normalizedPrefix)) {
            result.add(matchCase(originalPrefix, word));
        }
    }

    private List<String> limit(LinkedHashSet<String> values, int max) {
        ArrayList<String> out = new ArrayList<>();
        for (String value : values) {
            out.add(value);
            if (out.size() >= max) break;
        }
        return out;
    }

    private static String normalize(String value) {
        String n = Normalizer.normalize(value, Normalizer.Form.NFD);
        return n.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
    }

    private static String matchCase(String sample, String value) {
        if (sample.length() > 0 && Character.isUpperCase(sample.charAt(0))) {
            return value.substring(0, 1).toUpperCase(Locale.ROOT) + value.substring(1);
        }
        return value;
    }
}
