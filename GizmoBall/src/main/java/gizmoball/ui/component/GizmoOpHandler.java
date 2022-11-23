package gizmoball.ui.component;

import gizmoball.engine.geometry.AABB;
import gizmoball.engine.geometry.Vector2;
import gizmoball.ui.GeometryUtil;
import gizmoball.ui.GridWorld;
import gizmoball.ui.visualize.GizmoPhysicsBody;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.function.Function;

@Slf4j
public class GizmoOpHandler {

    private final GridWorld world;

    private final HashMap<GizmoCommand, Function<GizmoPhysicsBody, Boolean>> gizmoOps;

    public GizmoOpHandler(GridWorld world) {
        this.world = world;
        gizmoOps = new HashMap<>();
        gizmoOps.put(GizmoCommand.ADD, this::addGizmo);
        gizmoOps.put(GizmoCommand.REMOVE, this::removeGizmo);
        gizmoOps.put(GizmoCommand.ROTATE_LEFT, this::rotateLeft);
        gizmoOps.put(GizmoCommand.ROTATE_RIGHT, this::rotateRight);
        gizmoOps.put(GizmoCommand.MOVE_UP, this::moveUp);
        gizmoOps.put(GizmoCommand.MOVE_DOWN, this::moveDown);
        gizmoOps.put(GizmoCommand.MOVE_LEFT, this::moveLeft);
        gizmoOps.put(GizmoCommand.MOVE_RIGHT, this::moveRight);
        gizmoOps.put(GizmoCommand.ZOOM_IN, this::zoomInGizmo);
        gizmoOps.put(GizmoCommand.ZOOM_OUT, this::zoomOutGizmo);
    }

    public boolean handleCommand(GizmoCommand command, GizmoPhysicsBody body) {
        if (command == null) {
            throw new IllegalArgumentException("Command cannot be null");
        }
        if (body == null) {
            throw new IllegalArgumentException("PhysicsBody cannot be null");
        }
        Function<?, ?> function = gizmoOps.get(command);
        if (function == null) {
            throw new IllegalArgumentException("No function found for command: " + command);
        }
        log.debug("Handling command: " + command);
        return gizmoOps.get(command).apply(body);
    }

    /**
     * <p>向游戏添加小物体</p>
     * <p>会同时处理网格对应的信息</p>
     *
     * @param gizmoBody /
     */
    public boolean addGizmo(GizmoPhysicsBody gizmoBody) {
        world.addBodyToGrid(gizmoBody);
        return true;
    }

    public boolean removeGizmo(GizmoPhysicsBody gizmoBody) {
        if (!world.getBodies().contains(gizmoBody)) {
            throw new IllegalArgumentException("物件不存在");
        }
        world.removeBody(gizmoBody);
        AABB aabb = gizmoBody.getShape().createAABB();
        GeometryUtil.padToSquare(aabb);
        world.setGrid(aabb, null);
        return true;
    }

    public boolean moveUp(GizmoPhysicsBody gizmoBody) {
        return moveGizmo(gizmoBody, new Vector2(0, world.getGridSize()));
    }

    public boolean moveDown(GizmoPhysicsBody gizmoBody) {
        return moveGizmo(gizmoBody, new Vector2(0, -world.getGridSize()));
    }

    public boolean moveLeft(GizmoPhysicsBody gizmoBody) {
        return moveGizmo(gizmoBody, new Vector2(-world.getGridSize(), 0));
    }

    public boolean moveRight(GizmoPhysicsBody gizmoBody) {
        return moveGizmo(gizmoBody, new Vector2(world.getGridSize(), 0));
    }

    public boolean moveGizmo(GizmoPhysicsBody gizmoBody, Vector2 position) {
        AABB originAABB = gizmoBody.getShape().createAABB();
        GeometryUtil.padToSquare(originAABB);
        AABB translatedAABB = new AABB(originAABB);
        translatedAABB.translate(position);

        if (world.checkOverlay(translatedAABB, gizmoBody)) {
            throw new IllegalArgumentException("物件重叠");
        }

        // 先将原本的位置设为null
        world.setGrid(originAABB, null);
        gizmoBody.getShape().translate(position);
        world.setGrid(translatedAABB, gizmoBody);
        return true;
    }

    public boolean rotateRight(GizmoPhysicsBody gizmoBody) {
        return rotateGizmo(gizmoBody, -Math.PI / 2);
    }

    public boolean rotateLeft(GizmoPhysicsBody gizmoBody) {
        return rotateGizmo(gizmoBody, Math.PI / 2);
    }

    public boolean rotateGizmo(GizmoPhysicsBody gizmoBody, double theta) {
        AABB aabb = gizmoBody.getShape().createAABB();
        Vector2 center = new Vector2((aabb.maxX + aabb.minX) / 2, (aabb.maxY + aabb.minY) / 2);
        gizmoBody.getShape().rotate(theta, center.x, center.y);
        return true;
    }

    public boolean zoomInGizmo(GizmoPhysicsBody gizmoBody) {
        // 固定左下角点，往左下角缩小
        int rate = gizmoBody.getShape().getRate();
        if (rate == 1) {
            throw new IllegalArgumentException("物件已经最小");
        }

        AABB aabb = gizmoBody.getShape().createAABB();
        GeometryUtil.padToSquare(aabb);
        world.setGrid(aabb, null);
        aabb.maxY -= world.getGridSize();
        aabb.maxX -= world.getGridSize();
        world.setGrid(aabb, gizmoBody);

        gizmoBody.getShape().zoom(rate - 1);
        gizmoBody.getShape().translate(-world.getGridSize() / 2.0, -world.getGridSize() / 2.0);
        return true;
    }

    public boolean zoomOutGizmo(GizmoPhysicsBody gizmoBody) {
        // 固定左下角点，往右上角放大，如果越界或者重叠，就不缩放
        AABB originAABB = gizmoBody.getShape().createAABB();
        AABB translatedAABB = new AABB(originAABB);
        translatedAABB.maxY += world.getGridSize();
        translatedAABB.maxX += world.getGridSize();
        GeometryUtil.padToSquare(translatedAABB);

        if (world.checkOverlay(translatedAABB, gizmoBody)) {
            throw new IllegalArgumentException("物件重叠");
        }

        int rate = gizmoBody.getShape().getRate();
        gizmoBody.getShape().zoom(rate + 1);
        gizmoBody.getShape().translate(world.getGridSize() / 2.0, world.getGridSize() / 2.0);
        world.setGrid(translatedAABB, gizmoBody);
        return true;
    }
}
