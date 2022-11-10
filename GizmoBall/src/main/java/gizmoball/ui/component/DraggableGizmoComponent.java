package gizmoball.ui.component;


import gizmoball.engine.geometry.Vector2;
import gizmoball.engine.geometry.shape.Circle;
import gizmoball.engine.geometry.shape.Rectangle;
import gizmoball.engine.geometry.shape.Triangle;
import gizmoball.engine.physics.Mass;
import gizmoball.engine.physics.PhysicsBody;
import gizmoball.engine.world.entity.*;
import gizmoball.ui.visualize.ImagePhysicsBody;
import javafx.scene.Cursor;
import javafx.scene.layout.VBox;
import lombok.Getter;
import lombok.Setter;

import java.util.function.Function;

@Getter
@Setter
public class DraggableGizmoComponent extends ImageLabelComponent {

    protected static final Function<Vector2, ImagePhysicsBody> ballBodyCreator = (preferredSize) -> {
        Ball ball = new Ball(preferredSize.x / 2.0);
        ImagePhysicsBody imagePhysicsBody = new ImagePhysicsBody(ball);
        imagePhysicsBody.setMass(imagePhysicsBody.getShape().createMass(1));
        imagePhysicsBody.setRestitution(0.95);
        imagePhysicsBody.setFriction(0.4);
        imagePhysicsBody.setRestitutionVelocity(10);
        return imagePhysicsBody;
    };


    protected static final Function<Vector2, ImagePhysicsBody> circleBodyCreator = (preferredSize) -> {
        Circle circle = new Circle(preferredSize.x / 2.0);
        ImagePhysicsBody imagePhysicsBody = new ImagePhysicsBody(circle);
        imagePhysicsBody.setMass(new Mass(new Vector2(), 0.0, 0.0));
        imagePhysicsBody.setRestitution(0.95);
        imagePhysicsBody.setFriction(0.5);
        imagePhysicsBody.setRestitutionVelocity(10);
        return imagePhysicsBody;
    };

    protected static final Function<Vector2, ImagePhysicsBody> blackHoleBodyCreator = (preferredSize) -> {
        BlackHole blackhole = new BlackHole(preferredSize.x / 2.0);
        ImagePhysicsBody imagePhysicsBody = new ImagePhysicsBody(blackhole);
        imagePhysicsBody.setMass(new Mass(new Vector2(), 0.0, 0.0));
        return imagePhysicsBody;
    };

    protected static final Function<Vector2, ImagePhysicsBody> rectangleBodyCreator = (preferredSize) -> {
        Rectangle rectangle = new Rectangle(preferredSize.x / 2.0, preferredSize.y / 2.0);
        ImagePhysicsBody imagePhysicsBody = new ImagePhysicsBody(rectangle);
        imagePhysicsBody.setMass(new Mass(new Vector2(), 0.0, 0.0));
        imagePhysicsBody.setRestitution(0.95);
        imagePhysicsBody.setFriction(0.5);
        imagePhysicsBody.setRestitutionVelocity(10);
        return imagePhysicsBody;
    };

    protected static final Function<Vector2, ImagePhysicsBody> pipeBodyCreator = (preferredSize) -> {
        Pipe pipe = new Pipe(preferredSize.x / 2.0, preferredSize.y / 2.0);
        ImagePhysicsBody imagePhysicsBody = new ImagePhysicsBody(pipe);
        imagePhysicsBody.setMass(new Mass(new Vector2(), 0.0, 0.0));
        imagePhysicsBody.setRestitution(0.95);
        imagePhysicsBody.setFriction(0.0);
        imagePhysicsBody.setRestitutionVelocity(10);
        return imagePhysicsBody;
    };

    protected static final Function<Vector2, ImagePhysicsBody> curvedPipeBodyCreator = (preferredSize) -> {
        CurvedPipe curvedPipe = new CurvedPipe(preferredSize.x);
        ImagePhysicsBody imagePhysicsBody = new ImagePhysicsBody(curvedPipe);
        imagePhysicsBody.setMass(new Mass(new Vector2(), 0.0, 0.0));
        imagePhysicsBody.setRestitution(0.95);
        imagePhysicsBody.setFriction(0.0);
        return imagePhysicsBody;
    };

    protected static final Function<Vector2, ImagePhysicsBody> triangleBodyCreator = (preferredSize) -> {
        Vector2[] vertices = new Vector2[]{
                new Vector2(-preferredSize.y / 2.0, -preferredSize.y / 2.0),
                new Vector2(preferredSize.x / 2.0, -preferredSize.y / 2.0),
                new Vector2(-preferredSize.y / 2.0, preferredSize.y / 2.0)
        };
        Triangle triangle = new Triangle(vertices);
        ImagePhysicsBody imagePhysicsBody = new ImagePhysicsBody(triangle);
        imagePhysicsBody.setMass(new Mass(new Vector2(), 0.0, 0.0));
        imagePhysicsBody.setRestitution(0.95);
        imagePhysicsBody.setFriction(0.5);
        imagePhysicsBody.setRestitutionVelocity(10);
        return imagePhysicsBody;
    };

    protected static final Function<Vector2, ImagePhysicsBody> leftFlipperBodyCreator = (preferredSize) -> {
        Flipper flipper = new Flipper(preferredSize.x / 2.0, preferredSize.y / 4.0 / 2.0, Flipper.Direction.LEFT);
        ImagePhysicsBody imagePhysicsBody = new ImagePhysicsBody(flipper);
        imagePhysicsBody.setMass(new Mass(new Vector2(), 0.0, 0.0));
        imagePhysicsBody.setRestitution(0.95);
        imagePhysicsBody.setFriction(0.5);
        imagePhysicsBody.setRestitutionVelocity(10);
        return imagePhysicsBody;
    };

    protected static final Function<Vector2, ImagePhysicsBody> rightFlipperBodyCreator = (preferredSize) -> {
        Flipper flipper = new Flipper(preferredSize.x / 2.0, preferredSize.y / 4.0 / 2.0, Flipper.Direction.RIGHT);
        ImagePhysicsBody imagePhysicsBody = new ImagePhysicsBody(flipper);
        imagePhysicsBody.setMass(new Mass(new Vector2(), 0.0, 0.0));
        imagePhysicsBody.setRestitution(0.95);
        imagePhysicsBody.setFriction(0.5);
        imagePhysicsBody.setRestitutionVelocity(10);
        return imagePhysicsBody;
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
     *
     * @param preferredSize the preferred size of the gizmo.
     * @param center        the center of the gizmo.
     * @return the physics body.
     */
    public PhysicsBody createPhysicsBody(Vector2 preferredSize, Vector2 center) {
        ImagePhysicsBody physicsBody = (ImagePhysicsBody) gizmoType.getPhysicsBodySupplier().apply(preferredSize);
        physicsBody.getShape().translate(center);
        physicsBody.setGizmoType(gizmoType);
        return physicsBody;
    }
}
