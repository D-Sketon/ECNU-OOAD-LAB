package gizmoball.ui.component;

import gizmoball.engine.geometry.Vector2;
import gizmoball.engine.physics.PhysicsBody;
import gizmoball.ui.visualize.CanvasRenderer;
import gizmoball.ui.visualize.DefaultCanvasRenderer;
import gizmoball.ui.visualize.ImageRenderer;
import gizmoball.ui.visualize.SVGRenderer;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.function.Function;

@Getter
@AllArgsConstructor
public enum GizmoType{

    BALL("gizmo/ball", DraggableGizmoComponent.ballBodyCreator, new SVGRenderer("icons/ball.svg")),

    CIRCLE("gizmo/circle", DraggableGizmoComponent.circleBodyCreator, new SVGRenderer("icons/circle.svg")),

    BLACK_HOLE("gizmo/blackHole", DraggableGizmoComponent.blackholeBodyCreator, new SVGRenderer("icons/black_hole.svg")),

    RECTANGLE("gizmo/rectangle", DraggableGizmoComponent.rectangleBodyCreator, new SVGRenderer("icons/rectangle.svg")),

    TRIANGLE("gizmo/triangle", DraggableGizmoComponent.triangleBodyCreator, DefaultCanvasRenderer.INSTANCE),

    PIPE("gizmo/pipe", DraggableGizmoComponent.pipeBodyCreator, new ImageRenderer("icons/pipe.png")),

    CURVED_PIPE("gizmo/curvedPipe", DraggableGizmoComponent.curvedPipeBodyCreator, new ImageRenderer("icons/quarter_circle.png")),

    LEFT_FLIPPER("gizmo/leftFlipper", DraggableGizmoComponent.leftFlipperBodyCreator, DefaultCanvasRenderer.INSTANCE),

    RIGHT_FLIPPER("gizmo/rightFlipper", DraggableGizmoComponent.rightFlipperBodyCreator, DefaultCanvasRenderer.INSTANCE),

    ;

    private final String id;

    private final Function<Vector2, ? extends PhysicsBody> physicsBodySupplier;

    private final CanvasRenderer canvasRenderer;

    @Override
    public String toString() {
        return id;
    }
}
