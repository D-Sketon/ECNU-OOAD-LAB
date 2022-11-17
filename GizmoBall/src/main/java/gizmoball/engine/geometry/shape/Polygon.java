package gizmoball.engine.geometry.shape;

import gizmoball.engine.collision.Interval;
import gizmoball.engine.geometry.AABB;
import gizmoball.engine.geometry.Transform;
import gizmoball.engine.geometry.Vector2;
import gizmoball.engine.physics.Mass;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class Polygon extends AbstractShape {

    /**
     * 多边形顶点数组
     */
    protected Vector2[] vertices;

    /**
     * 多边形法向量数组
     */
    protected Vector2[] normals;

    public Polygon(Transform transform, Vector2[] vertices) {
        this(transform, vertices, getCounterClockwiseEdgeNormals(vertices));
    }

    public Polygon(Transform transform, Vector2[] vertices, Vector2[] normals) {
        super(transform);
        this.vertices = vertices;
        this.normals = normals;
    }

    protected static Vector2[] getCounterClockwiseEdgeNormals(Vector2[] vertices) {
        if (vertices == null) return null;

        int size = vertices.length;
        if (size == 0) return null;

        Vector2[] normals = new Vector2[size];
        for (int i = 0; i < size; i++) {
            Vector2 p1 = vertices[i];
            Vector2 p2 = (i + 1 == size) ? vertices[0] : vertices[i + 1];
            Vector2 n = p1.to(p2).left();
            n.normalize();
            normals[i] = n;
        }

        return normals;
    }

    @Override
    public void zoom(int rate) {
        if (rate < 1) {
            return;
        }
        // 缩放时顶点需要调整
        int size = vertices.length;
        for (int i = 0; i < size; i++) {
            vertices[i] = vertices[i].multiply((double) rate / this.rate);
        }
    }

    @Override
    public AABB createAABB() {
        Vector2 p = transform.getTransformed(this.vertices[0]);
        double minX = p.x;
        double maxX = p.x;
        double minY = p.y;
        double maxY = p.y;
        for (int i = 1; i < this.vertices.length; i++) {
            double px = transform.getTransformedX(this.vertices[i]);
            double py = transform.getTransformedY(this.vertices[i]);
            minX = Math.min(px, minX);
            minY = Math.min(py, minY);
            maxX = Math.max(px, maxX);
            maxY = Math.max(py, maxY);
        }
        return new AABB(minX, minY, maxX, maxY);
    }

    @Override
    public Vector2[] getAxes(Vector2[] foci) {
        int fociSize = foci != null ? foci.length : 0;
        int size = this.vertices.length;
        Vector2[] axes = new Vector2[size + fociSize];
        int index = 0;
        for (int i = 0; i < size; i++) {
            axes[index++] = transform.getTransformedR(this.normals[i]);
        }
        for (int i = 0; i < fociSize; i++) {
            Vector2 f = foci[i];
            Vector2 closest = transform.getTransformed(this.vertices[0]);
            double d = f.distanceSquared(closest);
            for (int j = 1; j < size; j++) {
                Vector2 p = transform.getTransformed(this.vertices[j]);
                double dt = f.distanceSquared(p);
                if (dt < d) {
                    closest = p;
                    d = dt;
                }
            }
            Vector2 axis = f.to(closest);
            axis.normalize();
            axes[index++] = axis;
        }
        return axes;
    }

    @Override
    public Vector2[] getFoci() {
        // 多边形没有焦点
        return null;
    }

    @Override
    public Interval project(Vector2 axis) {
        double v;
        Vector2 p = transform.getTransformed(this.vertices[0]);
        double min = axis.dot(p);
        double max = min;
        int size = this.vertices.length;
        for (int i = 1; i < size; i++) {
            p = transform.getTransformed(this.vertices[i]);
            v = axis.dot(p);
            min = Math.min(min, v);
            max = Math.max(max, v);
        }
        return new Interval(min, max);
    }

    @Override
    public Vector2 getFarthestFeature(Vector2 vector) {
        // 多边形没有PointFeature
        return null;
    }

    @Override
    public Vector2 getFarthestPoint(Vector2 vector) {
        Vector2 localn = transform.getInverseTransformedR(vector);
        int index = getFarthestVertexIndex(localn);
        return transform.getTransformed(this.vertices[index]);
    }

    private int getFarthestVertexIndex(Vector2 vector) {
        int maxIndex = 0;
        int n = this.vertices.length;
        double max = vector.dot(this.vertices[0]), candidateMax;

        if (max < (candidateMax = vector.dot(this.vertices[1]))) {
            do {
                max = candidateMax;
                maxIndex++;
            } while ((maxIndex + 1) < n && max < (candidateMax = vector.dot(this.vertices[maxIndex + 1])));
        } else if (max < (candidateMax = vector.dot(this.vertices[n - 1]))) {
            maxIndex = n;
            do {
                max = candidateMax;
                maxIndex--;
            } while (maxIndex > 0 && max <= (candidateMax = vector.dot(this.vertices[maxIndex - 1])));
        }
        return maxIndex;
    }

    @Override
    public Mass createMass(double density) {
        double INV_3 = 1.0 / 3.0;
        Vector2 center = new Vector2();
        // 总面积
        double area = 0.0;
        // 转动惯量
        double I = 0.0;
        int n = this.vertices.length;
        // 获得平均中点
        Vector2 ac = new Vector2();
        for (Vector2 vertex : this.vertices) {
            ac.add(vertex);
        }
        ac.divide(n);

        for (int i1 = n - 1, i2 = 0; i2 < n; i1 = i2++) {
            Vector2 p1 = this.vertices[i1];
            Vector2 p2 = this.vertices[i2];
            p1 = p1.difference(ac);
            p2 = p2.difference(ac);
            // 使用叉乘计算顶点和平均中点围成的的三角形面积
            double D = p1.cross(p2);
            double triangleArea = 0.5 * D;
            area += triangleArea;
            // 计算每个三角形重心的横（纵）坐标
            // 并与该三角形的面积作乘积
            // 最终将每个乘积加和与总面积作除
            // 即为质心的横（纵）坐标
            center.x += (p1.x + p2.x) * INV_3 * triangleArea;
            center.y += (p1.y + p2.y) * INV_3 * triangleArea;

            I += triangleArea * (p2.dot(p2) + p2.dot(p1) + p1.dot(p1));
        }
        area = Math.abs(area);
        center.divide(area);
        // 回到世界坐标系
        Vector2 c = center.sum(ac);

        // 总质量
        double m = density * area;
        I *= (density / 6.0);
        I -= m * center.getMagnitudeSquared();

        return new Mass(c, m, I);
    }

}
