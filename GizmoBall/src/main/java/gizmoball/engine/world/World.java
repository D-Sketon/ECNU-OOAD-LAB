package gizmoball.engine.world;

import gizmoball.engine.collision.contact.ContactConstraint;
import gizmoball.engine.collision.contact.SequentialImpulses;
import gizmoball.engine.collision.detector.BasicCollisionDetector;
import gizmoball.engine.collision.detector.CollisionDetector;
import gizmoball.engine.collision.manifold.Manifold;
import gizmoball.engine.geometry.Vector2;
import gizmoball.engine.physics.PhysicsBody;
import gizmoball.engine.world.entity.*;
import gizmoball.engine.world.listener.*;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 对整个弹球游戏物体所处世界的抽象
 */
public class World {

    /**
     * 地球重力
     */
    public static final Vector2 EARTH_GRAVITY = new Vector2(0, -9.8);

    protected Vector2 gravity;

    /**
     * 所有的球
     */
    protected final List<PhysicsBody> balls;

    /**
     * 所有的黑洞
     */
    protected final List<PhysicsBody> blackHoles;

    /**
     * 其他图形，圆，正方形，三角形
     */
    protected final List<PhysicsBody> obstacles;

    /**
     * 所有的管道
     */
    protected final List<PhysicsBody> pipes;

    /**
     * 两块挡板
     */
    protected final List<PhysicsBody> flippers;

    protected PhysicsBody leftFlipper;

    protected PhysicsBody rightFlipper;

    protected final List<TickListener> tickListeners;

    protected final CollisionDetector collisionDetector;

    protected final SequentialImpulses solver;


    public World(Vector2 gravity) {
        this.gravity = gravity;
        this.balls = new ArrayList<>();
        this.blackHoles = new ArrayList<>();
        this.obstacles = new ArrayList<>();
        this.pipes = new ArrayList<>();
        this.flippers = new ArrayList<>();

        this.tickListeners = new ArrayList<>();
        this.collisionDetector = new BasicCollisionDetector();
        this.solver = new SequentialImpulses();

        BallListener ballListener = new BallListener(balls);
        BlackHoleListener blackholeListener = new BlackHoleListener(balls, blackHoles);
        PipeListener pipeListener = new PipeListener(balls, pipes, gravity);
        ObstacleListener obstacleListener = new ObstacleListener(balls, obstacles);
        FlipperListener flipperListener = new FlipperListener(balls, flippers);
        tickListeners.add(ballListener);
        tickListeners.add(blackholeListener);
        tickListeners.add(pipeListener);
        tickListeners.add(obstacleListener);
        tickListeners.add(flipperListener);
    }

    public void addBodies(PhysicsBody body) {
        if (body.getShape() instanceof Flipper) {
            Flipper shape = (Flipper) body.getShape();
            if (shape.getDirection() == Flipper.Direction.LEFT) {
                leftFlipper = body;
            } else {
                rightFlipper = body;
            }
            flippers.add(body);
        } else if (body.getShape() instanceof Ball) {
            this.balls.add(body);
        } else if (body.getShape() instanceof BlackHole) {
            this.blackHoles.add(body);
        } else if (body.getShape() instanceof Pipe || body.getShape() instanceof CurvedPipe) {
            this.pipes.add(body);
        } else {
            this.obstacles.add(body);
        }

    }

    public void removeBodies(PhysicsBody... bodies) {
        this.blackHoles.removeAll(Arrays.asList(bodies));
        this.pipes.removeAll(Arrays.asList(bodies));
        this.balls.removeAll(Arrays.asList(bodies));
        this.obstacles.removeAll(Arrays.asList(bodies));
        this.flippers.removeAll(Arrays.asList(bodies));
    }

    public List<PhysicsBody> getBodies() {
        List<PhysicsBody> bodies = new ArrayList<>();
        bodies.addAll(obstacles);
        bodies.addAll(balls);
        bodies.addAll(blackHoles);
        bodies.addAll(pipes);
        bodies.addAll(flippers);
        return bodies;
    }

    public void flipperUp(Flipper.Direction direction) {
        if (direction == Flipper.Direction.LEFT && leftFlipper != null) {
            Flipper flipper = (Flipper) leftFlipper.getShape();
            flipper.rise();
        } else if (direction == Flipper.Direction.RIGHT && rightFlipper != null) {
            Flipper flipper = (Flipper) rightFlipper.getShape();
            flipper.rise();
        }
    }

    public void flipperDown(Flipper.Direction direction) {
        if (direction == Flipper.Direction.LEFT && leftFlipper != null) {
            Flipper flipper = (Flipper) leftFlipper.getShape();
            flipper.down();
        } else if (direction == Flipper.Direction.RIGHT && rightFlipper != null) {
            Flipper flipper = (Flipper) rightFlipper.getShape();
            flipper.down();
        }
    }

    /**
     * 游戏更新以tick为单位，每个tick更新一次
     */
    public void tick() {
        List<Pair<Manifold, Pair<PhysicsBody, PhysicsBody>>> pairs = new ArrayList<>();
        // 碰撞检测，返回碰撞检测
        for (TickListener listener : tickListeners) {
            List<Pair<Manifold, Pair<PhysicsBody, PhysicsBody>>> pair = listener.tick();
            pairs.addAll(pair);
        }
        List<ContactConstraint> contactConstraints = collisionDetector.preLocalSolve(pairs);
        collisionDetector.LocalSolve(solver, gravity, contactConstraints, balls);
    }


}
