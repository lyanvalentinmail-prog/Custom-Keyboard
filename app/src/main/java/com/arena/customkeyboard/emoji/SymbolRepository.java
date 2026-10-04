package com.arena.customkeyboard.emoji;

import android.content.Context;
import android.content.SharedPreferences;

import com.arena.customkeyboard.R;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;

public final class SymbolRepository {
    private static final String PREFS = "keyboard_symbols";
    private static final String KEY_CUSTOM = "custom_symbols";

    public static final class Category {
        public final String id;
        public final String title;
        public final int iconRes;
        public final List<String> items;

        public Category(String id, String title, int iconRes, List<String> items) {
            this.id = id;
            this.title = title;
            this.iconRes = iconRes;
            this.items = items;
        }
    }

    private SymbolRepository() {}

    public static List<Category> categories(Context context) {
        ArrayList<Category> list = new ArrayList<>();
        list.add(new Category("custom", "Personalizados", R.drawable.ic_add, custom(context)));
        list.add(new Category("math", "Matemáticos", R.drawable.ic_symbols, Arrays.asList(
                "∞","≠","≈","≤","≥","∑","√","π","÷","×","±","∓","∂","∫","∮","∏","∆","∇","∈","∉","∋","⊂","⊃","⊆","⊇","∪","∩","∧","∨","¬","∀","∃","∴","∵","∝","∠","⊥","∥","≡","≅","≪","≫","⌈","⌉","⌊","⌋")));
        list.add(new Category("arrows", "Flechas", R.drawable.ic_cursor, Arrays.asList(
                "→","←","↑","↓","↔","↕","↖","↗","↘","↙","⇒","⇐","⇑","⇓","⇔","⇕","⇦","⇧","⇨","⇩","➜","➝","➞","➟","➠","➤","➥","➦","➧","➨","➩","➪","➫","➬","➭","➮","↩","↪","↫","↬","↭","↯","⇠","⇢")));
        list.add(new Category("geometry", "Geométricos", R.drawable.ic_symbols, Arrays.asList(
                "★","☆","◆","◇","■","□","●","○","▲","△","▼","▽","▶","▷","◀","◁","◈","◉","◎","◌","◍","◐","◑","◒","◓","◔","◕","◖","◗","◢","◣","◤","◥","◦","⬟","⬢","⬣","⬤","⬥","⬦","⬧","⬨","⬩")));
        list.add(new Category("currency", "Monedas", R.drawable.ic_clipboard, Arrays.asList(
                "$","€","£","¥","¢","₡","₢","₣","₤","₥","₦","₧","₨","₩","₪","₫","₭","₮","₯","₰","₱","₲","₳","₴","₵","₶","₷","₸","₹","₺","₻","₼","₽","₾","₿")));
        list.add(new Category("technical", "Técnicos", R.drawable.ic_settings, Arrays.asList(
                "⌁","⌘","※","⌂","⌐","⌑","⌒","⌓","⌔","⌕","⌖","⌗","⌘","⌙","⌚","⌛","⌜","⌝","⌞","⌟","⌠","⌡","⌢","⌣","⌤","⌥","⌦","⌧","⌨","⌫","⌬","⌭","⌮","⌯","⌰","⌱","⌲","⌳","⌴","⌵","⌶","⌷","⌸","⌹","⌺","⌻","⌼","⌽","⌾","⌿","⍀","⍁","⍂","⍃","⍄")));
        list.add(new Category("decorative", "Decorativos", R.drawable.ic_heart, Arrays.asList(
                "♡","♥","❥","❦","❧","☙","✦","✧","✩","✪","✫","✬","✭","✮","✯","✰","✱","✲","✳","✴","✵","✶","✷","✸","✹","✺","✻","✼","✽","✾","✿","❀","❁","❂","❃","❄","❅","❆","❇","❈","❉","❊","❋")));
        list.add(new Category("typographic", "Tipográficos", R.drawable.ic_keyboard, Arrays.asList(
                "「","」","『","』","“","”","‘","’","«","»","‹","›","—","–","…","·","•","‣","⁃","⁂","†","‡","‰","‱","′","″","‴","‵","‶","‷","‹","›","※","‼","⁇","⁈","⁉","§","¶","©","®","™","℠")));
        list.add(new Category("letters", "Letras especiales", R.drawable.ic_language, Arrays.asList(
                "á","é","í","ó","ú","ü","ñ","Á","É","Í","Ó","Ú","Ü","Ñ","à","è","ì","ò","ù","ä","ë","ï","ö","ÿ","â","ê","î","ô","û","ã","õ","å","æ","ç","ð","ø","þ","ß","œ","š","ž","č","ć","ł","ń","ğ","ş","İ","ı")));
        return list;
    }

    public static void addCustom(Context context, String symbol) {
        if (symbol == null || symbol.trim().length() == 0) return;
        SharedPreferences prefs = prefs(context);
        LinkedHashSet<String> set = new LinkedHashSet<>(custom(context));
        set.add(symbol.trim());
        saveList(prefs, KEY_CUSTOM, new ArrayList<>(set));
    }

    public static List<String> custom(Context context) {
        return loadList(prefs(context), KEY_CUSTOM);
    }

    private static SharedPreferences prefs(Context context) {
        return context.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    private static ArrayList<String> loadList(SharedPreferences prefs, String key) {
        ArrayList<String> result = new ArrayList<>();
        String raw = prefs.getString(key, "[]");
        try {
            JSONArray array = new JSONArray(raw);
            for (int i = 0; i < array.length(); i++) result.add(array.getString(i));
        } catch (JSONException ignored) {}
        return result;
    }

    private static void saveList(SharedPreferences prefs, String key, List<String> list) {
        JSONArray array = new JSONArray();
        for (String item : list) array.put(item);
        prefs.edit().putString(key, array.toString()).apply();
    }
}
