package gizmoball.game;

import gizmoball.engine.AbstractWorld;
import gizmoball.engine.collision.contact.ContactConstraint;
import gizmoball.engine.collision.manifold.Manifold;
import gizmoball.engine.geometry.Vector2;
import gizmoball.engine.physics.PhysicsBody;
import gizmoball.game.entity.*;
import gizmoball.game.listener.*;
import gizmoball.ui.component.GizmoType;
import gizmoball.ui.visualize.GizmoPhysicsBody;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GizmoWorld extends AbstractWorld<GizmoPhysicsBody> {

    private final List<TickListener> tickListeners;

    protected final Map<GizmoType, List<PhysicsBody>> bodyTypeMap;

    public GizmoWorld(Vector2 gravity) {
        super(gravity);

        bodyTypeMap = new HashMap<>();

        List<PhysicsBody> balls = new ArrayList<>();
        bodyTypeMap.put(GizmoType.BALL, balls);
        tickListeners = new ArrayList<>();

        tickListeners.add(new BallListener(balls));
        tickListeners.add(new BlackHoleListener(balls, bodyTypeMap.computeIfAbsent(GizmoType.BLACK_HOLE, k -> new ArrayList<>()), bodies));
        tickListeners.add(new PipeListener(balls, bodyTypeMap.computeIfAbsent(GizmoType.PIPE, k -> new ArrayList<>()), gravity));
        tickListeners.add(new PipeListener(balls, bodyTypeMap.computeIfAbsent(GizmoType.CURVED_PIPE, k -> new ArrayList<>()), gravity));
        tickListeners.add(new ObstacleListener(balls, bodyTypeMap.computeIfAbsent(GizmoType.BOUNDARY, k -> new ArrayList<>())));
        tickListeners.add(new ObstacleListener(balls, bodyTypeMap.computeIfAbsent(GizmoType.CIRCLE, k -> new ArrayList<>())));
        tickListeners.add(new ObstacleListener(balls, bodyTypeMap.computeIfAbsent(GizmoType.TRIANGLE, k -> new ArrayList<>())));
        tickListeners.add(new ObstacleListener(balls, bodyTypeMap.computeIfAbsent(GizmoType.RECTANGLE, k -> new ArrayList<>())));
        tickListeners.add(new FlipperListener(balls, bodyTypeMap.computeIfAbsent(GizmoType.LEFT_FLIPPER, k -> new ArrayList<>())));
        tickListeners.add(new FlipperListener(balls, bodyTypeMap.computeIfAbsent(GizmoType.RIGHT_FLIPPER, k -> new ArrayList<>())));
    }

    public void addBody(GizmoPhysicsBody body) {
        super.addBody(body);

        GizmoType gizmoType = body.getGizmoType();
        bodyTypeMap.computeIfAbsent(gizmoType, k -> new ArrayList<>()).add(body);
    }

    public void removeBodies(GizmoPhysicsBody body) {
        super.removeBodies(body);

        GizmoType type = body.getGizmoType();
        List<PhysicsBody> list = bodyTypeMap.get(type);
        if(list != null) {
            list.remove(body);
        }
    }

    @Override
    public void removeAllBodies() {
        super.removeAllBodies();

        // 从类型Map中移除
        for (Map.Entry<GizmoType, List<PhysicsBody>> entry : bodyTypeMap.entrySet()) {
            entry.getValue().clear();
        }
    }

    public void flipperUp(Flipper.Direction direction) {
        if (direction == Flipper.Direction.LEFT) {
            for (PhysicsBody physicsBody : bodyTypeMap.get(GizmoType.LEFT_FLIPPER)) {
                Flipper flipper = (Flipper) physicsBody.getShape();
                flipper.rise();
            }
        } else if (direction == Flipper.Direction.RIGHT) {
            for (PhysicsBody physicsBody : bodyTypeMap.get(GizmoType.RIGHT_FLIPPER)) {
                Flipper flipper = (Flipper) physicsBody.getShape();
                flipper.rise();
            }
        }
    }

    public void flipperDown(Flipper.Direction direction) {
        if (direction == Flipper.Direction.LEFT) {
            for (PhysicsBody physicsBody : bodyTypeMap.get(GizmoType.LEFT_FLIPPER)) {
                Flipper flipper = (Flipper) physicsBody.getShape();
                flipper.down();
            }
        } else if (direction == Flipper.Direction.RIGHT) {
            for (PhysicsBody physicsBody : bodyTypeMap.get(GizmoType.RIGHT_FLIPPER)) {
                Flipper flipper = (Flipper) physicsBody.getShape();
                flipper.down();
            }
        }
    }


    @Override
    public void tick() {
        List<Pair<Manifold, Pair<PhysicsBody, PhysicsBody>>> pairs = new ArrayList<>();
        // 碰撞检测，返回碰撞检测
        for (TickListener listener : tickListeners) {
            List<Pair<Manifold, Pair<PhysicsBody, PhysicsBody>>> pair = listener.tick();
            pairs.addAll(pair);
        }
        List<ContactConstraint> contactConstraints = collisionDetector.preLocalSolve(pairs);
        collisionDetector.LocalSolve(solver, gravity, contactConstraints, bodyTypeMap.get(GizmoType.BALL));
    }
}
