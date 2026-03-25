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

    private double xOffset = 0;
    private double yOffset = 0;
    private boolean isHorizontal = true;

    private Label dragHandle;
    private Button rotateBtn;
    private ToggleButton toggleMode;
    
    // 🌟 เครื่องมือทั้ง 4 แบบ 🌟
    private ToggleButton penBtn;
    private ToggleButton highlightBtn;
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

        // สร้างปุ่มเครื่องมือ
        penBtn = new ToggleButton("✏ Pen");
        highlightBtn = new ToggleButton("🖍 Highlight");
        textBtn = new ToggleButton("🔤 Text");
        eraserBtn = new ToggleButton("⬜ Eraser");

        // กำหนดปุ่มเริ่มต้น
        setActiveTool(penBtn);
        canvas.setPenMode();

        // 🌟 ตั้งค่าการกดปุ่มให้สลับโหมด 🌟
        penBtn.setOnAction(e -> {
            setActiveTool(penBtn);
            canvas.setPenMode();
        });

        highlightBtn.setOnAction(e -> {
            setActiveTool(highlightBtn);
            canvas.setHighlightMode();
        });

        textBtn.setOnAction(e -> {
            setActiveTool(textBtn);
            canvas.setTextMode();
        });

        eraserBtn.setOnAction(e -> {
            setActiveTool(eraserBtn);
            canvas.setEraserMode();
        });

        colorLabel = new Label("Color:");
        colorLabel.setTextFill(Color.WHITE);

        // สีเริ่มต้นสำหรับปากกา และ ไฮไลท์
        colorPicker = new ColorPicker(Color.YELLOW); 
        colorPicker.setStyle("-fx-color-label-visible: false;");
        colorPicker.setOnAction(e -> {
            canvas.setBrushColor(colorPicker.getValue());
            if (canvas.isEraser()) {
                penBtn.fire(); // เด้งกลับมาหน้าปากกาถ้าเปลี่ยนสีตอนใช้ยางลบ
            }
        });
        canvas.setBrushColor(colorPicker.getValue()); // ส่งค่าเริ่มต้นไปให้ Canvas

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

    // ฟังก์ชันช่วยจัดการสีปุ่ม (ลดโค้ดซ้ำซ้อน)
    private void setActiveTool(ToggleButton activeBtn) {
        ToggleButton[] tools = {penBtn, highlightBtn, textBtn, eraserBtn};
        for (ToggleButton btn : tools) {
            if (btn == activeBtn) {
                btn.setSelected(true);
                btn.setStyle(selectedStyle());
            } else {
                btn.setSelected(false);
                btn.setStyle(normalStyle());
            }
        }
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
                penBtn, highlightBtn, textBtn, eraserBtn, // <--- นำ 4 ปุ่มมาเรียงกัน
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