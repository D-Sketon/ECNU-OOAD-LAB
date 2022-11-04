package gizmoball.ui.visualize;

import gizmoball.engine.geometry.shape.AbstractShape;
import gizmoball.engine.physics.PhysicsBody;
import gizmoball.ui.component.GizmoType;
import gizmoball.ui.visualize.CanvasRenderer;
import javafx.scene.canvas.GraphicsContext;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ImagePhysicsBody extends PhysicsBody implements CanvasRenderer {

    private GizmoType gizmoType;

    /**
     * 反序列化用
     */
    @Deprecated
    public ImagePhysicsBody() {
        super();
    }

    public ImagePhysicsBody(AbstractShape shape) {
        super(shape);
    }

    public ImagePhysicsBody(AbstractShape shape, GizmoType gizmoType) {
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
