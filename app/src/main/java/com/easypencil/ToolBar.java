package com.easypencil;

import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.Slider;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class ToolBar extends VBox {

    // ตัวแปรสำหรับ Drag & Drop
    private double xOffset = 0;
    private double yOffset = 0;
    
    // สถานะแนวนอน / แนวตั้ง
    private boolean isHorizontal = true;

    // ชิ้นส่วน UI
    private Label dragHandle;
    private Button rotateBtn;
    private ToggleButton toggleMode;
    
    // 🌟 เปลี่ยนจาก MenuButton มาเป็น ToggleButton 3 ตัว 🌟
    private ToggleButton penBtn;
    private ToggleButton textBtn;
    private ToggleButton eraserBtn;
    
    private Label colorLabel;
    private ColorPicker colorPicker;
    private Label sizeLabel;
    private Slider sizeSlider;
    private Button undoBtn;
    private Button clearBtn;
    private Button closeBtn;

    public ToolBar(DrawingCanvas canvas, Stage stage) {
        setAlignment(Pos.TOP_LEFT);
        setPickOnBounds(false);
        setPadding(new Insets(10));

        dragHandle = new Label("⣿");
        dragHandle.setTextFill(Color.web("#888888"));
        dragHandle.setStyle("-fx-font-size: 18px; -fx-cursor: move;");
        
        rotateBtn = new Button("🔄");
        rotateBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-font-size: 14px; -fx-cursor: hand;");
        rotateBtn.setOnAction(e -> {
            isHorizontal = !isHorizontal; 
            buildLayout(); 
        });

        toggleMode = new ToggleButton("✏ Draw");
        toggleMode.setSelected(true);
        toggleMode.setStyle(selectedStyle());
        toggleMode.setOnAction(e -> {
            if (toggleMode.isSelected()) {
                toggleMode.setText("✏ Draw");
                toggleMode.setStyle(selectedStyle());
                canvas.setMouseTransparent(false);
                Main.setDrawMode(true);
            } else {
                toggleMode.setText("👁 View");
                toggleMode.setStyle(normalStyle());
                canvas.setMouseTransparent(true);
                Main.setDrawMode(false);
            }
        });

        // 🌟 สร้าง 3 ปุ่มเครื่องมือ 🌟
        penBtn = new ToggleButton("✏ Pen");
        textBtn = new ToggleButton("🔤 Text");
        eraserBtn = new ToggleButton("⬜ Eraser");

        // ตั้งค่าให้ปุ่ม Pen ถูกเลือกเป็นค่าเริ่มต้น
        penBtn.setSelected(true);
        penBtn.setStyle(selectedStyle());
        textBtn.setStyle(normalStyle());
        eraserBtn.setStyle(normalStyle());

        // กำหนดการทำงาน: เมื่อกดปุ่มใดปุ่มหนึ่ง ให้ปุ่มอื่นยกเลิกการเลือก
        penBtn.setOnAction(e -> {
            penBtn.setSelected(true); // บังคับให้เป็นสถานะกดลงเสมอ
            penBtn.setStyle(selectedStyle());
            
            textBtn.setSelected(false);
            textBtn.setStyle(normalStyle());
            
            eraserBtn.setSelected(false);
            eraserBtn.setStyle(normalStyle());
            
            canvas.setTextMode(false);
            canvas.setEraser(false);
        });

        textBtn.setOnAction(e -> {
            textBtn.setSelected(true);
            textBtn.setStyle(selectedStyle());
            
            penBtn.setSelected(false);
            penBtn.setStyle(normalStyle());
            
            eraserBtn.setSelected(false);
            eraserBtn.setStyle(normalStyle());
            
            canvas.setTextMode(true);
            canvas.setEraser(false);
        });

        eraserBtn.setOnAction(e -> {
            eraserBtn.setSelected(true);
            eraserBtn.setStyle(selectedStyle());
            
            penBtn.setSelected(false);
            penBtn.setStyle(normalStyle());
            
            textBtn.setSelected(false);
            textBtn.setStyle(normalStyle());
            
            canvas.setTextMode(false);
            canvas.setEraser(true);
        });

        colorLabel = new Label("Color:");
        colorLabel.setTextFill(Color.WHITE);

        colorPicker = new ColorPicker(Color.RED);
        colorPicker.setStyle("-fx-color-label-visible: false;");
        colorPicker.setOnAction(e -> {
            canvas.setBrushColor(colorPicker.getValue());
            // ถ้าไปเปลี่ยนสีตอนใช้ยางลบอยู่ โปรแกรมจะจำลองการกดปุ่มปากกาให้อัตโนมัติ
            if (canvas.isEraser()) {
                penBtn.fire(); 
            }
        });

        sizeLabel = new Label("Size:");
        sizeLabel.setTextFill(Color.WHITE);

        sizeSlider = new Slider(1, 30, 4);
        sizeSlider.setPrefWidth(80);
        sizeSlider.valueProperty().addListener((obs, oldVal, newVal)
                -> canvas.setBrushSize(newVal.doubleValue())
        );

        undoBtn = new Button("↩ Undo");
        undoBtn.setStyle(normalStyle());
        undoBtn.setOnAction(e -> canvas.undo());

        clearBtn = new Button("🗑 Clear");
        clearBtn.setStyle(normalStyle());
        clearBtn.setOnAction(e -> canvas.clearCanvas());

        closeBtn = new Button("✕");
        closeBtn.setStyle(
                "-fx-background-color: #c0392b; -fx-text-fill: white;"
                + "-fx-background-radius: 6; -fx-font-size: 13px; -fx-cursor: hand;"
        );
        closeBtn.setOnAction(e -> stage.close());

        buildLayout();
    }

    private void buildLayout() {
        Pane container;
        Orientation sepOrientation;

        if (isHorizontal) {
            HBox box = new HBox(8);
            box.setAlignment(Pos.CENTER_LEFT);
            container = box;
            sepOrientation = Orientation.VERTICAL;
            dragHandle.setText("⣿");
            dragHandle.setPadding(new Insets(0, 5, 0, 0));
        } else {
            VBox box = new VBox(8);
            box.setAlignment(Pos.TOP_CENTER);
            container = box;
            sepOrientation = Orientation.HORIZONTAL;
            dragHandle.setText("⠛");
            dragHandle.setPadding(new Insets(0, 0, 5, 0));
        }

        container.setPadding(new Insets(8, 12, 8, 12));
        container.setStyle(
                "-fx-background-color: rgba(30,30,30,0.85);"
                + "-fx-background-radius: 12;"
        );

        container.setOnMousePressed(e -> {
            xOffset = e.getSceneX() - this.getLayoutX();
            yOffset = e.getSceneY() - this.getLayoutY();
        });
        container.setOnMouseDragged(e -> {
            this.setLayoutX(e.getSceneX() - xOffset);
            this.setLayoutY(e.getSceneY() - yOffset);
        });

        container.getChildren().addAll(
                dragHandle, 
                rotateBtn, 
                new Separator(sepOrientation),
                toggleMode, 
                new Separator(sepOrientation),
                // 🌟 นำปุ่มทั้ง 3 มาเรียงต่อกัน 🌟
                penBtn, textBtn, eraserBtn, 
                new Separator(sepOrientation),
                colorLabel, colorPicker, sizeLabel, sizeSlider, 
                new Separator(sepOrientation),
                undoBtn, clearBtn, 
                new Separator(sepOrientation),
                closeBtn
        );

        this.getChildren().clear();
        this.getChildren().add(container);
    }

    private String selectedStyle() {
        return "-fx-background-color: #3498db; -fx-text-fill: white;"
                + "-fx-background-radius: 6; -fx-font-size: 13px; -fx-cursor: hand;";
    }

    private String normalStyle() {
        return "-fx-background-color: #555; -fx-text-fill: white;"
                + "-fx-background-radius: 6; -fx-font-size: 13px; -fx-cursor: hand;";
    }
}