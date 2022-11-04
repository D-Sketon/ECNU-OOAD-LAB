package gizmoball.ui.visualize;

import gizmoball.engine.physics.PhysicsBody;
import javafx.scene.canvas.GraphicsContext;

public interface CanvasRenderer {

    void drawToCanvas(GraphicsContext graphicsContext, PhysicsBody physicsBody);
}
