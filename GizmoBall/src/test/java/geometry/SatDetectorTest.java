package geometry;

import gizmoball.engine.collision.Penetration;
import gizmoball.engine.collision.detector.SatDetector;
import gizmoball.engine.geometry.Epsilon;
import gizmoball.engine.geometry.Transform;
import gizmoball.engine.geometry.Vector2;
import gizmoball.engine.geometry.shape.Rectangle;
import org.junit.Assert;
import org.junit.Test;

public class SatDetectorTest {

    @Test
    public void satDetectorWithPenetrationTest() {
        Transform transform1 = new Transform(1, 0, 2.5, 2.5);
        Rectangle rectangle1 = new Rectangle(2.5, 2.5, transform1);
        Transform transform2 = new Transform(Math.sqrt(2) / 2, Math.sqrt(2) / 2, 5, 2);
        Rectangle rectangle2 = new Rectangle(0.5, 0.5, transform2);
        Penetration penetration1 = new Penetration();
        Assert.assertTrue(SatDetector.detect(rectangle1, rectangle2, penetration1));
        Assert.assertEquals(penetration1.getNormal(), new Vector2(1, 0));
        Assert.assertEquals(penetration1.getDepth(), Math.sqrt(2) / 2, Epsilon.E * 10);

        Penetration penetration2 = new Penetration();
        Assert.assertTrue(SatDetector.detect(rectangle2, rectangle1, penetration2));
        Assert.assertEquals(penetration2.getNormal(), new Vector2(-1, 0));
        Assert.assertEquals(penetration2.getDepth(), Math.sqrt(2) / 2, Epsilon.E * 10);
        
        System.out.println(rectangle1.getFarthestFeature(penetration1.getNormal()));
        System.out.println(rectangle2.getFarthestFeature(penetration2.getNormal()));
    }
}
