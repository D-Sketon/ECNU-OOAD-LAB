package gizmoball.engine.geometry.shape;

import gizmoball.engine.collision.Interval;
import gizmoball.engine.collision.feature.EdgeFeature;
import gizmoball.engine.collision.feature.PointFeature;
import gizmoball.engine.geometry.AABB;
import gizmoball.engine.geometry.Transform;
import gizmoball.engine.geometry.Vector2;
import lombok.Getter;

@Getter
public abstract class Polygon extends AbstractShape {

    /**
     * 多边形顶点数组
     */
    protected Vector2[] vertices;

    /**
     * 多边形法向量数组
     */
    protected Vector2[] normals;

    protected Polygon(Transform transform) {
        super(transform);
    }

    protected Polygon(Transform transform, Vector2[] vertices, Vector2[] normals) {
        super(transform);
        this.vertices = vertices;
        this.normals = normals;
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
            return new EdgeFeature(vm, vl, vm, maximum.to(left), index + 1);
        } else {
            int r = (index == 0) ? count - 1 : index - 1;

            Vector2 right = transform.getTransformed(this.vertices[r]);
            PointFeature vr = new PointFeature(right);
            return new EdgeFeature(vr, vm, vm, right.to(maximum), index);
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

}
