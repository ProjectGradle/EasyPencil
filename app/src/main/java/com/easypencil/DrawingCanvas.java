package com.easypencil;

import java.io.File;
import java.util.ArrayDeque;
import java.util.Deque;

import com.easypencil.Widget.FloatingTextBox;
import com.easypencil.util.ScreenCaptureUtil;

import javafx.scene.Cursor;
import javafx.scene.ImageCursor;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;
import javafx.scene.transform.Scale;

public class DrawingCanvas extends Pane {

    public enum DrawMode {
        PEN, HIGHLIGHT, ERASER, TEXT,
        SHAPE_LINE, SHAPE_RECT, SHAPE_CIRCLE, SHAPE_TRIANGLE
    }

    private final Canvas canvas;
    private final GraphicsContext gc;
    private final Deque<WritableImage> undoStack = new ArrayDeque<>();

    private Color brushColor = Color.RED;
    private double brushSize = 4.0;

    private DrawMode drawMode = DrawMode.PEN;

    private FloatingTextBox activeTextBox = null;

    private Cursor penCursor = Cursor.DEFAULT;
    private Cursor highlightCursor = Cursor.DEFAULT;
    private Cursor eraserCursor = Cursor.DEFAULT;
    private Cursor textCursor = Cursor.TEXT;

    // Track start point for shapes and SHIFT straight lines
    private double startX, startY;

    // Zoom state
    private double zoomLevel = 1.0;
    private final Scale scaleTransform;
    private final double screenW;
    private final double screenH;

    public DrawingCanvas() {
        screenW = javafx.stage.Screen.getPrimary().getBounds().getWidth();
        screenH = javafx.stage.Screen.getPrimary().getBounds().getHeight();

        canvas = new Canvas(screenW, screenH);
        gc = canvas.getGraphicsContext2D();
        gc.setLineJoin(StrokeLineJoin.ROUND);

        // Zoom transform pivots at screen center so zoom feels natural
        scaleTransform = new Scale(1.0, 1.0, screenW / 2, screenH / 2);
        canvas.getTransforms().add(scaleTransform);

        setPickOnBounds(false);
        canvas.setPickOnBounds(true);
        canvas.setMouseTransparent(false);

        getChildren().add(canvas);

        loadCursors();
        setupMouseEvents();
    }

    private void loadCursors() {
        try {
            String penUrl = getClass().getResource("/asset/pencil.png").toExternalForm();
            penCursor = new ImageCursor(new Image(penUrl), 0, 512);

            String highlightUrl = getClass().getResource("/asset/highlighter.png").toExternalForm();
            highlightCursor = new ImageCursor(new Image(highlightUrl), 0, 512);

            String textUrl = getClass().getResource("/asset/text_icon.png").toExternalForm();
            textCursor = new ImageCursor(new Image(textUrl), 0, 0);

            String eraserUrl = getClass().getResource("/asset/eraser.png").toExternalForm();
            Image eraserImg = new Image(eraserUrl);
            eraserCursor = new ImageCursor(eraserImg, eraserImg.getWidth() / 2, eraserImg.getHeight() / 2);

        } catch (Exception e) {
            System.err.println("ไม่สามารถโหลดไฟล์เคอร์เซอร์ได้: " + e.getMessage());
            penCursor = Cursor.CROSSHAIR;
            highlightCursor = Cursor.CROSSHAIR;
            textCursor = Cursor.TEXT;
            eraserCursor = Cursor.CLOSED_HAND;
        }
    }

    private void setupMouseEvents() {
        canvas.setOnMousePressed(e -> {
            if (drawMode == DrawMode.TEXT) {
                if (activeTextBox != null) {
                    finalizeText();
                    return;
                }
                activeTextBox = new FloatingTextBox(e.getX(), e.getY(), brushColor, brushSize, this::finalizeText);
                this.getChildren().add(activeTextBox);
                activeTextBox.focusText();
                return;
            }

            saveSnapshot();
            startX = e.getX();
            startY = e.getY();

            gc.setStroke(brushColor);
            gc.setFill(brushColor);

            if (drawMode == DrawMode.HIGHLIGHT) {
                gc.setGlobalAlpha(0.4);
                gc.setLineWidth(brushSize * 3);
            } else {
                gc.setGlobalAlpha(1.0);
                gc.setLineWidth(brushSize);
            }
            gc.setLineCap(StrokeLineCap.ROUND);

            // PEN/HIGHLIGHT: begin the path stroke; a click with no drag draws a dot
            if (drawMode == DrawMode.PEN || drawMode == DrawMode.HIGHLIGHT) {
                gc.beginPath();
                gc.moveTo(startX, startY);
                gc.stroke();
            }
        });

        canvas.setOnMouseDragged(e -> {
            if (drawMode == DrawMode.TEXT) return;

            double curX = e.getX();
            double curY = e.getY();

            switch (drawMode) {
                case ERASER -> {
                    gc.setGlobalAlpha(1.0);
                    gc.clearRect(curX - brushSize * 2, curY - brushSize * 2, brushSize * 4, brushSize * 4);
                }

                case PEN -> {
                    restoreSnapshot();
                    gc.setGlobalAlpha(1.0);
                    gc.setLineWidth(brushSize);
                    gc.setLineCap(StrokeLineCap.ROUND);
                    gc.setStroke(brushColor);
                    if (e.isShiftDown()) {
                        // Straight line snapped to H / V / 45°
                        double[] snapped = snapToAxis(startX, startY, curX, curY);
                        gc.strokeLine(startX, startY, snapped[0], snapped[1]);
                        // Reset path anchor so releasing SHIFT resumes from start
                        gc.beginPath();
                        gc.moveTo(startX, startY);
                    } else {
                        gc.lineTo(curX, curY);
                        gc.stroke();
                    }
                }

                case HIGHLIGHT -> {
                    restoreSnapshot();
                    gc.setGlobalAlpha(0.4);
                    gc.setLineWidth(brushSize * 3);
                    gc.setLineCap(StrokeLineCap.ROUND);
                    gc.setStroke(brushColor);
                    if (e.isShiftDown()) {
                        double[] snapped = snapToAxis(startX, startY, curX, curY);
                        gc.strokeLine(startX, startY, snapped[0], snapped[1]);
                        gc.beginPath();
                        gc.moveTo(startX, startY);
                    } else {
                        gc.lineTo(curX, curY);
                        gc.stroke();
                    }
                }

                case SHAPE_LINE -> {
                    restoreSnapshot();
                    gc.setGlobalAlpha(1.0);
                    gc.setLineWidth(brushSize);
                    gc.setLineCap(StrokeLineCap.ROUND);
                    gc.setStroke(brushColor);
                    double[] end = e.isShiftDown() ? snapToAxis(startX, startY, curX, curY)
                                                   : new double[]{curX, curY};
                    gc.strokeLine(startX, startY, end[0], end[1]);
                }

                case SHAPE_RECT -> {
                    restoreSnapshot();
                    gc.setGlobalAlpha(1.0);
                    gc.setLineWidth(brushSize);
                    gc.setStroke(brushColor);
                    double rx = Math.min(startX, curX);
                    double ry = Math.min(startY, curY);
                    double rw = Math.abs(curX - startX);
                    double rh = Math.abs(curY - startY);
                    if (e.isShiftDown()) {
                        // Square: use the smaller side
                        double side = Math.min(rw, rh);
                        rx = startX < curX ? startX : startX - side;
                        ry = startY < curY ? startY : startY - side;
                        rw = side;
                        rh = side;
                    }
                    gc.strokeRect(rx, ry, rw, rh);
                }

                case SHAPE_CIRCLE -> {
                    restoreSnapshot();
                    gc.setGlobalAlpha(1.0);
                    gc.setLineWidth(brushSize);
                    gc.setStroke(brushColor);
                    double ox = Math.min(startX, curX);
                    double oy = Math.min(startY, curY);
                    double ow = Math.abs(curX - startX);
                    double oh = Math.abs(curY - startY);
                    if (e.isShiftDown()) {
                        // Perfect circle: use the smaller dimension
                        double side = Math.min(ow, oh);
                        ox = startX < curX ? startX : startX - side;
                        oy = startY < curY ? startY : startY - side;
                        ow = side;
                        oh = side;
                    }
                    gc.strokeOval(ox, oy, ow, oh);
                }

                case SHAPE_TRIANGLE -> {
                    restoreSnapshot();
                    gc.setGlobalAlpha(1.0);
                    gc.setLineWidth(brushSize);
                    gc.setLineCap(StrokeLineCap.ROUND);
                    gc.setStroke(brushColor);
                    drawTrianglePreview(startX, startY, curX, curY);
                }
            }
        });

        canvas.setOnMouseReleased(e -> {
            if (drawMode == DrawMode.TEXT) return;
            if (drawMode == DrawMode.PEN || drawMode == DrawMode.HIGHLIGHT) {
                gc.closePath();
            }
        });
    }

    // Restore the top-of-stack snapshot onto the canvas (for shape preview redraw)
    private void restoreSnapshot() {
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        gc.setGlobalAlpha(1.0);
        if (!undoStack.isEmpty()) {
            gc.drawImage(undoStack.peek(), 0, 0);
        }
    }

    // Snap endpoint to horizontal, vertical, or 45° diagonal relative to start
    private double[] snapToAxis(double x1, double y1, double x2, double y2) {
        double dx = Math.abs(x2 - x1);
        double dy = Math.abs(y2 - y1);
        if (dx > dy * 2) {
            return new double[]{x2, y1};           // Horizontal
        } else if (dy > dx * 2) {
            return new double[]{x1, y2};           // Vertical
        } else {
            double len = Math.min(dx, dy);          // 45°
            double sx = x2 > x1 ? 1 : -1;
            double sy = y2 > y1 ? 1 : -1;
            return new double[]{x1 + len * sx, y1 + len * sy};
        }
    }

    // Triangle: apex at (startX, startY), base between (startX adjusted, curY) and (curX, curY)
    private void drawTrianglePreview(double x1, double y1, double x2, double y2) {
        double cx = (x1 + x2) / 2.0;
        double[] xs = {cx, x1, x2};
        double[] ys = {y1, y2, y2};
        gc.strokePolygon(xs, ys, 3);
    }

    private void saveSnapshot() {
        SnapshotParameters params = new SnapshotParameters();
        params.setFill(Color.TRANSPARENT);
        WritableImage snapshot = canvas.snapshot(params, null);
        undoStack.push(snapshot);
        if (undoStack.size() > 30) {
            undoStack.pollLast();
        }
    }

    private void finalizeText() {
        if (activeTextBox != null && !activeTextBox.getText().trim().isEmpty()) {
            saveSnapshot();
            gc.setGlobalAlpha(1.0);
            gc.setFill(brushColor);
            double fontSize = Math.max(16, brushSize * 4);
            gc.setFont(javafx.scene.text.Font.font("System", javafx.scene.text.FontWeight.BOLD, fontSize));
            gc.fillText(activeTextBox.getText(), activeTextBox.getStampX(), activeTextBox.getStampY());
        }
        if (activeTextBox != null) {
            this.getChildren().remove(activeTextBox);
            activeTextBox = null;
        }
    }

    // ── Mode setters ────────────────────────────────────────────────────────

    public void setPenMode() {
        drawMode = DrawMode.PEN;
        setCursor(penCursor);
        if (activeTextBox != null) finalizeText();
    }

    public void setHighlightMode() {
        drawMode = DrawMode.HIGHLIGHT;
        setCursor(highlightCursor);
        if (activeTextBox != null) finalizeText();
    }

    public void setTextMode() {
        drawMode = DrawMode.TEXT;
        setCursor(textCursor);
    }

    public void setEraserMode() {
        drawMode = DrawMode.ERASER;
        setCursor(eraserCursor);
        if (activeTextBox != null) finalizeText();
    }

    public void setLineMode() {
        drawMode = DrawMode.SHAPE_LINE;
        setCursor(Cursor.CROSSHAIR);
        if (activeTextBox != null) finalizeText();
    }

    public void setRectMode() {
        drawMode = DrawMode.SHAPE_RECT;
        setCursor(Cursor.CROSSHAIR);
        if (activeTextBox != null) finalizeText();
    }

    public void setCircleMode() {
        drawMode = DrawMode.SHAPE_CIRCLE;
        setCursor(Cursor.CROSSHAIR);
        if (activeTextBox != null) finalizeText();
    }

    public void setTriangleMode() {
        drawMode = DrawMode.SHAPE_TRIANGLE;
        setCursor(Cursor.CROSSHAIR);
        if (activeTextBox != null) finalizeText();
    }

    // ── Zoom ────────────────────────────────────────────────────────────────

    public void zoomIn() {
        zoomLevel = Math.min(zoomLevel * 1.25, 5.0);
        applyZoom();
    }

    public void zoomOut() {
        zoomLevel = Math.max(zoomLevel / 1.25, 0.2);
        applyZoom();
    }

    public void resetZoom() {
        zoomLevel = 1.0;
        applyZoom();
    }

    private void applyZoom() {
        scaleTransform.setX(zoomLevel);
        scaleTransform.setY(zoomLevel);
    }

    public double getZoomLevel() {
        return zoomLevel;
    }

    // ── Actions ─────────────────────────────────────────────────────────────

    public void undo() {
        if (!undoStack.isEmpty()) {
            gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
            gc.setGlobalAlpha(1.0);
            gc.drawImage(undoStack.pop(), 0, 0);
        }
    }

    public void clearCanvas() {
        saveSnapshot();
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
    }

    public void setBrushColor(Color color) {
        this.brushColor = color;
    }

    public void setBrushSize(double size) {
        this.brushSize = size;
    }

    public boolean isEraser() {
        return drawMode == DrawMode.ERASER;
    }

    public void saveAsPng(File file) {
        ScreenCaptureUtil.saveScreenAsPng(file, this::finalizeText);
    }
}
