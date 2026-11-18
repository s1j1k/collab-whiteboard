package com.example.gui;

import javax.swing.*;
import java.awt.*;

/**
 * 
 * Helper class to load and resize icons from resources
 * 
 * @author Sally Arnold
 */
public class IconUtils {
    public static ImageIcon loadScaledIcon(String path, int width, int height) {
        ImageIcon icon = new ImageIcon(IconUtils.class.getResource(path));
        Image img = icon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
        return new ImageIcon(img);
    }
}
