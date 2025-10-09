package com.example.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class ToolBarPanel extends JPanel {

    private JButton shapesButton;
    private JButton clearButton;
    private JButton colorButton;

    public ToolBarPanel(ActionListener listener) {
        setLayout(new FlowLayout(FlowLayout.LEFT));

        // TODO Shapes: your whiteboard should at least support line, triangle, circle,
        // and rectangle.
        JToolBar toolBar = new JToolBar();

        JToggleButton lineBtn = new JToggleButton(IconUtils.loadScaledIcon("/icons/icons8-line-50.png", 24, 24));
        JToggleButton triangleBtn = new JToggleButton(new ImageIcon(getClass().getResource("/icons/icons8-triangle-24.png")));
        JToggleButton circleBtn = new JToggleButton(new ImageIcon(getClass().getResource("/icons/icons8-circle-50.png")));
        JToggleButton rectBtn = new JToggleButton(new ImageIcon(getClass().getResource("/icons/icons8-rectangle-48.png")));
        JToggleButton freeDrawBtn = new JToggleButton(new ImageIcon(getClass().getResource("/icons/icons8-draw-50.png")));
        JToggleButton textBtn = new JToggleButton(new ImageIcon(getClass().getResource("/icons/icons8-text-24.png")));
        // TODO add object selection or move items (hand icon)
        // TODO add eraser


        ButtonGroup shapeGroup = new ButtonGroup();
        shapeGroup.add(lineBtn);
        shapeGroup.add(triangleBtn);
        shapeGroup.add(circleBtn);
        shapeGroup.add(rectBtn);
        shapeGroup.add(freeDrawBtn);
        shapeGroup.add(textBtn);

        toolBar.add(lineBtn);
        toolBar.add(triangleBtn);
        toolBar.add(circleBtn);
        toolBar.add(rectBtn);
        toolBar.add(freeDrawBtn);
        toolBar.add(textBtn);

        add(toolBar);

        // TODO add button for free drawing
        // TODO add icons for buttons
        // ImageIcon icon = new ImageIcon("path/to/myIcon.png");
        // JButton shapesButton = new JButton(IconUtils.loadScaledIcon("/icons/icons8-shapes-64.png", 24, 24));
        // ImageIcon pencilIcon = new ImageIcon(
        // getClass().getResource("/icons/pencil.png")
        // );

        // FIXME add alt text for accessibility
        // TODO add tooltip text instead of text labels

        clearButton = new JButton("Clear");
        colorButton = new JButton("Color", new ImageIcon(getClass().getResource("/icons/colors.png")));

        clearButton.setActionCommand("CLEAR");
        colorButton.setActionCommand("COLOR");

        clearButton.addActionListener(listener);
        colorButton.addActionListener(listener);

        // FIXME do this to all
        // shapesButton.setFocusable(false);
        // shapesButton.setBorderPainted(false);
        // shapesButton.setContentAreaFilled(false);
        // shapesButton.setOpaque(false);

        // add(shapesButton);
        add(clearButton);
        add(colorButton);
    }
}
