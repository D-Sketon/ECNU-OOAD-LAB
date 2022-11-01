package gizmoball.ui;

import gizmoball.engine.geometry.AABB;
import gizmoball.engine.geometry.Vector2;
import gizmoball.engine.physics.PhysicsBody;
import gizmoball.engine.world.World;
import lombok.Getter;
import org.apache.commons.math3.util.MathUtils;
import org.apache.commons.math3.util.Precision;

import java.math.BigDecimal;
import java.util.DoubleSummaryStatistics;

@Getter
public class GridWorld extends World {


    /**
     * 边界AABB
     */
    protected AABB boundaryAABB;

    /**
     * 每个网格中对应的PhysicsBody，一个body（包括三角，圆等）视为占满完整的一格
     */
    protected PhysicsBody[][] gizmoGridBodies;

    protected int gridSize;

    public GridWorld(Vector2 gravity) {
        this(gravity, 600, 600);
    }

    public GridWorld(Vector2 gravity, int width, int height) {
        this(gravity, width, height, 30);
    }

    public GridWorld(Vector2 gravity, int width, int height, int gridSize) {
        super(gravity);
        this.gridSize = gridSize;
        boundaryAABB = new AABB(0, 0, width, height);
        gizmoGridBodies = new PhysicsBody[(int) (width / gridSize)][(int) (height / gridSize)];
    }

    /**
     * <p>以右手系坐标，获取某个点对应的格子的下标</p>
     * <p>x : [0, GRID_SIZE)对应下标0</p>
     *
     * @param x 右手系坐标x
     * @param y 右手系坐标y
     * @return 如果超出格子范围，返回null，否则返回格子下标长度为2的数组[i, j]，对应gizmoGridBodies[i][j]
     */
    public int[] getGridIndex(double x, double y) {
        x = Precision.round(x,10);
        y = Precision.round(y,10);
        if (x < boundaryAABB.minX || x > boundaryAABB.maxX
                || y < boundaryAABB.minY || y > boundaryAABB.maxY) {
            return null;
        }
        int[] index = new int[2];
        index[0] = (int) (x / gridSize);
        index[1] = (int) (y / gridSize);
        return index;
    }

    /**
     * @see #getGridIndex(double, double)
     */
    private int[] getGridIndex(Vector2 position) {
        return getGridIndex(position.x, position.y);
    }

    public void setGrid(AABB aabb, PhysicsBody body) {
        int[] bottomLeft = getGridIndex(aabb.getMinX(), aabb.getMinY());
        int[] topRight = getGridIndex(aabb.getMaxX(), aabb.getMaxY());
        for (int i = bottomLeft[0]; i < topRight[0]; i++) {
            for (int j = bottomLeft[1]; j < topRight[1]; j++) {
                gizmoGridBodies[i][j] = body;
            }
        }
    }

    /**
     * <p>检查AABB所在范围的格子是否已经有物体</p>
     * <p>同时会检查是否越界</p>
     *
     * @param aabb /
     * @return /
     */
    public boolean checkOverlay(AABB aabb) {
        int[] bottomLeft = getGridIndex(aabb.getMinX(), aabb.getMinY());
        if(bottomLeft == null) {
            return true;
        }
        int[] topRight = getGridIndex(aabb.getMaxX(), aabb.getMaxY());
        if(topRight == null) {
            return true;
        }
        for (int i = bottomLeft[0]; i < topRight[0]; i++) {
            for (int j = bottomLeft[1]; j < topRight[1]; j++) {
                if (gizmoGridBodies[i][j] != null) {
                    return true;
                }
            }
        }
        return false;
    }


}
