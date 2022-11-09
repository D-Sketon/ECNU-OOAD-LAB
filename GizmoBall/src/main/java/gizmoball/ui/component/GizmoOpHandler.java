package gizmoball.ui.component;

import gizmoball.engine.geometry.AABB;
import gizmoball.engine.geometry.Epsilon;
import gizmoball.engine.geometry.Transform;
import gizmoball.engine.geometry.Vector2;
import gizmoball.engine.geometry.shape.AbstractShape;
import gizmoball.engine.physics.Mass;
import gizmoball.engine.physics.PhysicsBody;
import gizmoball.engine.world.entity.Ball;
import gizmoball.engine.world.entity.Flipper;
import gizmoball.ui.GeometryUtil;
import gizmoball.ui.GridWorld;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.HashMap;
import java.util.function.Function;

@Slf4j
public class GizmoOpHandler {

    private GridWorld world;

    private final HashMap<GizmoCommand, Function<PhysicsBody, Boolean>> gizmoOps;

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

    public boolean handleCommand(GizmoCommand command, PhysicsBody body) {
        if(command == null){
            throw new IllegalArgumentException("Command cannot be null");
        }
        if(body == null){
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
    public boolean addGizmo(PhysicsBody gizmoBody) {
        world.addBodyToGrid(gizmoBody);
        return true;
    }

    public boolean removeGizmo(PhysicsBody gizmoBody) {
        if(!world.getBodies().contains(gizmoBody)){
            throw new IllegalArgumentException("物件不存在");
        }
        world.removeBodies(gizmoBody);
        AABB aabb = gizmoBody.getShape().createAABB();
        world.setGrid(aabb, null);
        return true;
    }

    public boolean moveUp(PhysicsBody gizmoBody) {
        return moveGizmo(gizmoBody, new Vector2(0, world.getGridSize()));
    }

    public boolean moveDown(PhysicsBody gizmoBody) {
        return moveGizmo(gizmoBody, new Vector2(0, -world.getGridSize()));
    }

    public boolean moveLeft(PhysicsBody gizmoBody) {
        return moveGizmo(gizmoBody, new Vector2(-world.getGridSize(), 0));
    }

    public boolean moveRight(PhysicsBody gizmoBody) {
        return moveGizmo(gizmoBody, new Vector2(world.getGridSize(), 0));
    }

    public boolean moveGizmo(PhysicsBody gizmoBody, Vector2 position) {
        AABB originAABB = gizmoBody.getShape().createAABB();
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

    public boolean rotateRight(PhysicsBody gizmoBody) {
        return rotateGizmo(gizmoBody, -Math.PI / 2);
    }

    public boolean rotateLeft(PhysicsBody gizmoBody) {
        return rotateGizmo(gizmoBody, Math.PI / 2);
    }

    public boolean rotateGizmo(PhysicsBody gizmoBody, double theta) {
        AABB aabb = gizmoBody.getShape().createAABB();
        Vector2 center = new Vector2((aabb.maxX + aabb.minX) / 2, (aabb.maxY + aabb.minY) / 2);
        gizmoBody.getShape().rotate(theta, center.x, center.y);
        return true;
    }

    /**
     * 将不足格子的AABB填充到一个格子的大小
     */
    private void padAABBToGrid(AABB aabb){
        double width = aabb.maxX - aabb.minX;
        double height = aabb.maxY - aabb.minY;
        int gridSize = world.getGridSize();
        double modw = width % gridSize;
        double modh = height % gridSize;
        if(modw > Epsilon.E){
            aabb.maxX += (gridSize - modw) / 2;
            aabb.minX -= (gridSize - modw) / 2;
        }
        if(modh > Epsilon.E){
            aabb.maxY += (gridSize - modh) / 2;
            aabb.minY -= (gridSize - modh) / 2;
        }
    }

    public boolean zoomInGizmo(PhysicsBody gizmoBody) {
        // 固定左下角点，往左下角缩小
        AbstractShape shape = gizmoBody.getShape();
        AABB originAABB = shape.createAABB();
        int gridSize = world.getGridSize();
        int rate = shape.getRate();
        if (rate == 1) {
            throw new IllegalArgumentException("物件已经最小");
        }

        shape.zoom(rate - 1);
        AABB translatedAABB = shape.createAABB();
        padAABBToGrid(translatedAABB);
        Vector2 offset = GeometryUtil.snapToGrid(translatedAABB, gridSize, gridSize);
        // 往左下角缩小
        if (offset.x > 0) {
            offset.x -= gridSize;
        }
        if (offset.y > 0) {
            offset.y -= gridSize;
        }

        translatedAABB.translate(offset);
        if (world.checkOverlay(translatedAABB, gizmoBody)) {
            throw new Error("Not reachable");
        }

        world.setGrid(originAABB, null);
        shape.translate(offset);
        world.setGrid(translatedAABB, gizmoBody);
        //修改质量
        if(gizmoBody.getShape() instanceof Ball){
            gizmoBody.setMass(gizmoBody.getShape().createMass(10));
        }

        return true;
    }

    public boolean zoomOutGizmo(PhysicsBody gizmoBody) {
        // 固定左下角点，往右上角放大，如果越界或者重叠，就不缩放
        AbstractShape shape = gizmoBody.getShape();
        AABB originAABB = shape.createAABB();
        int gridSize = world.getGridSize();
        int originRate = shape.getRate();

        // try zoom
        shape.zoom(originRate + 1);
        AABB translatedAABB = shape.createAABB();
        // 除了挡板其他都是一个格子大小，理应不需要pad
        padAABBToGrid(translatedAABB);
        Vector2 offset = GeometryUtil.snapToGrid(translatedAABB, gridSize, gridSize);
        // 往右上角缩放
        if (offset.x < 0) {
            offset.x += gridSize;
        }
        if (offset.y < 0) {
            offset.y += gridSize;
        }
        translatedAABB.translate(offset);
        if(world.checkOverlay(translatedAABB, gizmoBody)){
            // 如果重叠，改回原来的大小
            shape.zoom(originRate);
            throw new IllegalArgumentException("物件重叠");
        }

        // 先将原本的位置设为null
        world.setGrid(originAABB, null);
        shape.translate(offset);
        // 修改质量
        if(gizmoBody.getShape() instanceof Ball){
            gizmoBody.setMass(gizmoBody.getShape().createMass(10));
        }
        world.setGrid(translatedAABB, gizmoBody);
        return true;
    }
}
