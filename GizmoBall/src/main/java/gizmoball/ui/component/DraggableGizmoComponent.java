package gizmoball.ui.component;


import gizmoball.engine.geometry.Vector2;
import gizmoball.engine.geometry.shape.Circle;
import gizmoball.engine.geometry.shape.Rectangle;
import gizmoball.engine.geometry.shape.Triangle;
import gizmoball.engine.physics.Mass;
import gizmoball.game.entity.*;
import gizmoball.ui.visualize.GizmoPhysicsBody;
import javafx.scene.Cursor;
import javafx.scene.layout.VBox;
import lombok.Getter;
import lombok.Setter;

import java.util.function.Function;

import static gizmoball.game.GizmoSettings.BLACK_HOLE_BIAS;


@Getter
@Setter
public class DraggableGizmoComponent extends ImageLabelComponent {

    protected static final Function<Vector2, GizmoPhysicsBody> ballBodyCreator = (preferredSize) -> {
        Ball ball = new Ball(preferredSize.x / 2.0);
        GizmoPhysicsBody gizmoPhysicsBody = new GizmoPhysicsBody(ball);
        gizmoPhysicsBody.setMass(gizmoPhysicsBody.getShape().createMass(1));
        gizmoPhysicsBody.setRestitution(0.95);
        gizmoPhysicsBody.setFriction(0.4);
        gizmoPhysicsBody.setRestitutionVelocity(10);
        return gizmoPhysicsBody;
    };


    protected static final Function<Vector2, GizmoPhysicsBody> circleBodyCreator = (preferredSize) -> {
        Circle circle = new Circle(preferredSize.x / 2.0);
        GizmoPhysicsBody gizmoPhysicsBody = new GizmoPhysicsBody(circle);
        gizmoPhysicsBody.setMass(new Mass(new Vector2(), 0.0, 0.0));
        gizmoPhysicsBody.setRestitution(0.95);
        gizmoPhysicsBody.setFriction(0.5);
        gizmoPhysicsBody.setRestitutionVelocity(10);
        return gizmoPhysicsBody;
    };

    protected static final Function<Vector2, GizmoPhysicsBody> blackHoleBodyCreator = (preferredSize) -> {
        BlackHole blackhole = new BlackHole(preferredSize.x / 2.0 - BLACK_HOLE_BIAS);
        GizmoPhysicsBody gizmoPhysicsBody = new GizmoPhysicsBody(blackhole);
        gizmoPhysicsBody.setMass(new Mass(new Vector2(), 0.0, 0.0));
        return gizmoPhysicsBody;
    };

    protected static final Function<Vector2, GizmoPhysicsBody> rectangleBodyCreator = (preferredSize) -> {
        Rectangle rectangle = new Rectangle(preferredSize.x / 2.0, preferredSize.y / 2.0);
        GizmoPhysicsBody gizmoPhysicsBody = new GizmoPhysicsBody(rectangle);
        gizmoPhysicsBody.setMass(new Mass(new Vector2(), 0.0, 0.0));
        gizmoPhysicsBody.setRestitution(0.95);
        gizmoPhysicsBody.setFriction(0.5);
        gizmoPhysicsBody.setRestitutionVelocity(10);
        return gizmoPhysicsBody;
    };

    protected static final Function<Vector2, GizmoPhysicsBody> pipeBodyCreator = (preferredSize) -> {
        Pipe pipe = new Pipe(preferredSize.x / 2.0, preferredSize.y / 2.0);
        GizmoPhysicsBody gizmoPhysicsBody = new GizmoPhysicsBody(pipe);
        gizmoPhysicsBody.setMass(new Mass(new Vector2(), 0.0, 0.0));
        gizmoPhysicsBody.setRestitution(0.95);
        gizmoPhysicsBody.setFriction(0.0);
        gizmoPhysicsBody.setRestitutionVelocity(10);
        return gizmoPhysicsBody;
    };

    protected static final Function<Vector2, GizmoPhysicsBody> curvedPipeBodyCreator = (preferredSize) -> {
        CurvedPipe curvedPipe = new CurvedPipe(preferredSize.x);
        GizmoPhysicsBody gizmoPhysicsBody = new GizmoPhysicsBody(curvedPipe);
        gizmoPhysicsBody.setMass(new Mass(new Vector2(), 0.0, 0.0));
        gizmoPhysicsBody.setRestitution(0.95);
        gizmoPhysicsBody.setFriction(0.0);
        return gizmoPhysicsBody;
    };
    protected static final Function<Vector2, GizmoPhysicsBody> triangleBodyCreator = (preferredSize) -> {
        Vector2[] vertices = new Vector2[]{
                new Vector2(-preferredSize.x / 2.0, -preferredSize.y / 2.0),
                new Vector2(preferredSize.x / 2.0, -preferredSize.y / 2.0),
                new Vector2(-preferredSize.x / 2.0, preferredSize.y / 2.0)
        };
        Triangle triangle = new Triangle(vertices);
        GizmoPhysicsBody gizmoPhysicsBody = new GizmoPhysicsBody(triangle);
        gizmoPhysicsBody.setMass(new Mass(new Vector2(), 0.0, 0.0));
        gizmoPhysicsBody.setRestitution(0.95);
        gizmoPhysicsBody.setFriction(0.5);
        gizmoPhysicsBody.setRestitutionVelocity(10);
        return gizmoPhysicsBody;
    };

    protected static final Function<Vector2, GizmoPhysicsBody> leftFlipperBodyCreator = (preferredSize) -> {
        Vector2[] vertices = new Vector2[]{
                new Vector2(-preferredSize.x / 2.0, -preferredSize.y / 4.0 / 2.0),
                new Vector2(preferredSize.x / 2.0, -preferredSize.y / 4.0 / 2.0),
                new Vector2(-preferredSize.x / 2.0, preferredSize.y / 4.0 / 2.0)
        };
        Flipper flipper = new Flipper(vertices, Flipper.Direction.LEFT);
        GizmoPhysicsBody gizmoPhysicsBody = new GizmoPhysicsBody(flipper);
        gizmoPhysicsBody.setMass(new Mass(new Vector2(), 0.0, 0.0));
        gizmoPhysicsBody.setRestitution(0.95);
        gizmoPhysicsBody.setFriction(0.5);
        gizmoPhysicsBody.setRestitutionVelocity(10);
        return gizmoPhysicsBody;
    };

    protected static final Function<Vector2, GizmoPhysicsBody> rightFlipperBodyCreator = (preferredSize) -> {
        Vector2[] vertices = new Vector2[]{
                new Vector2(-preferredSize.y / 2.0, -preferredSize.y / 4.0 / 2.0),
                new Vector2(preferredSize.x / 2.0, -preferredSize.y / 4.0 / 2.0),
                new Vector2(preferredSize.y / 2.0, preferredSize.y / 4.0 / 2.0)
        };
        Flipper flipper = new Flipper(vertices, Flipper.Direction.RIGHT);
        GizmoPhysicsBody gizmoPhysicsBody = new GizmoPhysicsBody(flipper);
        gizmoPhysicsBody.setMass(new Mass(new Vector2(), 0.0, 0.0));
        gizmoPhysicsBody.setRestitution(0.95);
        gizmoPhysicsBody.setFriction(0.5);
        gizmoPhysicsBody.setRestitutionVelocity(10);
        return gizmoPhysicsBody;
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
    public GizmoPhysicsBody createPhysicsBody(Vector2 preferredSize, Vector2 center) {
        GizmoPhysicsBody physicsBody = (GizmoPhysicsBody) gizmoType.getPhysicsBodySupplier().apply(preferredSize);
        physicsBody.getShape().translate(center);
        physicsBody.setGizmoType(gizmoType);
        return physicsBody;
    }
}
