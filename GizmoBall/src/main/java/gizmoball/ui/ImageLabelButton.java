package gizmoball.ui;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import lombok.Data;

@Data
public class ImageLabelButton {

    private Image image;

    private String labelText;

    //----------javafx控件相关----------

    private double imageWidth;

    private double imageHeight;

    private VBox vBox;

    private ImageView imageView;

    private Label label;

    public ImageLabelButton(String resource, String labelText) {
        this.image = new Image(getClass().getClassLoader().getResourceAsStream(resource));
        this.labelText = labelText;
        imageWidth = 60;
        imageHeight = 60;
    }

    public void setImageSize(double width, double height) {
        imageWidth = width;
        imageHeight = height;
    }

    public VBox createHBox() {
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
