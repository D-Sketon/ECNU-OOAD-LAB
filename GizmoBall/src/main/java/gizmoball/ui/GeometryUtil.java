package gizmoball.ui;

import gizmoball.engine.geometry.AABB;
import gizmoball.engine.geometry.Vector2;

public class GeometryUtil {

    /**
     * 计算AABB到边界的偏移，返回能将AABB移动到边界内的最小向量
     *
     * @param aabb     目标aabb
     * @param boundary 边界
     * @return 返回能将AABB移动到边界内的最小向量
     */
    public static Vector2 offsetToBoundary(AABB aabb, AABB boundary) {
        double offsetX = 0;
        double offsetY = 0;
        if (aabb.minX < boundary.minX) {
            offsetX = -aabb.minX;
        } else if (aabb.maxX > boundary.maxX) {
            offsetX = boundary.maxX - aabb.maxX;
        }
        if (aabb.minY < boundary.minY) {
            offsetY = -aabb.minY;
        } else if (aabb.maxY > boundary.maxY) {
            offsetY = boundary.maxY - aabb.maxY;
        }
        return new Vector2(offsetX, offsetY);
    }

    /**
     * 计算AABB对齐到网格的偏移，返回能将AABB对齐到网格的最小向量
     *
     * @param aabb       目标aabb
     * @param gridWidth  网格宽度
     * @param gridHeight 网格高度
     * @return 到对其网格需要应用的偏移
     */
    public static Vector2 snapToGrid(AABB aabb, int gridWidth, int gridHeight) {
        // 以左下角对齐
        return snapToGrid(new Vector2(aabb.minX, aabb.minY), gridWidth, gridHeight);
    }

    /**
     * 计算点对齐到网格的偏移，返回能将点对齐到网格的最小向量
     *
     * @param vector     点
     * @param gridWidth  网格宽度
     * @param gridHeight 网格高度
     * @return 到对其网格需要应用的偏移
     */
    public static Vector2 snapToGrid(Vector2 vector, int gridWidth, int gridHeight) {
        double x = vector.x;
        double y = vector.y;
        double offsetX = x % gridWidth;
        double offsetY = y % gridHeight;
        offsetX = offsetX > gridWidth / 2.0 ? gridWidth - offsetX : -offsetX;
        offsetY = offsetY > gridHeight / 2.0 ? gridHeight - offsetY : -offsetY;
        return new Vector2(offsetX, offsetY);
    }

    /**
     * 将AABB补成一个正方形
     *
     * @param aabb /
     */
    public static void padToSquare(AABB aabb) {
        double width = aabb.maxX - aabb.minX;
        double height = aabb.maxY - aabb.minY;
        double delta = Math.abs(width - height);
        if (width > height) {
            aabb.minY -= delta / 2.0;
            aabb.maxY += delta / 2.0;
        } else {
            aabb.minX -= delta / 2.0;
            aabb.maxX += delta / 2.0;
        }
    }
}
