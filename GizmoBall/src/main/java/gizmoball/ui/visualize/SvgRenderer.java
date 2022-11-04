package gizmoball.ui.visualize;

import gizmoball.engine.geometry.AABB;
import gizmoball.engine.geometry.Transform;
import gizmoball.engine.geometry.shape.AbstractShape;
import gizmoball.engine.physics.PhysicsBody;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.transform.Affine;

/**
 * <p><b>仅支持iconfont下载下来的svg</b></p>
 * <p>因为{@link javafx.scene.image.Image}不支持SVG，所以没有继承{@link ImageRenderer}</p>
 */
public class SvgRenderer implements CanvasRenderer {

    private final SVGNode svgNode;

    public SvgRenderer(String resource) {
        this.svgNode = SVGNode.fromResource(getClass().getClassLoader().getResourceAsStream(resource));
    }

    @Override
    public void drawToCanvas(GraphicsContext gc, PhysicsBody body) {
        AbstractShape shape = body.getShape();
        Transform transform = shape.getTransform();

        AABB aabb = body.getShape().createAABB();
        double shapeHeight = aabb.maxY - aabb.minY;
        double shapeWidth = aabb.maxX - aabb.minX;

        if(svgNode != null){
            gc.save();

            Affine affine = new Affine();
            affine.appendRotation(transform.getAngle(), transform.x, transform.y); // TODO center
            affine.appendTranslation(transform.getX() - shapeWidth / 2,
                    transform.getY() - shapeHeight / 2 + shapeHeight); // aabb.maxY - aabb.minY为了处理图片上下翻转
            affine.appendScale(shapeHeight / 1024 , -shapeWidth / 1024);
            gc.transform(affine);

            gc.beginPath();
            for (SVGPath svgPath : svgNode.getSvgPaths()) {
                gc.appendSVGPath(svgPath.getPath());
                gc.setFill(svgPath.getFill());
            }
            gc.fill();
            gc.closePath();

            gc.restore();
        }
    }
}
