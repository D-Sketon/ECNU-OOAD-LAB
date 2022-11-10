package gizmoball.ui.component;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import lombok.Data;

@Data
public class ImageLabelComponent {

    private Image image;

    private String labelText;

    //----------javafx控件相关----------

    private double imageWidth;

    private double imageHeight;

    private VBox vBox;

    private ImageView imageView;

    /**
     * 由于SVG图片点击透明处无效，包装ImageView，添加事件监听器。
     */
    private Pane imageWrapper;

    private Label label;

    public ImageLabelComponent(String resource, String labelText) {
        this(resource, labelText, 60, 60);
    }

    public ImageLabelComponent(String resource, String labelText, int imageWidth, int imageHeight) {
        this.image = new Image(getClass().getClassLoader().getResourceAsStream(resource),
                imageWidth, imageHeight, true, true);
        this.labelText = labelText;
        this.imageWidth = imageWidth;
        this.imageHeight = imageHeight;
    }

    public ImageLabelComponent(Image image, String labelText) {
        this.image = image;
        this.labelText = labelText;
        this.imageWidth = image.getWidth();
        this.imageHeight = image.getHeight();
    }


    public VBox createVBox() {
        vBox = new VBox();
        vBox.setAlignment(Pos.CENTER);

        imageWrapper = new Pane();
        imageWrapper.setMaxSize(imageWidth, imageHeight);
        imageWrapper.setCursor(javafx.scene.Cursor.HAND);

        imageView = new ImageView(image);
        imageView.setFitHeight(imageHeight);
        imageView.setFitWidth(imageWidth);
        imageWrapper.getChildren().add(imageView);
        vBox.getChildren().add(imageWrapper);

        label = new Label(this.labelText);
        label.setPrefWidth(100);
        label.setAlignment(Pos.CENTER);
        label.setTextAlignment(TextAlignment.CENTER);
        vBox.getChildren().add(label);

        return vBox;
    }
}
