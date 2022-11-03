package gizmoball.ui.component;

import gizmoball.engine.geometry.Vector2;
import gizmoball.engine.physics.PhysicsBody;
import gizmoball.ui.component.DraggableGizmoComponent;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.function.Function;

@Getter
@AllArgsConstructor
public enum GizmoType {

    BALL("gizmo/ball", DraggableGizmoComponent.circleBodyCreator),

    RECTANGLE("gizmo/rectangle", DraggableGizmoComponent.rectangleBodyCreator),

    TRIANGLE("gizmo/triangle", DraggableGizmoComponent.triangleBodyCreator),

    CIRCLE("gizmo/circle", DraggableGizmoComponent.circleBodyCreator),

    BLACK_HOLE("gizmo/blackHole", DraggableGizmoComponent.circleBodyCreator),

    PIPE("gizmo/pipe", DraggableGizmoComponent.pipeBodyCreator),

    CURVED_PIPE("gizmo/curvedPipe", DraggableGizmoComponent.curvedPipeBodyCreator),

    LEFT_FLIPPER("gizmo/leftFlipper", DraggableGizmoComponent.flipperBodyCreator),

    RIGHT_FLIPPER("gizmo/rightFlipper", DraggableGizmoComponent.flipperBodyCreator),

    ;
    private final String id;

    private final Function<Vector2, ? extends PhysicsBody> physicsBodySupplier;

    @Override
    public String toString() {
        return id;
    }
}
