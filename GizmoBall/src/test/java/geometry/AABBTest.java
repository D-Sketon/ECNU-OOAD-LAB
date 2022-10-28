package geometry;

import gizmoball.engine.geometry.AABB;
import gizmoball.engine.geometry.Epsilon;
import gizmoball.engine.geometry.Transform;
import gizmoball.engine.geometry.Vector2;
import gizmoball.engine.geometry.shape.Circle;
import gizmoball.engine.geometry.shape.Rectangle;
import gizmoball.engine.geometry.shape.Triangle;
import org.junit.Assert;
import org.junit.Test;

public class AABBTest {

    @Test
    public void circleAABBTest() {
        // 基础圆形
        Transform transform = new Transform(1, 0, 0, 0);
        Circle circle = new Circle(transform);
        circle.setRadius(1);
        Assert.assertEquals(circle.createAABB(), new AABB(-1, -1, 1, 1));

        transform = new Transform(1, 0, 5, 5);
        circle = new Circle(transform);
        circle.setRadius(1);
        Assert.assertEquals(circle.createAABB(), new AABB(4, 4, 6, 6));
        // 平移后的圆形
        circle.translate(new Vector2(-5, -5));
        Assert.assertEquals(circle.createAABB(), new AABB(-1, -1, 1, 1));
        // 缩放后的圆形
        circle.zoom(2);
        Assert.assertEquals(circle.createAABB(), new AABB(-2, -2, 2, 2));
        circle.zoom(1);
        Assert.assertEquals(circle.createAABB(), new AABB(-1, -1, 1, 1));
        // 旋转后的圆形（不建议传角度）
        circle.rotate(0, 1, 1, 0);
        Assert.assertEquals(circle.createAABB(), new AABB(0, -2, 2, 0));

        circle.rotate(Math.sqrt(2) / 2, Math.sqrt(2) / 2, 0, 0);
        Assert.assertEquals(circle.createAABB(), new AABB(Math.sqrt(2) - 1, -1, Math.sqrt(2) + 1, 1));
    }

    @Test
    public void triangleAABBTest() {
        // 基础三角形
        Transform transform = new Transform(1, 0, 0, 0);
        Vector2[] vector2s = {new Vector2(0, 0), new Vector2(1, 1), new Vector2(1, Math.sqrt(3))};
        Triangle triangle = new Triangle(vector2s, transform);
        Assert.assertEquals(triangle.createAABB(), new AABB(0, 0, 1, Math.sqrt(3)));

        transform = new Transform(1, 0, 5, 5);
        triangle = new Triangle(vector2s, transform);

        Assert.assertEquals(triangle.createAABB(), new AABB(5, 5, 6, 5 + Math.sqrt(3)));
        // 平移后的三角形
        triangle.translate(new Vector2(-5, -5));
        Assert.assertEquals(triangle.createAABB(), new AABB(0, 0, 1, Math.sqrt(3)));
        // 缩放后的三角形（transform中心点被设定为0,0）
        triangle.zoom(2);
        Assert.assertEquals(triangle.createAABB(), new AABB(0, 0, 2, 2 * Math.sqrt(3)));
        triangle.zoom(1);
        Assert.assertEquals(triangle.createAABB(), new AABB(0, 0, 1, Math.sqrt(3)));
        // 旋转后的三角形（不建议传角度）
        triangle.rotate(0, 1, 1, 0);
        Assert.assertEquals(triangle.createAABB(), new AABB(1 - Math.sqrt(3), -1, 1, 0));

        triangle.rotate(Math.sqrt(2) / 2, Math.sqrt(2) / 2, 0, 0);
        AABB aabb = triangle.createAABB();
        // 多次旋转会出现精度问题
        // 误差大于Epsilon.E
        Assert.assertEquals(aabb.getMinX(), -(Math.sqrt(6) - Math.sqrt(2)) / 2, Epsilon.E * 10);
        Assert.assertEquals(aabb.getMinY(), -(Math.sqrt(6) - Math.sqrt(2)) / 2, Epsilon.E * 10);
        Assert.assertEquals(aabb.getMaxX(), Math.sqrt(2), Epsilon.E * 10);
        Assert.assertEquals(aabb.getMaxY(), 0, Epsilon.E * 10);

        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 12; j++)
                triangle.rotate(Math.sqrt(3) / 2, 0.5, 0, 0);
        }
        aabb = triangle.createAABB();
        // 多次旋转会出现精度问题
        // 误差大于Epsilon.E
        System.out.println(aabb.getMinX());
        System.out.println(-(Math.sqrt(6) - Math.sqrt(2)) / 2);
        Assert.assertEquals(aabb.getMinX(), -(Math.sqrt(6) - Math.sqrt(2)) / 2, Epsilon.E * 10);
        Assert.assertEquals(aabb.getMinY(), -(Math.sqrt(6) - Math.sqrt(2)) / 2, Epsilon.E * 10);
        Assert.assertEquals(aabb.getMaxX(), Math.sqrt(2), Epsilon.E * 10);
        Assert.assertEquals(aabb.getMaxY(), 0, Epsilon.E * 10);
    }

    @Test
    public void rectangleAABBTest() {
        // 基础正方形
        Transform transform = new Transform(1, 0, 0, 0);
        Rectangle rectangle = new Rectangle(1, 1, transform);
        Assert.assertEquals(rectangle.createAABB(), new AABB(-1, -1, 1, 1));

        transform = new Transform(1, 0, 5, 5);
        rectangle = new Rectangle(1, 1, transform);
        Assert.assertEquals(rectangle.createAABB(), new AABB(4, 4, 6, 6));
        // 平移后的正方形
        rectangle.translate(new Vector2(-5, -5));
        Assert.assertEquals(rectangle.createAABB(), new AABB(-1, -1, 1, 1));
        // 缩放后的正方形
        rectangle.zoom(2);
        Assert.assertEquals(rectangle.createAABB(), new AABB(-2, -2, 2, 2));
        rectangle.zoom(1);
        Assert.assertEquals(rectangle.createAABB(), new AABB(-1, -1, 1, 1));
        // 旋转后的正方形（不建议传角度）
        rectangle.rotate(0, 1, 1, 0);
        Assert.assertEquals(rectangle.createAABB(), new AABB(0, -2, 2, 0));

        rectangle.rotate(Math.sqrt(2) / 2, Math.sqrt(2) / 2, 0, 0);
        Assert.assertEquals(rectangle.createAABB(), new AABB(0, -Math.sqrt(2), Math.sqrt(2) * 2, Math.sqrt(2)));
    }

    @Test
    public void quarterCircleAABBTest() {
        // TODO
    }

}
