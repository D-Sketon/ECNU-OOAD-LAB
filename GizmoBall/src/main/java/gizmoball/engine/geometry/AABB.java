package gizmoball.engine.geometry;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AABB {

    public double minX;

    public double minY;

    public double maxX;

    public double maxY;

    /**
     * 判断AABB是否重叠
     *
     * @param aabb AABB
     * @return boolean
     */
    public boolean overlaps(AABB aabb) {
        return this.minX <= aabb.maxX &&
                this.maxX >= aabb.minX &&
                this.minY <= aabb.maxY &&
                this.maxY >= aabb.minY;
    }

    /**
     * 按给定坐标平移
     *
     * @param x x轴平移距离
     * @param y y轴平移距离
     */
    public void translate(double x, double y) {
        this.minX += x;
        this.minY += y;
        this.maxX += x;
        this.maxY += y;
    }

    public void translate(Vector2 vector2) {
        this.translate(vector2.x, vector2.y);
    }
}
