package com.example.shared;

public enum Tool {
    LINE("Line", "/icons/line.png"),
    TRIANGLE("Triangle", "/icons/triangle.png"),
    CIRCLE("Circle", "/icons/circle.png"),
    RECTANGLE("Rectangle", "/icons/rectangle.png"),
    FREE_DRAW("Free Draw", "/icons/draw.png"),
    TEXT("Text", "/icons/text.png"),
    SELECT("Select", "/icons/select.png");

    private final String displayName;
    private final String iconPath;

    Tool(String displayName, String iconPath) {
        this.displayName = displayName;
        this.iconPath = iconPath;
    }

    public String getDisplayName() { return displayName; }
    public String getIconPath() { return iconPath; }
}
