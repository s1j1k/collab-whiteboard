package com.example.gui;

import javax.imageio.ImageIO;
import javax.swing.*;

import com.example.shared.CursorPosition;
import com.example.shared.DrawAction;
import com.example.shared.Tool;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class WhiteBoardCanvas extends JPanel {

    private Consumer<DrawAction> drawListener;
    private Consumer<CursorPosition> cursorListener;

    private BufferedImage canvasImage;
    private Graphics2D g2d;
    private int prevX, prevY;
    private Tool currentTool;

    // Draw tool fields
    private int startX, startY;
    private int currentX, currentY;
    private int mouseX, mouseY;
    private boolean drawing = false;
    private int drawStrokeSize = 2;
    private int eraserStrokeSize = 10;

    // Username bubble fields
    private long lastSentTime = 0;
    private static final int THROTTLE_MS = 50;
    private final Map<String, Point> cursors = new ConcurrentHashMap<>();

    private String username;

    public WhiteBoardCanvas(int width, int height) {
        setPreferredSize(new Dimension(width, height));
        setBackground(Color.WHITE);
        setDoubleBuffered(true);
        setOpaque(true);
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
                        DrawAction action = new DrawAction(currentTool, startX, startY, g2d.getColor(), userInput);
                        if (drawListener != null)
                            drawListener.accept(action);
                        applyDrawAction(action);
                        drawing = false;
                        repaint();
                    }
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (drawing && currentTool.isShapeTool()) {
                    // Commit line/shape to the canvas image
                    // FIXME do other tools
                    DrawAction action = new DrawAction(currentTool, startX, startY, e.getX(), e.getY(), g2d.getColor(),
                            drawStrokeSize);
                    if (drawListener != null)
                        drawListener.accept(action);
                    applyDrawAction(action);
                    drawing = false;
                    repaint();
                }
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                // FIXME align with paint fn
                mouseX = e.getX();
                mouseY = e.getY();
                // Draw the username bubble
                repaint();
                long now = System.currentTimeMillis();
                if (now - lastSentTime >= THROTTLE_MS) {
                    lastSentTime = now;

                    // Create cursor update
                    CursorPosition pos = new CursorPosition(username, e.getX(), e.getY());

                    // Send to server
                    if (cursorListener != null) {
                        cursorListener.accept(pos);
                    }
                    // new Thread(() -> {
                    // try {
                    // // client.getServerRef().broadcastCursorPosition(pos);
                    // if (cursorListener != null)
                    // cursorListener.accept(pos);
                    // } catch (Exception ex) {
                    // ex.printStackTrace();
                    // }
                    // }).start();
                }
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                switch (currentTool) {
                    case FREE_DRAW:
                        // TODO send over to the remote server
                        int x = e.getX();
                        int y = e.getY();
                        // g2d.drawLine(prevX, prevY, x, y);
                        DrawAction action = new DrawAction(currentTool, prevX, prevY, x, y, g2d.getColor(),
                                drawStrokeSize);
                        if (drawListener != null)
                            drawListener.accept(action);
                        applyDrawAction(action);
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

                        // g2d.setStroke(new BasicStroke(eraserStrokeSize, BasicStroke.CAP_ROUND,
                        // BasicStroke.JOIN_ROUND));
                        // g2d.drawLine(prevX, prevY, x, y);

                        action = new DrawAction(currentTool, prevX, prevY, x, y, Color.WHITE,
                                eraserStrokeSize);
                        if (drawListener != null)
                            drawListener.accept(action);
                        applyDrawAction(action);
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
        // FIXME load from file if there is a file
        // FIXME remove canbas image
        canvasImage = new BufferedImage(800, 600, BufferedImage.TYPE_INT_ARGB);
        g2d = canvasImage.createGraphics();
        g2d.setColor(Color.BLACK);
        // Make the stroke have circular ends
        g2d.setStroke(new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        clear(); // fill with white initially
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    protected void paintComponent(Graphics g) {
        // Boilerplate code
        super.paintComponent(g);
        g.drawImage(canvasImage, 0, 0, null);

        // Display username bubble
        // drawUsernameBubble(g, username, mouseX, mouseY);
        // g.setColor(new Color(0, 0, 0, 100));
        // TODO improvement: make a different random color associated to each user
        for (var entry : cursors.entrySet()) {
            Point p = entry.getValue();
            // g.fillOval(p.x - 5, p.y - 5, 10, 10);
            // g.setColor(Color.WHITE);
            // g.drawString(entry.getKey(), p.x + 10, p.y);
            drawUsernameBubble(g, entry.getKey(), p.x, p.y);
        }

        // === Local preview for drawing tools ===

        // Draw line preview if currently drawing
        if (currentTool == Tool.LINE && drawing) {
            g.setColor(Color.GRAY); // preview color
            g.drawLine(startX, startY, currentX, currentY);
        }

        // Draw triangle preview if currently drawing
        if (currentTool == Tool.TRIANGLE && drawing) {
            g.setColor(Color.GRAY); // preview color
            drawTriangle(g, startX, startY, currentX, currentY);
        }

        // Draw circle preview if currently drawing
        if (currentTool == Tool.CIRCLE && drawing) {
            g.setColor(Color.GRAY); // preview color
            drawOval(g, startX, startY, currentX, currentY);
        }

        // Draw rectangle preview if currently drawing
        if (currentTool == Tool.RECTANGLE && drawing) {
            g.setColor(Color.GRAY); // preview color
            drawRect(g, startX, startY, currentX, currentY);
        }
    }

    // FIXME this isn't used / implemented
    public void clear() {
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, getWidth(), getHeight());
        g2d.setColor(Color.BLACK);
        repaint();
    }

    public void updateCursorPosition(CursorPosition pos) {
        cursors.put(pos.username, new Point(pos.x, pos.y));
        repaint();
        // FIXME debugging
        System.out.println("Cursor position updated: " + cursors);
    }

    // TODO update the client based on the server broadcast
    // FIXME add remote methods
    // public void drawRemoteLine(int x1, int y1, int x2, int y2) {
    // g2d.drawLine(x1, y1, x2, y2);
    // repaint();
    // }

    public void setDrawListener(Consumer<DrawAction> listener) {
        this.drawListener = listener;
    }

    public void setCursorListener(Consumer<CursorPosition> listener) {
        this.cursorListener = listener;
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
        if (currentTool == Tool.TEXT) {
            // Change to text input cursor
            setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
        } else {
            // Change to default cursor
            setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        }
    }

    public void drawTriangle(Graphics g, int x1, int y1, int x2, int y2) {
        int[] xPoints = { x1, x2, x1 + (x1 - x2) };
        int[] yPoints = { y1, y2, y2 };
        int numPoints = 3;
        g.drawPolygon(xPoints, yPoints, numPoints);
    }

    // FIXME make shorter
    public void drawOval(Graphics g, int x1, int y1, int x2, int y2) {
        int x, y;
        int xDia = Math.abs(x2 - x1);
        int yDia = Math.abs(x2 - x1);
        if (x1 > x2) {
            x = x2;
        } else {
            x = x1;
        }
        if (y2 < y1) {
            y = y2;
        } else {
            y = y1;
        }
        g.drawOval(x, y, xDia, yDia);
    }

    public void drawRect(Graphics g, int x1, int y1, int x2, int y2) {
        int x, y;
        int xDia = Math.abs(x2 - x1);
        int yDia = Math.abs(y2 - y1);
        if (x1 > x2) {
            x = x2;
        } else {
            x = x1;
        }
        if (y2 < y1) {
            y = y2;
        } else {
            y = y1;
        }
        g.drawRect(x, y, xDia, yDia);
    }

    // FIXME thread safety for server
    /**
     * Permanently draw onto the canvas
     * 
     * @param action
     */
    // TODO move this to drawAction
    public void applyDrawAction(DrawAction action) {
        // draw using Graphics2D
        Stroke originalStroke = g2d.getStroke();
        Color originalColor = g2d.getColor();
        g2d.setColor(action.getColor());
        g2d.setStroke(new BasicStroke(action.getStrokeSize(), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        switch (action.getTool()) {
            case LINE, FREE_DRAW, ERASE:
                g2d.drawLine(action.getX1(), action.getY1(), action.getX2(), action.getY2());
                repaint();
                break;

            case TRIANGLE:
                // FIXME use the coordinates from the action not the current setup of the canvas
                drawTriangle(g2d, action.getX1(), action.getY1(), action.getX2(), action.getY2());
                drawing = false;
                repaint();
                break;

            case CIRCLE:
                drawOval(g2d, action.getX1(), action.getY1(), action.getX2(), action.getY2());
                drawing = false;
                repaint();
                break;

            case RECTANGLE:
                drawRect(g2d, action.getX1(), action.getY1(), action.getX2(), action.getY2());
                drawing = false;
                repaint();
                break;

            case TEXT:
                // Display text input - text, x-coordinate, y-coordinate
                g2d.drawString(action.getText(), action.getX1(), action.getY1());
        }

        // Reset to original color and stroke
        g2d.setColor(originalColor);
        g2d.setStroke(originalStroke);

    }

    // FIXME ensure username is set
    private void drawUsernameBubble(Graphics g, String text, int x, int y) {
        FontMetrics fm = g.getFontMetrics();
        int textWidth = fm.stringWidth(text);
        int textHeight = fm.getHeight();

        int padding = 6;
        int bubbleWidth = textWidth + padding * 2;
        int bubbleHeight = textHeight;

        int bubbleX = x + 12; // offset so it's not under the cursor
        int bubbleY = y - 20;

        // bubble background
        g.setColor(new Color(0, 0, 0, 150));
        g.fillRoundRect(bubbleX, bubbleY, bubbleWidth, bubbleHeight, 10, 10);

        // text
        g.setColor(Color.WHITE);
        g.drawString(text, bubbleX + padding, bubbleY + textHeight - 4);
    }

    // FIXME convert to DrawAction objects which will also allow select & delete
    // objects
    // FIXME this scales the image, does it stretch it if the server GUI gets
    // stretched?
    public void loadFromFile(File file) throws IOException {
        BufferedImage backgroundImage = ImageIO.read(file);
        g2d.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), null);
        repaint();
    }

    public void saveCanvasToFile(File file) throws IOException {
        BufferedImage image = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        paint(g2);
        g2.dispose();
        ImageIO.write(image, "png", file);
    }

    public void loadCanvasImage(byte[] imageData) {
        try {
            canvasImage = ImageIO.read(new ByteArrayInputStream(imageData));
            // FIXME move this to the initialization part ?
            g2d.dispose();
            g2d = canvasImage.createGraphics();
            g2d.setColor(Color.BLACK);
            // Make the stroke have circular ends
            g2d.setStroke(new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public BufferedImage getCanvasImage() {
        return canvasImage;
    }

}
