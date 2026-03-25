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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
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
        setPadding(new Insets(15)); 

        // 🌟 1. จุดจับลาก (Epic Pen Style) 🌟
        dragHandle = new Label("⣿");
        dragHandle.setTextFill(Color.web("#555555"));
        dragHandle.setStyle("-fx-font-size: 18px; -fx-cursor: move;");
        
        rotateBtn = new Button("🔄");
        styleIconBtn(rotateBtn);
        rotateBtn.setOnAction(e -> {
            isHorizontal = !isHorizontal; 
            buildLayout(); 
        });

        toggleMode = new ToggleButton("✏ Draw");
        styleToggleBtn(toggleMode);
        toggleMode.setSelected(true);
        toggleMode.setStyle(activeStyle());
        toggleMode.setOnAction(e -> {
            if (toggleMode.isSelected()) {
                toggleMode.setText("✏ Draw");
                toggleMode.setStyle(activeStyle());
                canvas.setMouseTransparent(false);
                Main.setDrawMode(true);
            } else {
                toggleMode.setText("👁 View");
                toggleMode.setStyle(normalStyle());
                canvas.setMouseTransparent(true);
                Main.setDrawMode(false);
            }
        });

        penBtn = new ToggleButton("Pen");
        highlightBtn = new ToggleButton("Highlight");
        textBtn = new ToggleButton("Text");
        eraserBtn = new ToggleButton("Eraser");

        // โหลดรูปไอคอนโดยส่งแค่ชื่อไฟล์ไป
        ImageView penIcon = createIcon("pencil.png");
        if (penIcon != null) penBtn.setGraphic(penIcon);

        ImageView highlightIcon = createIcon("highlighter.png");
        if (highlightIcon != null) highlightBtn.setGraphic(highlightIcon);

        ImageView textIcon = createIcon("text_icon.png");
        if (textIcon != null) textBtn.setGraphic(textIcon);

        ImageView eraserIcon = createIcon("eraser.png");
        if (eraserIcon != null) eraserBtn.setGraphic(eraserIcon);

        styleToggleBtn(penBtn);
        styleToggleBtn(highlightBtn);
        styleToggleBtn(textBtn);
        styleToggleBtn(eraserBtn);

        setActiveTool(penBtn);
        canvas.setPenMode();

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

        // 🌟 2. ตัวหนังสือและ Color Picker สไตล์มินิมอล 🌟
        colorLabel = new Label("Color");
        colorLabel.setTextFill(Color.web("#FFFFFF"));
        colorLabel.setStyle("-fx-font-size: 11px;");

        colorPicker = new ColorPicker(Color.web("#E91E63")); 
        colorPicker.setStyle("-fx-color-label-visible: false; -fx-background-color: #2b2b2b; -fx-background-radius: 20; -fx-cursor: hand;");
        colorPicker.setOnAction(e -> {
            canvas.setBrushColor(colorPicker.getValue());
            if (canvas.isEraser()) {
                penBtn.fire(); 
            }
        });
        canvas.setBrushColor(colorPicker.getValue()); 

        sizeLabel = new Label("Size: 4");
        sizeLabel.setTextFill(Color.web("#FFFFFF"));
        sizeLabel.setStyle("-fx-font-size: 11px;");
        sizeLabel.setPrefWidth(45); 

        sizeSlider = new Slider(1, 30, 4);
        sizeSlider.setPrefWidth(80);
        sizeSlider.setStyle("-fx-cursor: hand;");
        
        sizeSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            canvas.setBrushSize(newVal.doubleValue());
            sizeLabel.setText("Size: " + String.format("%.0f", newVal.doubleValue()));
        });

        undoBtn = new Button("↩ Undo");
        styleActionBtn(undoBtn);
        undoBtn.setOnAction(e -> canvas.undo());

        clearBtn = new Button("🗑 Clear");
        styleActionBtn(clearBtn);
        clearBtn.setOnAction(e -> canvas.clearCanvas());

        closeBtn = new Button("✕");
        closeBtn.setStyle(
                "-fx-background-color: transparent; -fx-text-fill: #ff4d4d;"
                + "-fx-background-radius: 20; -fx-font-size: 14px; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 6 12;"
        );
        closeBtn.setOnMouseEntered(e -> closeBtn.setStyle("-fx-background-color: #ff4d4d; -fx-text-fill: white; -fx-background-radius: 20; -fx-font-size: 14px; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 6 12;"));
        closeBtn.setOnMouseExited(e -> closeBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #ff4d4d; -fx-background-radius: 20; -fx-font-size: 14px; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 6 12;"));
        closeBtn.setOnAction(e -> stage.close());

        buildLayout();
    }

    private void setActiveTool(ToggleButton activeBtn) {
        ToggleButton[] tools = {penBtn, highlightBtn, textBtn, eraserBtn};
        for (ToggleButton btn : tools) {
            if (btn == activeBtn) {
                btn.setSelected(true);
                btn.setStyle(activeStyle());
            } else {
                btn.setSelected(false);
                btn.setStyle(normalStyle());
            }
        }
    }

    // 🌟 3. กล่อง Toolbar โค้งมนแบบเม็ดยา (Pill Shape) พื้นหลังสีดำเข้ม 🌟
    private void buildLayout() {
        Pane container;
        Orientation sepOrientation;

        if (isHorizontal) {
            HBox box = new HBox(6); 
            box.setAlignment(Pos.CENTER_LEFT);
            container = box;
            sepOrientation = Orientation.VERTICAL;
            dragHandle.setText("⣿");
            dragHandle.setPadding(new Insets(0, 5, 0, 0));
        } else {
            VBox box = new VBox(6);
            box.setAlignment(Pos.TOP_CENTER);
            container = box;
            sepOrientation = Orientation.HORIZONTAL;
            dragHandle.setText("⠛");
            dragHandle.setPadding(new Insets(0, 0, 5, 0));
        }

        container.setPadding(new Insets(8, 14, 8, 14));
        
        // พื้นหลังดำขลับแบบ Epic Pen พร้อมขอบโค้งมนระดับ 30
        container.setStyle(
                "-fx-background-color: #1a1a1a;"
                + "-fx-background-radius: 30;"
                + "-fx-border-color: #333333;"
                + "-fx-border-radius: 30;"
                + "-fx-border-width: 1;"
                + "-fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.6), 12, 0, 0, 6);"
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
                getStyledSeparator(sepOrientation),
                toggleMode, 
                getStyledSeparator(sepOrientation),
                penBtn, highlightBtn, textBtn, eraserBtn, 
                getStyledSeparator(sepOrientation),
                colorLabel, colorPicker, sizeLabel, sizeSlider, 
                getStyledSeparator(sepOrientation),
                undoBtn, clearBtn, 
                getStyledSeparator(sepOrientation),
                closeBtn
        );

        this.getChildren().clear();
        this.getChildren().add(container);
    }

    // --- สไตล์ปุ่มแบบ Epic Pen (Pink/Magenta Accent) ---

    private void styleToggleBtn(ToggleButton btn) {
        btn.setStyle(normalStyle());
        btn.setOnMouseEntered(e -> {
            if (!btn.isSelected()) btn.setStyle(hoverStyle());
        });
        btn.setOnMouseExited(e -> {
            if (!btn.isSelected()) btn.setStyle(normalStyle());
        });
    }

    private void styleActionBtn(Button btn) {
        btn.setStyle(normalStyle());
        btn.setOnMouseEntered(e -> btn.setStyle(hoverStyle()));
        btn.setOnMouseExited(e -> btn.setStyle(normalStyle()));
    }

    private void styleIconBtn(Button btn) {
        btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #999999; -fx-font-size: 14px; -fx-cursor: hand; -fx-background-radius: 20;");
        btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: #333333; -fx-text-fill: white; -fx-font-size: 14px; -fx-cursor: hand; -fx-background-radius: 20;"));
        btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #999999; -fx-font-size: 14px; -fx-cursor: hand; -fx-background-radius: 20;"));
    }

    private Separator getStyledSeparator(Orientation orientation) {
        Separator sep = new Separator(orientation);
        sep.setStyle("-fx-opacity: 0.15; -fx-background-color: #ffffff;"); 
        return sep;
    }

    // สีปุ่มปกติ (โปร่งใส กลืนไปกับพื้นหลัง)
    private String normalStyle() {
        return "-fx-background-color: transparent; -fx-text-fill: #cccccc;"
                + "-fx-background-radius: 20; -fx-font-size: 12px; -fx-cursor: hand; -fx-padding: 6 12;";
    }

    // สีตอนเอาเมาส์ชี้ (เทาสว่างขึ้นเล็กน้อย)
    private String hoverStyle() {
        return "-fx-background-color: #333333; -fx-text-fill: #ffffff;"
                + "-fx-background-radius: 20; -fx-font-size: 12px; -fx-cursor: hand; -fx-padding: 6 12;";
    }

    // 🌟 สีปุ่มตอนถูกกดใช้งาน (สีชมพูอมแดงแบบ Epic Pen) 🌟
    private String activeStyle() {
        return "-fx-background-color: #E91E63; -fx-text-fill: #ffffff;"
                + "-fx-background-radius: 20; -fx-font-size: 12px; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 6 12;"
                + "-fx-effect: dropshadow(gaussian, rgba(233, 30, 99, 0.4), 8, 0, 0, 0);";
    }

    // 🌟 ฟังก์ชันตัวช่วยสำหรับโหลดรูปภาพ 🌟
    private ImageView createIcon(String fileName) {
        try {
            // บังคับอ่านจากโฟลเดอร์ตรงๆ เหมือนกัน
            String fullPath = "file:app/src/main/resources/asset/" + fileName;
            Image img = new Image(fullPath);
            ImageView view = new ImageView(img);
            view.setFitWidth(16);  
            view.setFitHeight(16); 
            view.setPreserveRatio(true); 
            return view;
        } catch (Exception e) {
            System.out.println("Error Loading Icon: " + fileName);
            return null; 
        }
    }
}