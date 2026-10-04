package com.arena.customkeyboard.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Immutable description of a visible keyboard key.
 * The IME engine owns the behavior, the view only renders this model.
 */
public final class KeySpec {
    public static final int TYPE_TEXT = 0;
    public static final int TYPE_ACTION = 1;
    public static final int TYPE_SPACER = 2;

    public final String id;
    public final String label;
    public final String output;
    public final String action;
    public final int weight;
    public final int type;
    public final int iconRes;
    public final boolean repeatable;
    public final boolean special;
    public final List<String> alternatives;
    public final String contentDescription;

    private KeySpec(Builder builder) {
        id = builder.id;
        label = builder.label;
        output = builder.output;
        action = builder.action;
        weight = builder.weight;
        type = builder.type;
        iconRes = builder.iconRes;
        repeatable = builder.repeatable;
        special = builder.special;
        alternatives = Collections.unmodifiableList(new ArrayList<>(builder.alternatives));
        contentDescription = builder.contentDescription == null ? label : builder.contentDescription;
    }

    public boolean isAction(String value) {
        return TYPE_ACTION == type && action != null && action.equals(value);
    }

    public static Builder text(String id, String label, String output) {
        return new Builder(id).label(label).output(output).type(TYPE_TEXT);
    }

    public static Builder action(String id, String label, String action) {
        return new Builder(id).label(label).action(action).type(TYPE_ACTION).special(true);
    }

    public static Builder spacer(String id, int weight) {
        return new Builder(id).label("").type(TYPE_SPACER).weight(weight);
    }

    public static final class Builder {
        private final String id;
        private String label = "";
        private String output = "";
        private String action;
        private int weight = 10;
        private int type = TYPE_TEXT;
        private int iconRes = 0;
        private boolean repeatable = false;
        private boolean special = false;
        private List<String> alternatives = new ArrayList<>();
        private String contentDescription;

        public Builder(String id) {
            this.id = id;
        }

        public Builder label(String value) { label = value; return this; }
        public Builder output(String value) { output = value; return this; }
        public Builder action(String value) { action = value; return this; }
        public Builder weight(int value) { weight = value; return this; }
        public Builder type(int value) { type = value; return this; }
        public Builder icon(int value) { iconRes = value; return this; }
        public Builder repeatable(boolean value) { repeatable = value; return this; }
        public Builder special(boolean value) { special = value; return this; }
        public Builder contentDescription(String value) { contentDescription = value; return this; }
        public Builder alternatives(String... values) { alternatives = Arrays.asList(values); return this; }
        public Builder alternatives(List<String> values) { alternatives = values; return this; }
        public KeySpec build() { return new KeySpec(this); }
    }
}
