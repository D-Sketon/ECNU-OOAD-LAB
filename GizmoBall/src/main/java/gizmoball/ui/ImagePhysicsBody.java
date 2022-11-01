package gizmoball.ui;

import gizmoball.engine.geometry.shape.AbstractShape;
import gizmoball.engine.physics.PhysicsBody;
import javafx.scene.image.Image;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ImagePhysicsBody extends PhysicsBody {

    private Image image;

    public ImagePhysicsBody(AbstractShape shape) {
        super(shape);
    }

    public ImagePhysicsBody(AbstractShape shape, Image image) {
        super(shape);
        this.image = image;
    }
}
