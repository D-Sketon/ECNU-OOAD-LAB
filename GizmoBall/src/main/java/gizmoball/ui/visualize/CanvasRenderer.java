package gizmoball.ui.visualize;

import gizmoball.engine.physics.PhysicsBody;
import javafx.scene.canvas.GraphicsContext;

public interface CanvasRenderer {

    CanvasRenderer DO_NOT_RENDER = new CanvasRenderer() {
        @Override
        public void drawToCanvas(GraphicsContext graphicsContext, PhysicsBody physicsBody) {
            // do nothing
        }
    };

    void drawToCanvas(GraphicsContext graphicsContext, PhysicsBody physicsBody);
}
