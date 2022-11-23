package gizmoball.ui.visualize;

import gizmoball.engine.geometry.Transform;
import gizmoball.engine.geometry.Vector2;
import gizmoball.engine.geometry.shape.AbstractShape;
import gizmoball.engine.geometry.shape.Circle;
import gizmoball.engine.geometry.shape.Polygon;
import gizmoball.engine.physics.PhysicsBody;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class DefaultCanvasRenderer implements CanvasRenderer {

    public static final DefaultCanvasRenderer INSTANCE = new DefaultCanvasRenderer();

    private static final Color FILL_COLOR = Color.valueOf("#1296db");

    private DefaultCanvasRenderer() {
    }

    @Override
    public void drawToCanvas(GraphicsContext gc, PhysicsBody physicsBody) {
        AbstractShape shape = physicsBody.getShape();
        Transform transform = shape.getTransform();

        if (shape instanceof Polygon) {
            // 画多边形
            Polygon polygon = (Polygon) shape;
            gc.setFill(FILL_COLOR);

            Vector2[] vertices = polygon.getVertices();
            double[] xpoints = new double[vertices.length];
            double[] ypoints = new double[vertices.length];
            for (int i = 0; i < vertices.length; i++) {
                Vector2 transformed = transform.getTransformed(vertices[i]);
                xpoints[i] = transformed.x;
                ypoints[i] = transformed.y;
            }
            gc.fillPolygon(xpoints, ypoints, vertices.length);
        } else if (shape instanceof Circle) {
            Circle circle = (Circle) shape;
            gc.setFill(FILL_COLOR);
            gc.fillOval(circle.getTransform().getX() - circle.getRadius(),
                    circle.getTransform().getY() - circle.getRadius(),
                    circle.getRadius() * 2, circle.getRadius() * 2);
        }
    }
}
