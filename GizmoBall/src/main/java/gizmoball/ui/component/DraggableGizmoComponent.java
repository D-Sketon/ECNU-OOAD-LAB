package gizmoball.ui.component;


import gizmoball.engine.geometry.Vector2;
import gizmoball.engine.geometry.shape.*;
import gizmoball.engine.physics.PhysicsBody;
import gizmoball.ui.visualize.ImagePhysicsBody;
import javafx.scene.Cursor;
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

    protected static final Function<Vector2, ImagePhysicsBody> pipeBodyCreator = (preferredSize) -> {
        Pipe pipe = new Pipe(preferredSize.x / 2.0, preferredSize.y / 2.0);
        return new ImagePhysicsBody(pipe);
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

    public DraggableGizmoComponent(String resource, String labelText, GizmoType gizmoType) {
        super(resource, labelText);
        this.gizmoType = gizmoType;
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
        physicsBody.setGizmoType(gizmoType);
        return physicsBody;
    }
}
