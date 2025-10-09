package com.example.gui;

import javax.swing.*;

import com.example.shared.Tool;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class WhiteBoardCanvas extends JPanel {

    private BufferedImage canvasImage;
    private Graphics2D g2d;
    private int prevX, prevY;
    private Tool currentTool;

    // Line tool fields
    private int startX, startY;
    private int currentX, currentY;
    private boolean drawingLine = false;

    public WhiteBoardCanvas(int width, int height) {
        setPreferredSize(new Dimension(width, height));
        setBackground(Color.WHITE);
        setDoubleBuffered(true);
        initCanvas();
        // TODO handle other tools
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (currentTool == Tool.LINE) {
                    startX = e.getX();
                    startY = e.getY();
                    drawingLine = true;
                    currentX = startX;
                    currentY = startY;
                } else if (currentTool == Tool.FREE_DRAW) {
                    prevX = e.getX();
                    prevY = e.getY();
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (currentTool == Tool.LINE && drawingLine) {
                    // Commit line to the canvas image
                    g2d.drawLine(startX, startY, e.getX(), e.getY());
                    drawingLine = false;
                    repaint();
                }
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                switch (currentTool) {
                    case FREE_DRAW:
                        int x = e.getX();
                        int y = e.getY();
                        g2d.drawLine(prevX, prevY, x, y);
                        repaint();
                        prevX = x;
                        prevY = y;
                        break;

                    case LINE:
                        // Update current mouse position for preview
                        currentX = e.getX();
                        currentY = e.getY();
                        repaint(); // will trigger paintComponent to show preview
                        break;
                }
                // TODO notify network layer of new stroke segment
            }
        });
    }

    private void initCanvas() {
        canvasImage = new BufferedImage(800, 600, BufferedImage.TYPE_INT_ARGB);
        g2d = canvasImage.createGraphics();
        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(2));
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        clear(); // fill with white initially
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(canvasImage, 0, 0, null);

        // Draw line preview if currently drawing
        if (currentTool == Tool.LINE && drawingLine) {
            g.setColor(Color.GRAY); // preview color
            g.drawLine(startX, startY, currentX, currentY);
        }
    }

    public void clear() {
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, getWidth(), getHeight());
        g2d.setColor(Color.BLACK);
        repaint();
    }

    public void drawRemoteLine(int x1, int y1, int x2, int y2) {
        g2d.drawLine(x1, y1, x2, y2);
        repaint();
    }

    public void setDrawingColor(Color newColor) {
        g2d.setColor(newColor);
    }

    public void setCurrentTool(Tool newTool) {
        currentTool = newTool;
    }
}
