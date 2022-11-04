package gizmoball.ui.component;

import com.fasterxml.jackson.annotation.JsonIgnore;
import gizmoball.engine.geometry.Vector2;
import gizmoball.engine.physics.PhysicsBody;
import gizmoball.ui.component.DraggableGizmoComponent;
import gizmoball.ui.visualize.CanvasRenderer;
import gizmoball.ui.visualize.DefaultCanvasRenderer;
import gizmoball.ui.visualize.ImageRenderer;
import gizmoball.ui.visualize.SvgRenderer;
import javafx.scene.canvas.GraphicsContext;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.function.Function;

@Getter
@AllArgsConstructor
public enum GizmoType{

    BALL("gizmo/ball", DraggableGizmoComponent.circleBodyCreator, new SvgRenderer("icons/ball.svg")),

    RECTANGLE("gizmo/rectangle", DraggableGizmoComponent.rectangleBodyCreator, new SvgRenderer("icons/rectangle.svg")),

    TRIANGLE("gizmo/triangle", DraggableGizmoComponent.triangleBodyCreator, DefaultCanvasRenderer.INSTANCE),

    CIRCLE("gizmo/circle", DraggableGizmoComponent.circleBodyCreator, new SvgRenderer("icons/circle.svg")),

    BLACK_HOLE("gizmo/blackHole", DraggableGizmoComponent.circleBodyCreator, new SvgRenderer("icons/black_hole.svg")),

    PIPE("gizmo/pipe", DraggableGizmoComponent.rectangleBodyCreator, new SvgRenderer("icons/pipe.svg")),

    CURVED_PIPE("gizmo/curvedPipe", DraggableGizmoComponent.curvedPipeBodyCreator, new ImageRenderer("icons/quarter_circle.png")),

    LEFT_FLIPPER("gizmo/leftFlipper", DraggableGizmoComponent.flipperBodyCreator, DefaultCanvasRenderer.INSTANCE),

    RIGHT_FLIPPER("gizmo/rightFlipper", DraggableGizmoComponent.flipperBodyCreator, DefaultCanvasRenderer.INSTANCE),

    ;

    private final String id;

    private final Function<Vector2, ? extends PhysicsBody> physicsBodySupplier;

    private final CanvasRenderer canvasRenderer;

    @Override
    public String toString() {
        return id;
    }
}
