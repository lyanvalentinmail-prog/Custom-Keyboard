package com.arena.customkeyboard.model;

import com.arena.customkeyboard.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public final class KeyboardLayout {
    public static final String ACTION_SHIFT = "shift";
    public static final String ACTION_BACKSPACE = "backspace";
    public static final String ACTION_ENTER = "enter";
    public static final String ACTION_SPACE = "space";
    public static final String ACTION_MODE_NUMBERS = "mode_numbers";
    public static final String ACTION_MODE_LETTERS = "mode_letters";
    public static final String ACTION_MODE_SYMBOLS = "mode_symbols";
    public static final String ACTION_MODE_PROGRAMMER = "mode_programmer";
    public static final String ACTION_EMOJI = "emoji";
    public static final String ACTION_TAB = "tab";
    public static final String ACTION_ESC = "esc";
    public static final String ACTION_CTRL = "ctrl";
    public static final String ACTION_ALT = "alt";

    private KeyboardLayout() {}

    public static List<List<KeySpec>> forMode(KeyboardMode mode, String language, boolean shifted, boolean capsLocked) {
        switch (mode) {
            case NUMBERS:
                return numbers();
            case SYMBOLS:
                return symbols();
            case PROGRAMMER:
                return programmer();
            case LETTERS:
            default:
                return letters(language, shifted, capsLocked);
        }
    }

    public static List<List<KeySpec>> letters(String language, boolean shifted, boolean capsLocked) {
        boolean spanish = language != null && language.toLowerCase(Locale.US).startsWith("es");
        ArrayList<List<KeySpec>> rows = new ArrayList<>();
        rows.add(letterRow(new String[]{"q","w","e","r","t","y","u","i","o","p"}, shifted));
        rows.add(letterRow(spanish
                ? new String[]{"a","s","d","f","g","h","j","k","l","ñ"}
                : new String[]{"a","s","d","f","g","h","j","k","l"}, shifted));

        ArrayList<KeySpec> row3 = new ArrayList<>();
        row3.add(KeySpec.action("shift", capsLocked ? "⇪" : "⇧", ACTION_SHIFT)
                .icon(R.drawable.ic_shift)
                .weight(13)
                .contentDescription(capsLocked ? "Bloqueo de mayúsculas" : "Mayúsculas")
                .build());
        String[] third = new String[]{"z","x","c","v","b","n","m"};
        row3.addAll(letterRow(third, shifted));
        row3.add(KeySpec.action("backspace", "", ACTION_BACKSPACE)
                .icon(R.drawable.ic_backspace)
                .weight(13)
                .repeatable(true)
                .contentDescription("Borrar")
                .build());
        rows.add(row3);

        ArrayList<KeySpec> row4 = new ArrayList<>();
        row4.add(KeySpec.action("numbers", "123", ACTION_MODE_NUMBERS).weight(17).contentDescription("Números").build());
        row4.add(text(",", ",").weight(9).build());
        row4.add(KeySpec.action("space", "ESPACIO", ACTION_SPACE).weight(44).contentDescription("Espacio, desliza para mover cursor").build());
        row4.add(text(".", ".").weight(9).build());
        row4.add(KeySpec.action("enter", "", ACTION_ENTER).icon(R.drawable.ic_enter).weight(17).contentDescription("Enter").build());
        rows.add(row4);
        return rows;
    }

    private static List<KeySpec> letterRow(String[] letters, boolean shifted) {
        ArrayList<KeySpec> row = new ArrayList<>();
        for (String value : letters) {
            String output = shifted ? value.toUpperCase(Locale.getDefault()) : value;
            row.add(text(value, output).alternatives(alternativesFor(value)).build());
        }
        return row;
    }

    private static KeySpec.Builder text(String id, String output) {
        return KeySpec.text(id, output, output).contentDescription(output);
    }

    private static List<String> alternativesFor(String base) {
        switch (base) {
            case "a": return Arrays.asList("á", "à", "ä", "â", "ã", "å", "æ");
            case "e": return Arrays.asList("é", "è", "ë", "ê", "€");
            case "i": return Arrays.asList("í", "ì", "ï", "î");
            case "o": return Arrays.asList("ó", "ò", "ö", "ô", "õ", "ø");
            case "u": return Arrays.asList("ú", "ù", "ü", "û");
            case "n": return Arrays.asList("ñ", "ń");
            case "c": return Arrays.asList("ç", "©");
            case "s": return Arrays.asList("ß", "§");
            case "y": return Arrays.asList("ý", "ÿ");
            case ".": return Arrays.asList("…", ":", ";");
            case ",": return Arrays.asList(";", "'", "\"");
            default: return new ArrayList<>();
        }
    }

    public static List<List<KeySpec>> numbers() {
        ArrayList<List<KeySpec>> rows = new ArrayList<>();
        rows.add(simpleTextRow(new String[]{"1","2","3","4","5","6","7","8","9","0"}));
        rows.add(simpleTextRow(new String[]{"@","#","€","_","&","-","+","(",")","/"}));
        ArrayList<KeySpec> row3 = new ArrayList<>();
        row3.add(KeySpec.action("symbols", "=\\<", ACTION_MODE_SYMBOLS).weight(14).contentDescription("Símbolos").build());
        row3.addAll(simpleTextRow(new String[]{"*", "\"", "'", ":", ";", "!", "?"}));
        row3.add(KeySpec.action("backspace", "", ACTION_BACKSPACE).icon(R.drawable.ic_backspace).weight(14).repeatable(true).contentDescription("Borrar").build());
        rows.add(row3);
        ArrayList<KeySpec> row4 = new ArrayList<>();
        row4.add(KeySpec.action("letters", "ABC", ACTION_MODE_LETTERS).weight(17).contentDescription("Letras").build());
        row4.add(KeySpec.action("programmer", "{ }", ACTION_MODE_PROGRAMMER).weight(12).contentDescription("Modo programador").build());
        row4.add(KeySpec.action("space", "ESPACIO", ACTION_SPACE).weight(40).contentDescription("Espacio").build());
        row4.add(text(".", ".").weight(9).build());
        row4.add(KeySpec.action("enter", "", ACTION_ENTER).icon(R.drawable.ic_enter).weight(17).contentDescription("Enter").build());
        rows.add(row4);
        return rows;
    }

    public static List<List<KeySpec>> symbols() {
        ArrayList<List<KeySpec>> rows = new ArrayList<>();
        rows.add(simpleTextRow(new String[]{"[","]","{","}","#","%","^","*","+","="}));
        rows.add(simpleTextRow(new String[]{"_","\\","|","~","<",">","$","£","¥","•"}));
        ArrayList<KeySpec> row3 = new ArrayList<>();
        row3.add(KeySpec.action("numbers", "123", ACTION_MODE_NUMBERS).weight(14).contentDescription("Números").build());
        row3.addAll(simpleTextRow(new String[]{"∞","≈","≠","≤","≥","π","√"}));
        row3.add(KeySpec.action("backspace", "", ACTION_BACKSPACE).icon(R.drawable.ic_backspace).weight(14).repeatable(true).contentDescription("Borrar").build());
        rows.add(row3);
        ArrayList<KeySpec> row4 = new ArrayList<>();
        row4.add(KeySpec.action("letters", "ABC", ACTION_MODE_LETTERS).weight(18).contentDescription("Letras").build());
        row4.add(KeySpec.action("emoji", "", ACTION_EMOJI).icon(R.drawable.ic_emoji).weight(12).contentDescription("Emojis").build());
        row4.add(KeySpec.action("space", "ESPACIO", ACTION_SPACE).weight(40).contentDescription("Espacio").build());
        row4.add(text(".", ".").weight(9).build());
        row4.add(KeySpec.action("enter", "", ACTION_ENTER).icon(R.drawable.ic_enter).weight(17).contentDescription("Enter").build());
        rows.add(row4);
        return rows;
    }

    public static List<List<KeySpec>> programmer() {
        ArrayList<List<KeySpec>> rows = new ArrayList<>();
        rows.add(simpleTextRow(new String[]{"{","}","[","]","(",")","<",">",";",":"}));
        rows.add(simpleTextRow(new String[]{"+","-","*","/","_","|","`","~","\"","'"}));
        ArrayList<KeySpec> row3 = new ArrayList<>();
        row3.add(KeySpec.action("esc", "Esc", ACTION_ESC).weight(13).contentDescription("Escape").build());
        row3.add(KeySpec.action("tab", "Tab", ACTION_TAB).weight(13).contentDescription("Tabulador").build());
        row3.add(KeySpec.action("ctrl", "Ctrl", ACTION_CTRL).weight(13).contentDescription("Control").build());
        row3.add(KeySpec.action("alt", "Alt", ACTION_ALT).weight(13).contentDescription("Alt").build());
        row3.addAll(simpleTextRow(new String[]{"=", "!", "?", "@"}));
        row3.add(KeySpec.action("backspace", "", ACTION_BACKSPACE).icon(R.drawable.ic_backspace).weight(14).repeatable(true).contentDescription("Borrar").build());
        rows.add(row3);
        ArrayList<KeySpec> row4 = new ArrayList<>();
        row4.add(KeySpec.action("letters", "ABC", ACTION_MODE_LETTERS).weight(17).contentDescription("Letras").build());
        row4.add(KeySpec.action("numbers", "123", ACTION_MODE_NUMBERS).weight(13).contentDescription("Números").build());
        row4.add(KeySpec.action("space", "ESPACIO", ACTION_SPACE).weight(40).contentDescription("Espacio").build());
        row4.add(text(".", ".").weight(9).build());
        row4.add(KeySpec.action("enter", "", ACTION_ENTER).icon(R.drawable.ic_enter).weight(17).contentDescription("Enter").build());
        rows.add(row4);
        return rows;
    }

    private static List<KeySpec> simpleTextRow(String[] values) {
        ArrayList<KeySpec> row = new ArrayList<>();
        for (String value : values) row.add(text(value, value).alternatives(alternativesFor(value)).build());
        return row;
    }
}
