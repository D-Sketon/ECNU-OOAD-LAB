package gizmoball.ui.visualize;

import gizmoball.engine.physics.PhysicsBody;
import javafx.scene.canvas.GraphicsContext;

public interface CanvasRenderer {

    CanvasRenderer DO_NOT_RENDER = (graphicsContext, physicsBody) -> {
        // do nothing
    };

    void drawToCanvas(GraphicsContext graphicsContext, PhysicsBody physicsBody);
}
