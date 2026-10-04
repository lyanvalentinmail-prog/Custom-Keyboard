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

public final class EmojiRepository {
    private static final String PREFS = "keyboard_emoji";
    private static final String KEY_RECENTS = "recents";
    private static final String KEY_FAVORITES = "favorites";

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

    private EmojiRepository() {}

    public static List<Category> categories(Context context) {
        ArrayList<Category> list = new ArrayList<>();
        list.add(new Category("recent", "Recientes", R.drawable.ic_keyboard, recents(context)));
        list.add(new Category("fav", "Favoritos", R.drawable.ic_heart, favorites(context)));
        list.add(new Category("people", "Personas", R.drawable.ic_person, Arrays.asList(
                "😀","😃","😄","😁","😆","😅","😂","🤣","😊","😇","🙂","🙃","😉","😍","😘","😗","😙","😚","😋","😛","😜","🤪","🤨","🧐","🤓","😎","🥳","😏","😒","😞","😔","😟","😕","🙁","☹️","😣","😖","😫","😩","🥺","😢","😭","😤","😠","😡","🤬","🤯","😳","🥵","🥶","😱","😨","😰","😥","😓","🤗","🤔","🫡","🤭","🤫","🤥","😶","😐","😑","😬","🙄","😯","😦","😧","😮","😲","🥱","😴","🤤","😪","😵","🤐","🥴","🤢","🤮","🤧","😷","🤒","🤕")));
        list.add(new Category("animals", "Animales", R.drawable.ic_animal, Arrays.asList(
                "🐶","🐱","🐭","🐹","🐰","🦊","🐻","🐼","🐻‍❄️","🐨","🐯","🦁","🐮","🐷","🐽","🐸","🐵","🙈","🙉","🙊","🐒","🐔","🐧","🐦","🐤","🐣","🐥","🦆","🦅","🦉","🦇","🐺","🐗","🐴","🦄","🐝","🐛","🦋","🐌","🐞","🐜","🪲","🐢","🐍","🦎","🦖","🦕","🐙","🦑","🦐","🦞","🦀","🐡","🐠","🐟","🐬","🐳","🐋","🦈")));
        list.add(new Category("food", "Comida", R.drawable.ic_food, Arrays.asList(
                "🍏","🍎","🍐","🍊","🍋","🍌","🍉","🍇","🍓","🫐","🍈","🍒","🍑","🥭","🍍","🥥","🥝","🍅","🍆","🥑","🥦","🥬","🥒","🌶️","🫑","🌽","🥕","🫒","🧄","🧅","🥔","🍠","🥐","🥯","🍞","🥖","🧀","🥚","🍳","🧈","🥞","🧇","🥓","🥩","🍗","🍖","🌭","🍔","🍟","🍕","🥪","🥙","🧆","🌮","🌯","🫔","🥗","🥘","🫕","🍝","🍜","🍲","🍛","🍣","🍱","🥟","🍤","🍙","🍚","🍘","🍥","🥠","🥮","🍢","🍡","🍧","🍨","🍦","🥧","🧁","🍰","🎂","🍮","🍭","🍬","🍫","🍿","🍩","🍪","☕","🍵","🧃","🥤")));
        list.add(new Category("activities", "Actividades", R.drawable.ic_activity, Arrays.asList(
                "⚽","🏀","🏈","⚾","🥎","🎾","🏐","🏉","🥏","🎱","🪀","🏓","🏸","🏒","🏑","🥍","🏏","🪃","🥅","⛳","🪁","🏹","🎣","🤿","🥊","🥋","🎽","🛹","🛼","🛷","⛸️","🥌","🎿","⛷️","🏂","🪂","🏋️","🤼","🤸","⛹️","🤺","🤾","🏌️","🏇","🧘","🏄","🏊","🤽","🚣","🧗","🚵","🚴","🎮","🕹️","🎲","♟️","🎯","🎳","🎭","🎨","🎬","🎤","🎧","🎼","🎹","🥁","🎷","🎺","🎸","🪕","🎻")));
        list.add(new Category("travel", "Viajes", R.drawable.ic_travel, Arrays.asList(
                "🚗","🚕","🚙","🚌","🚎","🏎️","🚓","🚑","🚒","🚐","🛻","🚚","🚛","🚜","🦯","🦽","🦼","🛴","🚲","🛵","🏍️","🛺","🚨","🚔","🚍","🚘","🚖","🚡","🚠","🚟","🚃","🚋","🚞","🚝","🚄","🚅","🚈","🚂","🚆","🚇","🚊","🚉","✈️","🛫","🛬","🛩️","💺","🛰️","🚀","🛸","🚁","🛶","⛵","🚤","🛥️","🛳️","⛴️","🚢","⚓","🗿","🗽","🗼","🏰","🏯","🏟️","🎡","🎢","🎠","⛲","⛱️","🏖️","🏝️","🏜️","🌋","⛰️","🏔️","🗻","🏕️","⛺","🛖","🏠","🏡","🏘️","🏙️")));
        list.add(new Category("objects", "Objetos", R.drawable.ic_object, Arrays.asList(
                "💡","🔦","🕯️","🪔","🧯","🛢️","💸","💵","💴","💶","💷","🪙","💰","💳","💎","⚖️","🪜","🧰","🪛","🔧","🔨","⚒️","🛠️","⛏️","🪚","🔩","⚙️","🪤","🧱","⛓️","🧲","🔫","💣","🧨","🪓","🔪","🗡️","⚔️","🛡️","🚬","⚰️","🪦","⚱️","🏺","🔮","📿","🧿","💈","⚗️","🔭","🔬","🕳️","🩹","🩺","💊","💉","🧬","🦠","🧫","🧪","🌡️","🧹","🪠","🧺","🧻","🚽","🚰","🚿","🛁","🛀","🧼","🪥","🪒","🧽","🪣","🧴","🔑","🗝️","🚪","🪑","🛋️","🛏️","🛌","🧸","🪆")));
        list.add(new Category("symbols", "Símbolos", R.drawable.ic_heart, Arrays.asList(
                "❤️","🧡","💛","💚","💙","💜","🖤","🤍","🤎","💔","❣️","💕","💞","💓","💗","💖","💘","💝","💟","☮️","✝️","☪️","🕉️","☸️","✡️","🔯","🕎","☯️","☦️","🛐","⛎","♈","♉","♊","♋","♌","♍","♎","♏","♐","♑","♒","♓","🆔","⚛️","🉑","☢️","☣️","📴","📳","🈶","🈚","🈸","🈺","🈷️","✴️","🆚","💮","🉐","㊙️","㊗️","🈴","🈵","🈹","🈲","🅰️","🅱️","🆎","🆑","🅾️","🆘","❌","⭕","🛑","⛔","📛","🚫","💯","💢","♨️","🚷","🚯","🚳","🚱","🔞","📵","🚭","❗","❕","❓","❔","‼️","⁉️")));
        list.add(new Category("flags", "Banderas", R.drawable.ic_flag, Arrays.asList(
                "🏳️","🏴","🏁","🚩","🏳️‍🌈","🏳️‍⚧️","🇪🇸","🇲🇽","🇦🇷","🇨🇴","🇨🇱","🇵🇪","🇺🇾","🇵🇾","🇧🇴","🇪🇨","🇻🇪","🇨🇷","🇵🇦","🇩🇴","🇵🇷","🇨🇺","🇺🇸","🇬🇧","🇫🇷","🇩🇪","🇮🇹","🇵🇹","🇧🇷","🇨🇦","🇯🇵","🇰🇷","🇨🇳","🇮🇳","🇦🇺","🇳🇿","🇿🇦","🇪🇺")));
        return list;
    }

    public static List<String> search(Context context, String query) {
        String q = query == null ? "" : query.trim().toLowerCase();
        ArrayList<String> out = new ArrayList<>();
        if (q.length() == 0) return out;
        for (Category category : categories(context)) {
            for (String item : category.items) {
                if (!out.contains(item) && item.contains(q)) out.add(item);
            }
        }
        return out;
    }

    public static void addRecent(Context context, String emoji) {
        addToList(context, KEY_RECENTS, emoji, 48);
    }

    public static void toggleFavorite(Context context, String emoji) {
        SharedPreferences prefs = prefs(context);
        LinkedHashSet<String> set = new LinkedHashSet<>(loadList(prefs, KEY_FAVORITES));
        if (set.contains(emoji)) set.remove(emoji);
        else set.add(emoji);
        saveList(prefs, KEY_FAVORITES, new ArrayList<>(set));
    }

    public static List<String> recents(Context context) {
        return loadList(prefs(context), KEY_RECENTS);
    }

    public static List<String> favorites(Context context) {
        return loadList(prefs(context), KEY_FAVORITES);
    }

    private static void addToList(Context context, String key, String value, int max) {
        if (value == null || value.length() == 0) return;
        SharedPreferences prefs = prefs(context);
        ArrayList<String> list = loadList(prefs, key);
        list.remove(value);
        list.add(0, value);
        while (list.size() > max) list.remove(list.size() - 1);
        saveList(prefs, key, list);
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
