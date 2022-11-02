package gizmoball.ui.component;


import gizmoball.engine.geometry.Vector2;
import gizmoball.engine.geometry.shape.Circle;
import gizmoball.engine.geometry.shape.QuarterCircle;
import gizmoball.engine.geometry.shape.Rectangle;
import gizmoball.engine.geometry.shape.Triangle;
import gizmoball.engine.physics.PhysicsBody;
import gizmoball.ui.ImagePhysicsBody;
import javafx.scene.Cursor;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.VBox;
import lombok.Getter;
import lombok.Setter;

import java.util.function.Function;

@Getter
@Setter
public class DraggableGizmoComponent extends ImageLabelComponent {


    protected static final Function<Vector2, ImagePhysicsBody> circleBodyCreator = (preferredSize) -> {
        Circle circle = new Circle(preferredSize.x / 2.0);
        return new ImagePhysicsBody(circle);
    };

    protected static final Function<Vector2, ImagePhysicsBody> rectangleBodyCreator = (preferredSize) -> {
        Rectangle rectangle = new Rectangle(preferredSize.x / 2.0, preferredSize.y / 2.0);
        return new ImagePhysicsBody(rectangle);
    };

    protected static final Function<Vector2, ImagePhysicsBody> flipperBodyCreator = (preferredSize) -> {
        Rectangle rectangle = new Rectangle(preferredSize.x / 2.0, preferredSize.y / 4.0 / 2.0);
        return new ImagePhysicsBody(rectangle);
    };

    protected static final Function<Vector2, ImagePhysicsBody> triangleBodyCreator = (preferredSize) -> {
        Vector2[] vertices = new Vector2[]{
                new Vector2(-preferredSize.y / 2.0, -preferredSize.y / 2.0),
                new Vector2(preferredSize.x / 2.0, -preferredSize.y / 2.0),
                new Vector2(-preferredSize.y / 2.0, preferredSize.y / 2.0)
        };
        Triangle triangle = new Triangle(vertices);
        return new ImagePhysicsBody(triangle);
    };

    protected static final Function<Vector2, ImagePhysicsBody> curvedPipeBodyCreator = (preferredSize) -> {
        QuarterCircle quarterCircle = new QuarterCircle(preferredSize.x);
        return new ImagePhysicsBody(quarterCircle);
    };

    private GizmoType gizmoType;

    private Image flippedImage;

    private SVGNode svgNode;

    public DraggableGizmoComponent(String resource, String labelText, GizmoType gizmoType) {
        super(resource, labelText);
        this.gizmoType = gizmoType;
        this.flippedImage = upsideDown(getImage());

        int li = resource.lastIndexOf('.');
        String svgResource = (li == -1 ? resource : resource.substring(0, li)) + ".svg";
        this.svgNode = SVGNode.fromResource(getClass().getClassLoader().getResourceAsStream(svgResource));
    }

    /**
     * 上下翻转图片
     * @param image /
     * @return /
     */
    public static Image upsideDown(Image image) {
        int w = (int) image.getWidth();
        int h = (int) image.getHeight();
        PixelReader pixelReader = image.getPixelReader();
        WritableImage writableImage = new WritableImage(w, h);
        PixelWriter pixelWriter = writableImage.getPixelWriter();

        for (int i = 0; i < h; i++) {
            for (int j = 0; j < w; j++) {
                pixelWriter.setArgb(j, h - 1 - i, pixelReader.getArgb(j, i));
            }
        }

        return writableImage;
    }

    @Override
    public VBox createVBox() {
        VBox vBox = super.createVBox();
        this.getImageView().setCursor(Cursor.HAND);
        return vBox;
    }

    /**
     * Create a physics body for this gizmo.
     * @param preferredSize the preferred size of the gizmo.
     * @param center the center of the gizmo.
     * @return the physics body.
     */
    public PhysicsBody createPhysicsBody(Vector2 preferredSize, Vector2 center) {
        ImagePhysicsBody physicsBody = (ImagePhysicsBody) gizmoType.getPhysicsBodySupplier().apply(preferredSize);
        physicsBody.getShape().translate(center);
        physicsBody.setImage(this.flippedImage);
        physicsBody.setSvgNode(this.svgNode);
        return physicsBody;
    }
}
