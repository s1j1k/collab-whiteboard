package com.example.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class WhiteBoardCanvas extends JPanel {

    private BufferedImage canvasImage;
    private Graphics2D g2d;
    private int prevX, prevY;
    private boolean drawing = false;

    public WhiteBoardCanvas(int width, int height) {
        setPreferredSize(new Dimension(width, height));
        setBackground(Color.WHITE);
        setDoubleBuffered(true);
        initCanvas();
        

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                drawing = true;
                prevX = e.getX();
                prevY = e.getY();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                drawing = false;
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (drawing) {
                    int x = e.getX();
                    int y = e.getY();
                    g2d.drawLine(prevX, prevY, x, y);
                    repaint();
                    prevX = x;
                    prevY = y;
                    // TODO: Notify network layer of new stroke segment
                }
            }
        });
    }

    private void initCanvas() {
        canvasImage = new BufferedImage(800, 600, BufferedImage.TYPE_INT_ARGB);
        g2d = canvasImage.createGraphics();
        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(2));
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(canvasImage, 0, 0, null);
    }

    public void clear() {
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, getWidth(), getHeight());
        g2d.setColor(Color.BLACK);
        repaint();
    }

    // Called by server/client logic when updates arrive
    public void drawRemoteLine(int x1, int y1, int x2, int y2) {
        g2d.drawLine(x1, y1, x2, y2);
        repaint();
    }

    // Change drawing colour
    public void setDrawingColor(Color newColor) {
        g2d.setColor(newColor);
    }
}
