package gizmoball.ui;

import gizmoball.engine.geometry.AABB;
import gizmoball.engine.geometry.Vector2;
import gizmoball.engine.geometry.shape.Rectangle;
import gizmoball.engine.physics.Mass;
import gizmoball.engine.physics.PhysicsBody;
import gizmoball.game.GizmoWorld;
import gizmoball.ui.component.GizmoType;
import gizmoball.ui.file.PersistentUtil;
import gizmoball.ui.visualize.GizmoPhysicsBody;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.math3.util.Precision;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static gizmoball.game.GizmoSettings.BOUNDARY_BUFFER;


@Getter
@Slf4j
public class GridWorld extends GizmoWorld {


    /**
     * 边界AABB
     */
    protected AABB boundaryAABB;

    /**
     * 每个网格中对应的PhysicsBody，一个body（包括三角，圆等）视为占满完整的一格
     */
    protected GizmoPhysicsBody[][] gizmoGridBodies;

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
        gizmoGridBodies = new GizmoPhysicsBody[(int) (width / gridSize)][(int) (height / gridSize)];
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

    public void setGrid(AABB aabb, GizmoPhysicsBody body) {
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

    public void initBoundary() {
        double worldWidth = boundaryAABB.maxX;
        double worldHeight = boundaryAABB.maxY;
        // init border
        // bottom
        createBoundary(worldWidth / 2 + BOUNDARY_BUFFER, worldHeight / 2, worldWidth / 2, -worldHeight / 2);
        // top
        createBoundary(worldWidth / 2 + BOUNDARY_BUFFER, worldHeight / 2, worldWidth / 2 + BOUNDARY_BUFFER, worldHeight + worldHeight / 2);
        // left
        createBoundary(worldWidth / 2, worldHeight / 2, -worldWidth / 2, worldHeight / 2);
        // right
        createBoundary(worldWidth / 2, worldHeight / 2, worldWidth + worldWidth / 2, worldHeight / 2);
    }

    private void createBoundary(double halfWidth, double halfHeight, double x, double y) {
        Rectangle rectangle = new Rectangle(halfWidth, halfHeight);
        rectangle.getTransform().setX(x);
        rectangle.getTransform().setY(y);
        GizmoPhysicsBody border = new GizmoPhysicsBody(rectangle, GizmoType.BOUNDARY);
        border.setMass(new Mass(new Vector2(), 0.0, 0.0));
        border.setRestitution(0.95);
        border.setFriction(0.5);
        // 不放入格子
        super.addBody(border);
    }


    public void addBodyToGrid(PhysicsBody body) {
        if (body instanceof GizmoPhysicsBody) {
            this.addBody((GizmoPhysicsBody) body);
        }
    }

    @Override
    public void addBody(GizmoPhysicsBody body) {
        AABB aabb = body.getShape().createAABB();
        GeometryUtil.padToSquare(aabb);
        if (checkOverlay(aabb, body)) {
            throw new IllegalArgumentException("物件重叠");
        }

        super.addBody(body);
        setGrid(aabb, body);
    }

    @Override
    public void removeBody(GizmoPhysicsBody body) {
        super.removeBody(body);
    }

    @Override
    public void removeAllBodies() {
        super.removeAllBodies();

        // 从格子中移除
        for (GizmoPhysicsBody[] gizmoGridBody : this.gizmoGridBodies) {
            Arrays.fill(gizmoGridBody, null);
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
            List<PhysicsBody> bodiesToJson = new ArrayList<>();
            bodyTypeMap.forEach((k, v) -> {
                if (k != GizmoType.BOUNDARY) {
                    bodiesToJson.addAll(v);
                }
            });

            snapshot = PersistentUtil.toJsonString(bodiesToJson);
            log.debug("take snapshot: {}", snapshot);

            PersistentUtil.write(snapshot, file);
            log.info("已保存{}个物体的快照", bodiesToJson.size());
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
            removeAllBodies();
            initBoundary();
            o.forEach(this::addBodyToGrid);

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
