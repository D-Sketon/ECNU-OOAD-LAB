package gizmoball.engine.world.listener;

import gizmoball.engine.collision.manifold.Manifold;
import gizmoball.engine.physics.PhysicsBody;
import javafx.util.Pair;

import java.util.List;

/**
 * 游戏每一个tick触发一次
 */
public interface TickListener {

    List<Pair<Manifold, Pair<PhysicsBody, PhysicsBody>>> tick();
}
