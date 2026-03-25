package com.easypencil;

import java.util.ArrayDeque;
import java.util.Deque;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;

public class DrawingCanvas extends Pane {

    private final Canvas canvas;
    private final GraphicsContext gc;
    private final Deque<WritableImage> undoStack = new ArrayDeque<>();

    private Color brushColor = Color.RED;
    private double brushSize = 4.0;
    private boolean eraser = false;

    public DrawingCanvas() {
        double w = javafx.stage.Screen.getPrimary().getBounds().getWidth();
        double h = javafx.stage.Screen.getPrimary().getBounds().getHeight();

        canvas = new Canvas(w, h);
        gc = canvas.getGraphicsContext2D();
        gc.setLineCap(StrokeLineCap.ROUND);
        gc.setLineJoin(StrokeLineJoin.ROUND);

        setPickOnBounds(false);
        canvas.setPickOnBounds(true);
        canvas.setMouseTransparent(false);

        getChildren().add(canvas);
        setupMouseEvents();
    }

    private void setupMouseEvents() {
        canvas.setOnMousePressed(e -> {
            saveSnapshot(); // บันทึกภาพก่อนเริ่มวาด/ลบ สำหรับ Undo

            if (eraser) {
                // เปิดโหมดลบ (ทำให้เส้นที่วาดกลายเป็นโปร่งใส)
                gc.setGlobalBlendMode(javafx.scene.effect.BlendMode.ADD);
                gc.setLineWidth(brushSize * 4); // ปรับขนาดหัวยางลบให้ใหญ่กว่าปากกาปกติ
            } else {
                // เปิดโหมดวาดปกติ
                gc.setGlobalBlendMode(javafx.scene.effect.BlendMode.SRC_OVER);
                gc.setStroke(brushColor);
                gc.setLineWidth(brushSize);
            }

            gc.beginPath();
            gc.moveTo(e.getX(), e.getY());
            gc.stroke(); // ทำให้คลิกจุดเดียวแล้วเกิดรอย (ไม่ต้องลากก็ติด)
        });

        canvas.setOnMouseDragged(e -> {
            // ลากเส้นไปตามเมาส์ (ใช้ได้ทั้งวาดและลบ เพราะเราตั้ง BlendMode ไว้แล้วตอน Click)
            gc.lineTo(e.getX(), e.getY());
            gc.stroke();
        });

        canvas.setOnMouseReleased(e -> {
            gc.closePath();
            // คืนค่าโหมดวาดกลับเป็นปกติ เผื่อไว้
            gc.setGlobalBlendMode(javafx.scene.effect.BlendMode.SRC_OVER);
        });
    }

    private void saveSnapshot() {
        WritableImage snapshot = canvas.snapshot(null, null);
        undoStack.push(snapshot);
        if (undoStack.size() > 30) {
            undoStack.pollLast();
        }
    }

    public void undo() {
        if (!undoStack.isEmpty()) {
            gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
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

    public void setEraser(boolean eraser) {
        this.eraser = eraser;
    }

    public boolean isEraser() {
        return eraser;
    }
}
