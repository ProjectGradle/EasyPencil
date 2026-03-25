package com.easypencil;

import java.util.ArrayDeque;
import java.util.Deque;

import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
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
    private boolean textMode = false;
    private boolean highlightMode = false; 

    private VBox activeTextContainer = null;
    private javafx.scene.control.TextArea activeTextArea = null;
    private double dragOffsetX = 0;
    private double dragOffsetY = 0;

    public DrawingCanvas() {
        double w = javafx.stage.Screen.getPrimary().getBounds().getWidth();
        double h = javafx.stage.Screen.getPrimary().getBounds().getHeight();

        canvas = new Canvas(w, h);
        gc = canvas.getGraphicsContext2D();
        gc.setLineJoin(StrokeLineJoin.ROUND);

        setPickOnBounds(false);
        canvas.setPickOnBounds(true);
        canvas.setMouseTransparent(false);

        getChildren().add(canvas);
        setupMouseEvents();
    }

    private void setupMouseEvents() {
        canvas.setOnMousePressed(e -> {
            if (textMode) {
                if (activeTextContainer != null) {
                    finalizeText();
                    return; 
                }
                createTextArea(e.getX(), e.getY());
                return;
            }

            saveSnapshot();
            gc.setStroke(brushColor);
            
            if (highlightMode) {
                gc.setGlobalAlpha(0.4); 
                gc.setLineWidth(brushSize * 3); 
                gc.setLineCap(StrokeLineCap.ROUND); 
            } else {
                gc.setGlobalAlpha(1.0); 
                gc.setLineWidth(brushSize);
                gc.setLineCap(StrokeLineCap.ROUND); 
            }
            
            gc.beginPath();
            gc.moveTo(e.getX(), e.getY());
            gc.stroke(); // วาดจุดแรกตอนคลิก
        });

        canvas.setOnMouseDragged(e -> {
            if (textMode) return; 

            if (eraser) {
                // บังคับให้ Alpha เป็น 1.0 เสมอก่อนลบ ป้องกันบัคลบไม่สะอาด
                gc.setGlobalAlpha(1.0);
                gc.clearRect(
                        e.getX() - brushSize * 2,
                        e.getY() - brushSize * 2,
                        brushSize * 4, brushSize * 4);
            } else {
                gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
                
                // 🌟 หัวใจสำคัญแก้บัคภาพหาย: คืนค่า Alpha เป็น 1.0 ก่อนวาดภาพ Snapshot เก่ากลับมา 🌟
                gc.setGlobalAlpha(1.0);
                gc.drawImage(undoStack.peek(), 0, 0);

                // 🌟 ตั้งค่า Alpha กลับไปเป็น 40% เฉพาะตอนวาดเส้นไฮไลท์ใหม่ทับลงไป 🌟
                if (highlightMode) {
                    gc.setGlobalAlpha(0.4);
                }

                gc.lineTo(e.getX(), e.getY());
                gc.stroke();
            }
        });

        canvas.setOnMouseReleased(e -> {
            if (!textMode) {
                gc.closePath();
            }
        });
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

    private void createTextArea(double x, double y) {
        activeTextContainer = new VBox();
        activeTextContainer.setLayoutX(x);
        activeTextContainer.setLayoutY(y - 24); 

        javafx.scene.control.Label moveHandle = new javafx.scene.control.Label("✥ ลากตรงนี้เพื่อย้ายตำแหน่ง");
        moveHandle.setStyle("-fx-background-color: rgba(41, 128, 185, 0.9); -fx-text-fill: white; -fx-padding: 3 8; -fx-cursor: move; -fx-font-size: 12px; -fx-background-radius: 6 6 0 0;");
        moveHandle.setMinHeight(24);
        moveHandle.setMaxHeight(24);
        moveHandle.setMaxWidth(Double.MAX_VALUE); 

        moveHandle.setOnMousePressed(e -> {
            dragOffsetX = e.getSceneX() - activeTextContainer.getLayoutX();
            dragOffsetY = e.getSceneY() - activeTextContainer.getLayoutY();
            e.consume(); 
        });
        moveHandle.setOnMouseDragged(e -> {
            activeTextContainer.setLayoutX(e.getSceneX() - dragOffsetX);
            activeTextContainer.setLayoutY(e.getSceneY() - dragOffsetY);
            e.consume();
        });

        activeTextArea = new javafx.scene.control.TextArea();
        activeTextArea.setWrapText(false); 

        String hexColor = String.format("#%02X%02X%02X",
                (int) (brushColor.getRed() * 255),
                (int) (brushColor.getGreen() * 255),
                (int) (brushColor.getBlue() * 255));

        double fontSize = Math.max(16, brushSize * 4);

        activeTextArea.setStyle(
                "-fx-control-inner-background: rgba(255,255,255,0.8); " +
                "-fx-background-color: transparent; " +
                "-fx-text-fill: " + hexColor + "; " +
                "-fx-font-size: " + fontSize + "px; " +
                "-fx-border-color: #888; -fx-border-style: dashed; -fx-border-width: 0 1 1 1;"
        );

        javafx.scene.text.Text textMeasurer = new javafx.scene.text.Text();
        textMeasurer.setFont(javafx.scene.text.Font.font("System", fontSize));

        double initialWidth = fontSize * 3;
        double initialHeight = fontSize * 2.5;
        activeTextArea.setPrefWidth(initialWidth);
        activeTextArea.setPrefHeight(initialHeight);

        activeTextArea.textProperty().addListener((obs, oldText, newText) -> {
            textMeasurer.setText(newText.isEmpty() ? "A" : newText);
            double newWidth = textMeasurer.getLayoutBounds().getWidth() + 35;
            double newHeight = textMeasurer.getLayoutBounds().getHeight() + 25;
            activeTextArea.setPrefWidth(Math.max(initialWidth, newWidth));
            activeTextArea.setPrefHeight(Math.max(initialHeight, newHeight));
        });

        activeTextArea.setOnKeyPressed(e -> {
            if (e.isControlDown() && e.getCode() == javafx.scene.input.KeyCode.ENTER) {
                finalizeText();
            }
        });

        activeTextContainer.getChildren().addAll(moveHandle, activeTextArea);
        this.getChildren().add(activeTextContainer);
        activeTextArea.requestFocus();
    }

    private void finalizeText() {
        if (activeTextArea != null && activeTextContainer != null && !activeTextArea.getText().trim().isEmpty()) {
            saveSnapshot(); 
            gc.setGlobalAlpha(1.0); 
            gc.setFill(brushColor);
            double fontSize = Math.max(16, brushSize * 4);
            gc.setFont(javafx.scene.text.Font.font("System", javafx.scene.text.FontWeight.BOLD, fontSize));
            
            double stampX = activeTextContainer.getLayoutX() + 5;
            double stampY = activeTextContainer.getLayoutY() + 24 + fontSize; 
            
            gc.fillText(activeTextArea.getText(), stampX, stampY);
        }
        
        if (activeTextContainer != null) {
            this.getChildren().remove(activeTextContainer);
            activeTextContainer = null;
            activeTextArea = null;
        }
    }

    public void setPenMode() {
        this.textMode = false;
        this.eraser = false;
        this.highlightMode = false;
        if (activeTextContainer != null) finalizeText();
    }

    public void setHighlightMode() {
        this.textMode = false;
        this.eraser = false;
        this.highlightMode = true;
        if (activeTextContainer != null) finalizeText();
    }

    public void setTextMode() {
        this.textMode = true;
        this.eraser = false;
        this.highlightMode = false;
    }

    public void setEraserMode() {
        this.textMode = false;
        this.eraser = true;
        this.highlightMode = false;
        if (activeTextContainer != null) finalizeText();
    }

    public void undo() {
        if (!undoStack.isEmpty()) {
            gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
            gc.setGlobalAlpha(1.0); // 🌟 กันภาพจางเวลาย้อนกลับ (Undo) หลังจากใช้ไฮไลท์
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
        return eraser;
    }
}