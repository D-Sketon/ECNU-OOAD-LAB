package gizmoball.ui;

import com.fasterxml.jackson.core.JsonProcessingException;
import gizmoball.engine.geometry.AABB;
import gizmoball.engine.geometry.Vector2;
import gizmoball.engine.geometry.shape.Rectangle;
import gizmoball.engine.physics.Mass;
import gizmoball.engine.physics.PhysicsBody;
import gizmoball.engine.world.World;
import gizmoball.ui.file.PersistentUtil;
import gizmoball.engine.world.listener.CollisionListener;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.math3.util.Precision;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.ArrayList;

@Getter
@Slf4j
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
        this(gravity, 600, 600, new ArrayList<>());
    }

    public GridWorld(Vector2 gravity, int width, int height, List<CollisionListener> listeners) {
        this(gravity, width, height, 30, listeners);
    }

    public GridWorld(Vector2 gravity, int width, int height, int gridSize, List<CollisionListener> listeners) {
        super(gravity, listeners);
        this.gridSize = gridSize;
        boundaryAABB = new AABB(0, 0, width, height);
        gizmoGridBodies = new PhysicsBody[(int) (width / gridSize)][(int) (height / gridSize)];
        initBoundary();
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
        x = Precision.round(x, 10);
        y = Precision.round(y, 10);
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
        if (bottomLeft == null) {
            return true;
        }
        int[] topRight = getGridIndex(aabb.getMaxX(), aabb.getMaxY());
        if (topRight == null) {
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

    protected void initBoundary() {
        double worldWidth = boundaryAABB.maxX;
        double worldHeight = boundaryAABB.maxY;
        // init border
        Rectangle bottomRectangle = new Rectangle(worldWidth / 2, worldHeight / 2);
        bottomRectangle.getTransform().setX(bottomRectangle.getHalfWidth());
        bottomRectangle.getTransform().setY(-bottomRectangle.getHalfHeight());
        PhysicsBody bottomBorder = new PhysicsBody(bottomRectangle);
        bottomBorder.setMass(new Mass(new Vector2(), 0.0, 0.0));
        addBodies(bottomBorder);

        Rectangle topRectangle = new Rectangle(worldWidth / 2, worldHeight / 2);
        topRectangle.getTransform().setX(topRectangle.getHalfWidth());
        topRectangle.getTransform().setY(worldHeight + topRectangle.getHalfHeight());
        PhysicsBody topBorder = new PhysicsBody(topRectangle);
        topBorder.setMass(new Mass(new Vector2(), 0.0, 0.0));
        addBodies(topBorder);

        Rectangle leftRectangle = new Rectangle(worldWidth / 2, worldHeight / 2);
        leftRectangle.getTransform().setX(-leftRectangle.getHalfWidth());
        leftRectangle.getTransform().setY(leftRectangle.getHalfHeight());
        PhysicsBody leftBorder = new PhysicsBody(leftRectangle);
        leftBorder.setMass(new Mass(new Vector2(), 0.0, 0.0));
        addBodies(leftBorder);

        Rectangle rightRectangle = new Rectangle(worldWidth / 2, worldHeight / 2);
        rightRectangle.getTransform().setX(worldWidth + rightRectangle.getHalfWidth());
        rightRectangle.getTransform().setY(rightRectangle.getHalfHeight());
        PhysicsBody rightBorder = new PhysicsBody(rightRectangle);
        rightBorder.setMass(new Mass(new Vector2(), 0.0, 0.0));
        addBodies(rightBorder);
    }

    public void addBodyToGrid(PhysicsBody body) throws IllegalArgumentException {
        AABB aabb = body.getShape().createAABB();
        if (checkOverlay(aabb)) {
            throw new IllegalArgumentException("物件重叠");
        }
        super.addBodies(body);
        setGrid(aabb, body);
    }

    public void setBodiesToGrid(List<PhysicsBody> bodies) {
        for (PhysicsBody body : bodies) {
            addBodyToGrid(body);
        }
    }

    private String snapshot;

    public String snapshot() {
        try {
            snapshot = PersistentUtil.toJsonString(bodies.stream().skip(4).collect(Collectors.toList()));
            PersistentUtil.write(snapshot, "snapshot.json");
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return snapshot;
    }

    public void restore(){
       restore(snapshot);
    }

    public void restore(String snapshot){
        try {
            List<PhysicsBody> o = PersistentUtil.fromJsonString(snapshot);
            this.bodies.clear();
            for (PhysicsBody[] gizmoGridBody : this.gizmoGridBodies) {
                Arrays.fill(gizmoGridBody, null);
            }
            initBoundary();
            setBodiesToGrid(o);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
