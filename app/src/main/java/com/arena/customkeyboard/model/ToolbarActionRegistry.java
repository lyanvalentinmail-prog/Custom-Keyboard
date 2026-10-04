package com.arena.customkeyboard.model;

import com.arena.customkeyboard.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Registry of every toolbar action supported by the IME. */
public final class ToolbarActionRegistry {
    public static final String GIF = "gif";
    public static final String EMOJI = "emoji";
    public static final String CLIPBOARD = "clipboard";
    public static final String SETTINGS = "settings";
    public static final String SEARCH = "search";
    public static final String TRANSLATE = "translate";
    public static final String SELECTION = "selection";
    public static final String SYMBOLS = "symbols";
    public static final String LANGUAGE = "language";
    public static final String MICROPHONE = "microphone";
    public static final String CLOSE = "close";
    public static final String KEYBOARD = "keyboard";

    private static final LinkedHashMap<String, ToolbarAction> ACTIONS = new LinkedHashMap<>();

    static {
        register(new ToolbarAction(GIF, "GIF", R.drawable.ic_gif, false));
        register(new ToolbarAction(EMOJI, "Emojis", R.drawable.ic_emoji, false));
        register(new ToolbarAction(CLIPBOARD, "Portapapeles", R.drawable.ic_clipboard, true));
        register(new ToolbarAction(SETTINGS, "Configuración", R.drawable.ic_settings, false));
        register(new ToolbarAction(SEARCH, "Buscar", R.drawable.ic_search, false));
        register(new ToolbarAction(TRANSLATE, "Traducir", R.drawable.ic_translate, true));
        register(new ToolbarAction(SELECTION, "Selección de texto", R.drawable.ic_cursor, false));
        register(new ToolbarAction(SYMBOLS, "Símbolos", R.drawable.ic_symbols, false));
        register(new ToolbarAction(LANGUAGE, "Idioma", R.drawable.ic_language, false));
        register(new ToolbarAction(MICROPHONE, "Micrófono", R.drawable.ic_microphone, true));
        register(new ToolbarAction(CLOSE, "Cerrar", R.drawable.ic_close, false));
        register(new ToolbarAction(KEYBOARD, "Teclado", R.drawable.ic_keyboard, false));
    }

    private ToolbarActionRegistry() {}

    private static void register(ToolbarAction action) {
        ACTIONS.put(action.id, action);
    }

    public static ToolbarAction get(String id) {
        return ACTIONS.get(id);
    }

    public static List<ToolbarAction> all() {
        return Collections.unmodifiableList(new ArrayList<>(ACTIONS.values()));
    }

    public static List<String> defaultSamsungLikeOrder() {
        ArrayList<String> ids = new ArrayList<>();
        ids.add(GIF);
        ids.add(EMOJI);
        ids.add(CLIPBOARD);
        ids.add(SETTINGS);
        ids.add(SEARCH);
        ids.add(TRANSLATE);
        ids.add(SELECTION);
        return ids;
    }

    public static List<String> aiOrder() {
        ArrayList<String> ids = new ArrayList<>();
        ids.add(TRANSLATE);
        ids.add(SEARCH);
        ids.add(GIF);
        ids.add(EMOJI);
        ids.add(CLIPBOARD);
        ids.add(SETTINGS);
        return ids;
    }

    public static List<ToolbarAction> fromIds(List<String> ids) {
        ArrayList<ToolbarAction> result = new ArrayList<>();
        for (String id : ids) {
            ToolbarAction action = ACTIONS.get(id);
            if (action != null) result.add(action);
        }
        return result;
    }

    public static Map<String, ToolbarAction> map() {
        return Collections.unmodifiableMap(ACTIONS);
    }
}
