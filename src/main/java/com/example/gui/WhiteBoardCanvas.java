package com.example.gui;

import javax.swing.*;

import com.example.shared.DrawAction;
import com.example.shared.Tool;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.function.Consumer;

public class WhiteBoardCanvas extends JPanel {

    private Consumer<DrawAction> drawListener;

    private BufferedImage canvasImage;
    private Graphics2D g2d;
    private int prevX, prevY;
    private Tool currentTool;

    // Line tool fields
    private int startX, startY;
    private int currentX, currentY;
    private boolean drawing = false;

    private int drawStrokeSize = 2;
    private int eraserStrokeSize = 10;

    public WhiteBoardCanvas(int width, int height) {
        setPreferredSize(new Dimension(width, height));
        setBackground(Color.WHITE);
        setDoubleBuffered(true);
        initCanvas();
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                // Save the location pressed
                startX = e.getX();
                startY = e.getY();
                drawing = true;
                currentX = startX;
                currentY = startY;
                prevX = e.getX();
                prevY = e.getY();

                if (currentTool == Tool.TEXT) {
                    // Display an input dialog and get the user's input as a String
                    String userInput = JOptionPane.showInputDialog("Enter your text here:");

                    // Check if the user entered something or cancelled the dialog
                    if (userInput != null && !userInput.isEmpty()) {
                        // Display text input - text, x-coordinate, y-coordinate
                        g2d.drawString(userInput, currentX, currentY);
                        repaint();
                    }
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                // TODO convert to draw action syntax
                if (currentTool == Tool.LINE && drawing) {
                    // Commit line to the canvas image
                    // FIXME do other tools
                    DrawAction action = new DrawAction(currentTool, startX, startY, e.getX(), e.getY(), g2d.getColor(),
                            drawStrokeSize);
                    if (drawListener != null)
                        drawListener.accept(action);
                    // TODO use same interface
                    applyDrawAction(action);
                    drawing = false;
                    repaint();
                }
                // FIXME handle within drraw action & don't check which tool here
                if (currentTool == Tool.TRIANGLE && drawing) {
                    // Commit triangle to the canvas image
                    drawTriangle(g2d);
                    drawing = false;
                    repaint();
                }
                if (currentTool == Tool.CIRCLE && drawing) {
                    // Commit oval to the canvas image
                    drawOval(g2d);
                    drawing = false;
                    repaint();
                }
                if (currentTool == Tool.RECTANGLE && drawing) {
                    // Commit rectangle to the canvas image
                    drawRect(g2d);
                    drawing = false;
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

                    case TRIANGLE, CIRCLE, RECTANGLE, LINE:
                        // Update current mouse position for preview
                        currentX = e.getX();
                        currentY = e.getY();
                        repaint(); // will trigger paintComponent to show preview
                        break;

                    case ERASE:
                        x = e.getX();
                        y = e.getY();
                        Color originalColor = g2d.getColor();
                        // TODO use getBackground() and support themes
                        g2d.setColor(Color.WHITE);
                        g2d.setStroke(new BasicStroke(eraserStrokeSize, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                        g2d.drawLine(prevX, prevY, x, y);
                        g2d.setColor(originalColor);
                        repaint();
                        prevX = x;
                        prevY = y;
                        break;
                }
                // TODO notify network layer of new stroke segment for free draw & erase
            }
        });
    }

    public Tool getCurrentTool() {
        return currentTool;
    }

    private void initCanvas() {
        canvasImage = new BufferedImage(800, 600, BufferedImage.TYPE_INT_ARGB);
        g2d = canvasImage.createGraphics();
        g2d.setColor(Color.BLACK);
        // g2d.setStroke(new BasicStroke(2));
        // Have circle ends
        g2d.setStroke(new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        clear(); // fill with white initially
    }

    @Override
    protected void paintComponent(Graphics g) {
        // Boilerplate code
        super.paintComponent(g);
        g.drawImage(canvasImage, 0, 0, null);

        // === Preview for drawing tools ===

        // Draw line preview if currently drawing
        if (currentTool == Tool.LINE && drawing) {
            g.setColor(Color.GRAY); // preview color
            g.drawLine(startX, startY, currentX, currentY);
        }

        // Draw triangle preview if currently drawing
        if (currentTool == Tool.TRIANGLE && drawing) {
            g.setColor(Color.GRAY); // preview color
            drawTriangle(g);
        }

        // Draw circle preview if currently drawing
        if (currentTool == Tool.CIRCLE && drawing) {
            g.setColor(Color.GRAY); // preview color
            drawOval(g);
        }

        // Draw rectangle preview if currently drawing
        if (currentTool == Tool.RECTANGLE && drawing) {
            g.setColor(Color.GRAY); // preview color
            drawRect(g);
        }
    }

    // FIXME this isn't used / implemented
    public void clear() {
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, getWidth(), getHeight());
        g2d.setColor(Color.BLACK);
        repaint();
    }

    // FIXME add remote methods
    // public void drawRemoteLine(int x1, int y1, int x2, int y2) {
    // g2d.drawLine(x1, y1, x2, y2);
    // repaint();
    // }

    public void setDrawListener(Consumer<DrawAction> listener) {
        this.drawListener = listener;
    }

    public void setDrawingColor(Color newColor) {
        g2d.setColor(newColor);
    }

    public void setDrawStrokeSize(int size) {
        this.drawStrokeSize = size;
        g2d.setStroke(new BasicStroke(size, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
    }

    public void setEraserStrokeSize(int size) {
        this.eraserStrokeSize = size;
    }

    public int getDrawStrokeSize() {
        return drawStrokeSize;
    }

    public int getEraserStrokeSize() {
        return eraserStrokeSize;
    }

    public void setCurrentTool(Tool newTool) {
        currentTool = newTool;

        // TODO update stroke thickness

        // TODO update the stroke thickness toggle when changing tools

        if (currentTool == Tool.TEXT) {
            // Change to text input cursor
            setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
        } else {
            // Change to default cursor
            setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        }
    }

    public void drawTriangle(Graphics g) {
        int[] xPoints = { startX, currentX, startX + (startX - currentX) };
        int[] yPoints = { startY, currentY, currentY };
        int numPoints = 3;
        g.drawPolygon(xPoints, yPoints, numPoints);
    }

    public void drawOval(Graphics g) {
        int x, y;
        int xDia, yDia;
        xDia = Math.abs(currentX - startX);
        yDia = Math.abs(currentY - startY);
        if (startX > currentX) {
            x = currentX;
        } else {
            x = startX;
        }
        if (currentY < startY) {
            y = currentY;
        } else {
            y = startY;
        }
        g.drawOval(x, y, xDia, yDia);
    }

    public void drawRect(Graphics g) {
        int x, y;
        int xDia, yDia;
        xDia = Math.abs(currentX - startX);
        yDia = Math.abs(currentY - startY);
        if (startX > currentX) {
            x = currentX;
        } else {
            x = startX;
        }
        if (currentY < startY) {
            y = currentY;
        } else {
            y = startY;
        }
        g.drawRect(x, y, xDia, yDia);
    }

    // TODO implement this for other tools
    // FIXME thread safety for server
    public void applyDrawAction(DrawAction action) {

        // draw using Graphics2D
        Stroke originalStroke = g2d.getStroke();
        Color originalColor = g2d.getColor();
        g2d.setColor(action.getColor());
        g2d.setStroke(new BasicStroke(action.getStrokeSize(), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        switch (action.getTool()) {
            case LINE:
                g2d.drawLine(action.getX1(), action.getY1(), action.getX2(), action.getY2());
                repaint();
                break;
        }

        g2d.setColor(originalColor);
        g2d.setStroke(originalStroke);

    }

}
