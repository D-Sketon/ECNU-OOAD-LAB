package gizmoball.ui;

import gizmoball.engine.geometry.shape.AbstractShape;
import gizmoball.engine.physics.PhysicsBody;
import gizmoball.ui.component.SVGNode;
import javafx.scene.image.Image;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ImagePhysicsBody extends PhysicsBody {

    private Image image;

    private SVGNode svgNode;

    public ImagePhysicsBody(AbstractShape shape) {
        super(shape);
    }

    public ImagePhysicsBody(AbstractShape shape, Image image) {
        super(shape);
        this.image = image;
    }

    public ImagePhysicsBody(AbstractShape shape, SVGNode svgNode) {
        super(shape);
        this.svgNode = svgNode;
    }
}
