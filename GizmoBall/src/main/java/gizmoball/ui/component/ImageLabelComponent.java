package gizmoball.ui.component;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
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

    private Label label;

    public ImageLabelComponent(String resource, String labelText) {
        this(resource, labelText, 60, 60);
    }

    public ImageLabelComponent(String resource, String labelText, int imageWidth, int imageHeight) {
        this.image = new Image(getClass().getClassLoader().getResourceAsStream(resource), imageWidth, imageHeight, true, true);
        this.labelText = labelText;
        this.imageWidth = imageWidth;
        this.imageHeight = imageHeight;
    }

    public VBox createVBox() {
        vBox = new VBox();
        vBox.setAlignment(Pos.CENTER);
        imageView = new ImageView(image);
        imageView.setFitHeight(imageHeight);
        imageView.setFitWidth(imageWidth);
        vBox.getChildren().add(imageView);
        label = new Label(this.labelText);
        label.setPrefWidth(100);
        label.setAlignment(Pos.CENTER);
        label.setTextAlignment(TextAlignment.CENTER);
        vBox.getChildren().add(label);
        return vBox;
    }
}
