package gizmoball.ui;

import gizmoball.engine.geometry.Transform;
import gizmoball.engine.geometry.Vector2;
import gizmoball.engine.geometry.shape.AbstractShape;
import gizmoball.engine.geometry.shape.Circle;
import gizmoball.engine.geometry.shape.Rectangle;
import gizmoball.engine.geometry.shape.Triangle;
import gizmoball.engine.physics.PhysicsBody;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.function.Function;
import java.util.function.Supplier;

@Getter
@AllArgsConstructor
public enum GizmoType {

    BALL("gizmo/ball", DraggableGizmoComponent.circleBodyCreator),

    RECTANGLE("gizmo/rectangle", DraggableGizmoComponent.rectangleBodyCreator),

    TRIANGLE("gizmo/triangle", DraggableGizmoComponent.triangleBodyCreator),

    CIRCLE("gizmo/circle", DraggableGizmoComponent.circleBodyCreator),

    BLACK_HOLE("gizmo/blackHole", DraggableGizmoComponent.circleBodyCreator),

    PIPE("gizmo/pipe", DraggableGizmoComponent.rectangleBodyCreator),

    CURVED_PIPE("gizmo/curvedPipe", DraggableGizmoComponent.curvedPipeBodyCreator),

    LEFT_FLIPPER("gizmo/leftFlipper", DraggableGizmoComponent.flipperBodyCreator),

    RIGHT_FLIPPER("gizmo/rightFlipper", DraggableGizmoComponent.flipperBodyCreator),

    ;
    private final String id;

    private final Function<Vector2, PhysicsBody> physicsBodySupplier;

    @Override
    public String toString() {
        return id;
    }
}
