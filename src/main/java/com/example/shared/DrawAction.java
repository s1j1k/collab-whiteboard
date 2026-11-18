package com.example.shared;

import java.awt.Color;
import java.io.Serializable;

/**
 * @author Sally Arnold
 */
public class DrawAction implements Serializable {
    private final int x1, y1, x2, y2;
    private final Color color;
    private final int strokeSize;
    private final Tool tool;
    private final String text;

    public DrawAction(Tool tool, int x1, int y1, int x2, int y2, Color color, int strokeSize) {
        this.tool = tool;
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
        this.color = color;
        this.strokeSize = strokeSize;

        // Initialize unused fields
        text = null;
    }

    // Another constructor for drawing text
    public DrawAction(Tool tool, int x1, int y1, Color color, String text) {
        this.tool = tool;
        this.x1 = x1;
        this.y1 = y1;
        this.color = color;
        this.text = text;

        // Initialize unused fields
        strokeSize = 0;
        x2 = y2 = 0;
    }

    // FIXME should drawing logic live here?

    public Tool getTool() { return tool; }
    public int getX1() { return x1; }
    public int getY1() { return y1; }
    public int getX2() { return x2; }
    public int getY2() { return y2; }
    public Color getColor() { return color; }
    public int getStrokeSize() { return strokeSize; }
    public String getText() { return text; }
}
