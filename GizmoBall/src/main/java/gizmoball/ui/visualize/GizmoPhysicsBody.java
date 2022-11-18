package gizmoball.ui.visualize;

import gizmoball.engine.geometry.shape.AbstractShape;
import gizmoball.engine.physics.PhysicsBody;
import gizmoball.ui.component.GizmoType;
import javafx.scene.canvas.GraphicsContext;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GizmoPhysicsBody extends PhysicsBody implements CanvasRenderer {

    private GizmoType gizmoType;

    /**
     * 反序列化用
     */
    @Deprecated
    public GizmoPhysicsBody() {
        super();
    }

    public GizmoPhysicsBody(AbstractShape shape) {
        super(shape);
    }

    public GizmoPhysicsBody(AbstractShape shape, GizmoType gizmoType) {
        super(shape);
        this.gizmoType = gizmoType;
    }

    public void drawToCanvas(GraphicsContext graphicsContext) {
        drawToCanvas(graphicsContext, this);
    }

    @Override
    public void drawToCanvas(GraphicsContext graphicsContext, PhysicsBody physicsBody) {
        gizmoType.getCanvasRenderer().drawToCanvas(graphicsContext, physicsBody);
    }


}
