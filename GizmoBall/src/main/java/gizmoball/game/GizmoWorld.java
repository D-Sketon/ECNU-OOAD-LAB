package gizmoball.game;

import gizmoball.engine.AbstractWorld;
import gizmoball.engine.collision.contact.ContactConstraint;
import gizmoball.engine.collision.manifold.Manifold;
import gizmoball.engine.geometry.Vector2;
import gizmoball.engine.geometry.shape.AbstractShape;
import gizmoball.engine.physics.PhysicsBody;
import gizmoball.game.entity.*;
import gizmoball.game.listener.*;
import gizmoball.ui.component.GizmoType;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.List;

public class GizmoWorld extends AbstractWorld<GizmoType> {

    private final List<TickListener> tickListeners;

    public GizmoWorld(Vector2 gravity) {
        super(gravity);

        List<PhysicsBody> balls = new ArrayList<>();
        List<PhysicsBody> blackHoles = new ArrayList<>();
        List<PhysicsBody> obstacles = new ArrayList<>();
        List<PhysicsBody> pipes = new ArrayList<>();
        List<PhysicsBody> leftFlippers = new ArrayList<>();
        List<PhysicsBody> rightFlippers = new ArrayList<>();

        bodies.put(GizmoType.BALL, balls);
        bodies.put(GizmoType.OBSTACLE, obstacles);
        bodies.put(GizmoType.PIPE, pipes);
        bodies.put(GizmoType.BLACK_HOLE, blackHoles);
        bodies.put(GizmoType.LEFT_FLIPPER, leftFlippers);
        bodies.put(GizmoType.RIGHT_FLIPPER, rightFlippers);

        tickListeners = new ArrayList<>();
        BallListener ballListener = new BallListener(balls);
        BlackHoleListener blackholeListener = new BlackHoleListener(balls, blackHoles);
        PipeListener pipeListener = new PipeListener(balls, pipes, gravity);
        ObstacleListener obstacleListener = new ObstacleListener(balls, obstacles);
        FlipperListener leftFlipperListener = new FlipperListener(balls, leftFlippers);
        FlipperListener rightFlipperListener = new FlipperListener(balls, rightFlippers);
        tickListeners.add(ballListener);
        tickListeners.add(blackholeListener);
        tickListeners.add(pipeListener);
        tickListeners.add(obstacleListener);
        tickListeners.add(leftFlipperListener);
        tickListeners.add(rightFlipperListener);
    }

    public void addBodies(PhysicsBody body) {
        GizmoType gizmoType;
        AbstractShape shape = body.getShape();
        if (shape instanceof Flipper) {
            Flipper flipper = (Flipper) shape;
            if (flipper.getDirection() == Flipper.Direction.LEFT) {
                gizmoType = GizmoType.LEFT_FLIPPER;
            } else {
                gizmoType = GizmoType.RIGHT_FLIPPER;
            }
        } else if (body.getShape() instanceof Ball) {
            gizmoType = GizmoType.BALL;
        } else if (body.getShape() instanceof BlackHole) {
            gizmoType = GizmoType.BLACK_HOLE;
        } else if (body.getShape() instanceof Pipe || body.getShape() instanceof CurvedPipe) {
            gizmoType = GizmoType.PIPE;
        } else {
            gizmoType = GizmoType.OBSTACLE;
        }
        addBodies(body, gizmoType);
    }

    public void removeBodies(PhysicsBody body) {
        GizmoType[] values = GizmoType.values();
        for (GizmoType gizmoType : values) {
            removeBodies(body, gizmoType);
        }
    }

    public void flipperUp(Flipper.Direction direction) {
        if (direction == Flipper.Direction.LEFT) {
            for (PhysicsBody physicsBody : bodies.get(GizmoType.LEFT_FLIPPER)) {
                Flipper flipper = (Flipper) physicsBody.getShape();
                flipper.rise();
            }
        } else if (direction == Flipper.Direction.RIGHT) {
            for (PhysicsBody physicsBody : bodies.get(GizmoType.RIGHT_FLIPPER)) {
                Flipper flipper = (Flipper) physicsBody.getShape();
                flipper.rise();
            }
        }
    }

    public void flipperDown(Flipper.Direction direction) {
        if (direction == Flipper.Direction.LEFT) {
            for (PhysicsBody physicsBody : bodies.get(GizmoType.LEFT_FLIPPER)) {
                Flipper flipper = (Flipper) physicsBody.getShape();
                flipper.down();
            }
        } else if (direction == Flipper.Direction.RIGHT) {
            for (PhysicsBody physicsBody : bodies.get(GizmoType.RIGHT_FLIPPER)) {
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
        collisionDetector.LocalSolve(solver, gravity, contactConstraints, bodies.get(GizmoType.BALL));
    }
}
