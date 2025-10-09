package com.example.gui;

import javax.swing.*;
import java.awt.*;

public class IconUtils {
    public static ImageIcon loadScaledIcon(String path, int width, int height) {
        ImageIcon icon = new ImageIcon(IconUtils.class.getResource(path));
        Image img = icon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
        return new ImageIcon(img);
    }

    public static ImageIcon loadScaledIcon(String path) {
        super(path, 24, 24);
    }
}
