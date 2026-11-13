package com.example.paint;

import javax.swing.JPanel;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayDeque;
import java.util.Deque;

public final class PaintCanvas extends JPanel {
    public enum Tool { PENCIL, ERASER, LINE, RECTANGLE, ELLIPSE }

    private final Deque<BufferedImage> undoStack = new ArrayDeque<>();
    private final Deque<BufferedImage> redoStack = new ArrayDeque<>();
    private BufferedImage image;
    private Tool tool = Tool.PENCIL;
    private Color drawingColor = Color.BLACK;
    private int strokeWidth = 1;
    private Point start;
    private Point current;
    private BufferedImage preview;

    public PaintCanvas() {
        setBackground(Color.WHITE);
        setPreferredSize(new Dimension(800, 560));
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent event) {
                ensureImage();
                saveUndoState();
                start = event.getPoint();
                current = start;
                preview = copy(image);
            }

            @Override
            public void mouseReleased(MouseEvent event) {
                if (start == null) {
                    return;
                }
                current = event.getPoint();
                if (tool == Tool.PENCIL || tool == Tool.ERASER) {
                    drawSegment(image, start, current);
                } else {
                    drawShape(image, start, current);
                }
                start = null;
                current = null;
                preview = null;
                repaint();
            }
        });
        addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent event) {
                if (start == null) {
                    return;
                }
                Point next = event.getPoint();
                if (tool == Tool.PENCIL || tool == Tool.ERASER) {
                    drawSegment(image, current, next);
                    current = next;
                } else {
                    current = next;
                    image = copy(preview);
                    drawShape(image, start, current);
                }
                repaint();
            }
        });
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        ensureImage();
        graphics.drawImage(image, 0, 0, null);
    }

    public void setTool(Tool tool) {
        this.tool = tool;
    }

    public void setDrawingColor(Color color) {
        drawingColor = color;
    }

    public Color getDrawingColor() {
        return drawingColor;
    }

    public void setStrokeWidth(int width) {
        strokeWidth = Math.max(1, width);
    }

    public BufferedImage getImage() {
        ensureImage();
        return copy(image);
    }

    public void setImage(BufferedImage loadedImage) {
        saveUndoState();
        image = copy(loadedImage);
        redoStack.clear();
        revalidate();
        repaint();
    }

    public void undo() {
        if (undoStack.isEmpty()) {
            return;
        }
        redoStack.push(copy(image));
        image = undoStack.pop();
        repaint();
    }

    public void redo() {
        if (redoStack.isEmpty()) {
            return;
        }
        undoStack.push(copy(image));
        image = redoStack.pop();
        repaint();
    }

    public void clear() {
        ensureImage();
        saveUndoState();
        Graphics2D graphics = image.createGraphics();
        graphics.setColor(Color.WHITE);
        graphics.fillRect(0, 0, image.getWidth(), image.getHeight());
        graphics.dispose();
        redoStack.clear();
        repaint();
    }

    private void ensureImage() {
        if (image != null && image.getWidth() == getWidth() && image.getHeight() == getHeight()) {
            return;
        }
        int width = Math.max(getWidth(), 800);
        int height = Math.max(getHeight(), 560);
        BufferedImage resized = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = resized.createGraphics();
        graphics.setColor(Color.WHITE);
        graphics.fillRect(0, 0, width, height);
        if (image != null) {
            graphics.drawImage(image, 0, 0, null);
        }
        graphics.dispose();
        image = resized;
    }

    private void saveUndoState() {
        ensureImage();
        undoStack.push(copy(image));
        if (undoStack.size() > 30) {
            undoStack.removeLast();
        }
        redoStack.clear();
    }

    private void drawSegment(BufferedImage target, Point from, Point to) {
        Graphics2D graphics = target.createGraphics();
        configureGraphics(graphics);
        graphics.setColor(tool == Tool.ERASER ? Color.WHITE : drawingColor);
        graphics.drawLine(from.x, from.y, to.x, to.y);
        graphics.dispose();
    }

    private void drawShape(BufferedImage target, Point first, Point second) {
        Graphics2D graphics = target.createGraphics();
        configureGraphics(graphics);
        graphics.setColor(drawingColor);
        int x = Math.min(first.x, second.x);
        int y = Math.min(first.y, second.y);
        int width = Math.abs(first.x - second.x);
        int height = Math.abs(first.y - second.y);
        switch (tool) {
            case LINE -> graphics.drawLine(first.x, first.y, second.x, second.y);
            case RECTANGLE -> graphics.drawRect(x, y, width, height);
            case ELLIPSE -> graphics.drawOval(x, y, width, height);
            default -> {
            }
        }
        graphics.dispose();
    }

    private void configureGraphics(Graphics2D graphics) {
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.setStroke(new BasicStroke(strokeWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
    }

    private BufferedImage copy(BufferedImage source) {
        BufferedImage copy = new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics graphics = copy.getGraphics();
        graphics.drawImage(source, 0, 0, null);
        graphics.dispose();
        return copy;
    }
}
