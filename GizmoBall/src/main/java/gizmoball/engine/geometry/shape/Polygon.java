package gizmoball.engine.geometry.shape;

import gizmoball.engine.Settings;
import gizmoball.engine.collision.Interval;
import gizmoball.engine.collision.feature.EdgeFeature;
import gizmoball.engine.collision.feature.PointFeature;
import gizmoball.engine.geometry.AABB;
import gizmoball.engine.geometry.Transform;
import gizmoball.engine.geometry.Vector2;
import gizmoball.engine.physics.Mass;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@ToString(callSuper = true)
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

    public Polygon(Vector2[] vertices) {
        this(new Transform(), vertices, getCounterClockwiseEdgeNormals(vertices));
    }

    public Polygon(Transform transform, Vector2[] vertices, Vector2[] normals) {
        super(transform);
        this.vertices = vertices;
        this.normals = normals;
    }

    public static Vector2[] getCounterClockwiseEdgeNormals(Vector2[] vertices) {
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
    public EdgeFeature getFarthestFeature(Vector2 vector) {
        Vector2 localn = transform.getInverseTransformedR(vector);

        int index = getFarthestVertexIndex(localn);
        int count = this.vertices.length;

        Vector2 maximum = new Vector2(this.vertices[index]);
        transform.transform(maximum);
        PointFeature vm = new PointFeature(maximum);

        Vector2 leftN = this.normals[index == 0 ? count - 1 : index - 1];
        Vector2 rightN = this.normals[index];

        if (leftN.dot(localn) < rightN.dot(localn)) {
            int l = (index == count - 1) ? 0 : index + 1;

            Vector2 left = transform.getTransformed(this.vertices[l]);
            PointFeature vl = new PointFeature(left);
            return new EdgeFeature(vm, vl, vm, maximum.to(left));
        } else {
            int r = (index == 0) ? count - 1 : index - 1;

            Vector2 right = transform.getTransformed(this.vertices[r]);
            PointFeature vr = new PointFeature(right);
            return new EdgeFeature(vr, vm, vm, right.to(maximum));
        }
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
        // can't use normal centroid calculation since it will be weighted towards sides
        // that have larger distribution of points.
        Vector2 center = new Vector2();
        double area = 0.0;
        double I = 0.0;
        int n = this.vertices.length;
        // get the average center
        Vector2 ac = new Vector2();
        for (Vector2 vertex : this.vertices) {
            ac.add(vertex);
        }
        ac.divide(n);
        // loop through the vertices using two variables to avoid branches in the loop
        for (int i1 = n - 1, i2 = 0; i2 < n; i1 = i2++) {
            // get two vertices
            Vector2 p1 = this.vertices[i1];
            Vector2 p2 = this.vertices[i2];
            // get the vector from the center to the point
            p1 = p1.difference(ac);
            p2 = p2.difference(ac);
            // perform the cross product (yi * x(i+1) - y(i+1) * xi)
            double D = p1.cross(p2);
            // multiply by half
            double triangleArea = 0.5 * D;
            // add it to the total area
            area += triangleArea;

            // area weighted centroid
            // (p1 + p2) * (D / 6)
            // = (x1 + x2) * (yi * x(i+1) - y(i+1) * xi) / 6
            // we will divide by the total area later
            center.x += (p1.x + p2.x) * Settings.INV_3 * triangleArea;
            center.y += (p1.y + p2.y) * Settings.INV_3 * triangleArea;

            // (yi * x(i+1) - y(i+1) * xi) * (p2^2 + p2 . p1 + p1^2)
            I += triangleArea * (p2.dot(p2) + p2.dot(p1) + p1.dot(p1));
            // we will do the m / 6A = (d / 6) when we have the area summed up
        }
        area = Math.abs(area);
        // compute the mass
        double m = density * area;
        // finish the centroid calculation by dividing by the total area
        // and adding in the average center
        center.divide(area);
        Vector2 c = center.sum(ac);
        // finish the inertia tensor by dividing by the total area and multiplying by d / 6
        I *= (density / 6.0);
        // shift the axis of rotation to the area weighted center
        // (center is the vector from the average center to the area weighted center since
        // the average center is used as the origin)
        I -= m * center.getMagnitudeSquared();

        return new Mass(c, m, I);
    }

}
