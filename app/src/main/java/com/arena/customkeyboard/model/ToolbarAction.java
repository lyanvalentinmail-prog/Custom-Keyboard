package com.arena.customkeyboard.model;

public final class ToolbarAction {
    public final String id;
    public final String title;
    public final int iconRes;
    public final boolean privacySensitive;

    public ToolbarAction(String id, String title, int iconRes, boolean privacySensitive) {
        this.id = id;
        this.title = title;
        this.iconRes = iconRes;
        this.privacySensitive = privacySensitive;
    }
}
