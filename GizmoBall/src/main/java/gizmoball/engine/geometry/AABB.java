package gizmoball.engine.geometry;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;

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
}
