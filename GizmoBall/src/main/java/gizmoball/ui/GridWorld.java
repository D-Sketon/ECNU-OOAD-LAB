package gizmoball.ui;

import com.fasterxml.jackson.core.JsonProcessingException;
import gizmoball.engine.geometry.AABB;
import gizmoball.engine.geometry.Vector2;
import gizmoball.engine.geometry.shape.Rectangle;
import gizmoball.engine.physics.Mass;
import gizmoball.engine.physics.PhysicsBody;
import gizmoball.engine.world.World;
import gizmoball.ui.file.PersistentUtil;
import gizmoball.engine.world.filter.CollisionFilter;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.math3.util.Precision;

import java.io.File;
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
        int width = (int) Math.ceil((aabb.getMaxX() - aabb.getMinX()) / gridSize);
        int height = (int) Math.ceil((aabb.getMaxY() - aabb.getMinY()) / gridSize);

        for (int i = bottomLeft[0]; i < bottomLeft[0] + width; i++) {
            for (int j = bottomLeft[1]; j < bottomLeft[1] + height; j++) {
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
    public boolean checkOverlay(AABB aabb, PhysicsBody body) {
        int[] bottomLeft = getGridIndex(aabb.getMinX(), aabb.getMinY());
        if (bottomLeft == null) {
            return true;
        }
        int[] topRight = getGridIndex(aabb.getMaxX(), aabb.getMaxY());
        if (topRight == null) {
            return true;
        }
        int width = (int) Math.ceil((aabb.getMaxX() - aabb.getMinX()) / gridSize);
        int height = (int) Math.ceil((aabb.getMaxY() - aabb.getMinY()) / gridSize);

        for (int i = bottomLeft[0]; i < bottomLeft[0] + width; i++) {
            for (int j = bottomLeft[1]; j < bottomLeft[1] + height; j++) {
                if (gizmoGridBodies[i][j] != null && body != gizmoGridBodies[i][j]) {
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
        bottomBorder.setRestitution(0.95);
        bottomBorder.setFriction(0.5);
        addBodies(bottomBorder);

        Rectangle topRectangle = new Rectangle(worldWidth / 2, worldHeight / 2);
        topRectangle.getTransform().setX(topRectangle.getHalfWidth());
        topRectangle.getTransform().setY(worldHeight + topRectangle.getHalfHeight());
        PhysicsBody topBorder = new PhysicsBody(topRectangle);
        topBorder.setMass(new Mass(new Vector2(), 0.0, 0.0));
        topBorder.setRestitution(0.95);
        topBorder.setFriction(0.5);
        addBodies(topBorder);

        Rectangle leftRectangle = new Rectangle(worldWidth / 2, worldHeight / 2);
        leftRectangle.getTransform().setX(-leftRectangle.getHalfWidth());
        leftRectangle.getTransform().setY(leftRectangle.getHalfHeight());
        PhysicsBody leftBorder = new PhysicsBody(leftRectangle);
        leftBorder.setMass(new Mass(new Vector2(), 0.0, 0.0));
        leftBorder.setRestitution(0.95);
        leftBorder.setFriction(0.5);
        addBodies(leftBorder);

        Rectangle rightRectangle = new Rectangle(worldWidth / 2, worldHeight / 2);
        rightRectangle.getTransform().setX(worldWidth + rightRectangle.getHalfWidth());
        rightRectangle.getTransform().setY(rightRectangle.getHalfHeight());
        PhysicsBody rightBorder = new PhysicsBody(rightRectangle);
        rightBorder.setMass(new Mass(new Vector2(), 0.0, 0.0));
        rightBorder.setRestitution(0.95);
        rightBorder.setFriction(0.5);
        addBodies(rightBorder);
    }

    public void addBodyToGrid(PhysicsBody body) throws IllegalArgumentException {
        AABB aabb = body.getShape().createAABB();
        if (checkOverlay(aabb, body)) {
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

    /**
     * <p>获取当前世界的物体快照到默认文件".snapshot.json"</p>
     *
     * @return json格式的字符串表示每一个物体
     */
    public String snapshot() {
        return snapshot(new File(".snapshot.json"));
    }

    /**
     * <p>获取当前世界的物体快照到指定文件</p>
     *
     * @param file 指定文件
     * @return json格式的字符串表示每一个物体
     */
    public String snapshot(File file) {
        try {
            log.info("保存快照中...");
            List<PhysicsBody> bodies = new ArrayList<>();
            final int borderCount = 4;
            bodies.addAll(obstacles.stream().skip(borderCount) // 跳过边界
                    .collect(Collectors.toList()));
            bodies.addAll(balls);
            bodies.addAll(blackholes);
            bodies.addAll(pipes);
            bodies.addAll(flippers);

            snapshot = PersistentUtil.toJsonString(bodies);
            log.debug("take snapshot: {}", snapshot);

            PersistentUtil.write(snapshot, file);
            log.info("已保存{}个物体的快照", bodies.size());
        } catch (Exception e) {
            log.error("snapshot error", e);
        }
        return snapshot;
    }

    /**
     * @see #restore(String)
     */
    public void restore() throws RuntimeException {
        restore(snapshot);
    }

    /**
     * <p>恢复世界的物体</p>
     *
     * @param snapshot snapshot获取的json字符串
     */
    public void restore(String snapshot) throws RuntimeException {
        try {
            log.info("恢复快照中...");
            List<PhysicsBody> o = PersistentUtil.fromJsonString(snapshot);
            this.obstacles.clear();
            this.balls.clear();
            this.blackholes.clear();
            this.pipes.clear();
            this.flippers.clear();
            //其他四个列表也要清空
            for (PhysicsBody[] gizmoGridBody : this.gizmoGridBodies) {
                Arrays.fill(gizmoGridBody, null);
            }
            initBoundary();
            setBodiesToGrid(o);
            log.info("成功加载{}个物体", o.size());
        } catch (IOException e) {
            log.error("restore error", e);
            throw new RuntimeException(e);

        }
    }

    public void restore(File file) throws RuntimeException {
        try {
            restore(PersistentUtil.readFromFile(file));
        } catch (IOException e) {
            log.error("restore error", e);
            throw new RuntimeException(e);
        }
    }
}
