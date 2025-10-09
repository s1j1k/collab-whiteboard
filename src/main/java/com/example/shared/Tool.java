package com.example.shared;

public enum Tool {
    LINE("Line", "/icons/line.png"),
    RECTANGLE("Rectangle", "/icons/rectangle.png"),
    OVAL("Oval", "/icons/oval.png"),
    FREE_DRAW("Free Draw", "/icons/free_draw.png"),
    TEXT("Text", "/icons/text.png");

    private final String displayName;
    private final String iconPath;

    Tool(String displayName, String iconPath) {
        this.displayName = displayName;
        this.iconPath = iconPath;
    }

    public String getDisplayName() { return displayName; }
    public String getIconPath() { return iconPath; }
}
