package geometry;

import gizmoball.engine.collision.detector.CircleDetector;
import gizmoball.engine.geometry.Transform;
import gizmoball.engine.geometry.shape.Circle;
import org.junit.Assert;
import org.junit.Test;

public class CircleDetectorTest {

    @Test
    public void circleDetectorWithoutPenetrationTest() {
        Circle circle1 = new Circle(new Transform(1, 0, 0, 0));
        circle1.setRadius(1);
        Circle circle2 = new Circle(new Transform(1, 0, 0, 0));
        circle2.setRadius(2);
        Assert.assertTrue(CircleDetector.detect(circle1, circle2, null));

        // 相切不算碰撞
        circle1 = new Circle(new Transform(1, 0, 1, 0));
        circle1.setRadius(1);
        circle2 = new Circle(new Transform(1, 0, 3, 0));
        circle2.setRadius(1);
        Assert.assertFalse(CircleDetector.detect(circle1, circle2, null));

        circle1 = new Circle(new Transform(1, 0, 1, 0));
        circle1.setRadius(1);
        circle2 = new Circle(new Transform(1, 0, 3, 0));
        circle2.setRadius(2);
        Assert.assertTrue(CircleDetector.detect(circle1, circle2, null));

        circle1 = new Circle(new Transform(1, 0, 1, 0));
        circle1.setRadius(1);
        circle2 = new Circle(new Transform(1, 0, 10, 0));
        circle2.setRadius(2);
        Assert.assertFalse(CircleDetector.detect(circle1, circle2, null));
    }
}

