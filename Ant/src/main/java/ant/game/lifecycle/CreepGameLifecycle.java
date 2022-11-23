package ant.game.lifecycle;

import ant.game.core.CreepingGame;
import ant.game.core.GameContext;
import ant.game.entity.Ant;
import ant.game.entity.GameObject;
import ant.game.entity.Rod;
import ant.game.physics.Collider;
import ant.game.physics.Vector2D;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class CreepGameLifecycle extends AbstractGameLifecycle {

    public CreepGameLifecycle(GameContext gameContext, CreepingGame creepingGame) {
        super(gameContext, creepingGame);
    }

    @Override
    public void onUpdate() {
        handleCreep();
        handleCollision();
    }

    /**
     * 处理蚂蚁之间的碰撞
     */
    private void handleCollision() {
        List<Ant> ants = gameContext.getAnts();
        ants.sort(Comparator.comparingDouble(ant -> ant.getPosition().getX()));
        for (int i = 0; i < ants.size() - 1; i++) {
            Ant ant1 = ants.get(i);
            Ant ant2 = ants.get(i + 1);
            if (collisionDetect(ant1, ant2)) {
                ant1.getCollider().getOnCollide().accept(ant1, ant2);
                ant2.getCollider().getOnCollide().accept(ant2, ant1);
                System.out.println("Ant1: " + ant1.getId() + " and ant2: " + ant2.getId() + " have collided");
            }
        }
    }

    /**
     * 2d基础碰撞检测
     */
    private boolean collisionDetect(GameObject go1, GameObject go2) {
        Collider c1 = go1.getCollider();
        Collider c2 = go2.getCollider();
        Vector2D aMin = Vector2D.subtract(Vector2D.add(c1.getCenterOffset(), go1.getPosition()), c1.getHalfExtends());
        Vector2D aMax = Vector2D.add(Vector2D.add(c1.getCenterOffset(), go1.getPosition()), c1.getHalfExtends());
        Vector2D bMin = Vector2D.subtract(Vector2D.add(c2.getCenterOffset(), go2.getPosition()), c2.getHalfExtends());
        Vector2D bMax = Vector2D.add(Vector2D.add(c2.getCenterOffset(), go2.getPosition()), c2.getHalfExtends());
        return (aMin.getX() <= bMax.getX() && aMax.getX() >= bMin.getX())
                && (aMin.getY() <= bMax.getY() && aMax.getY() >= bMin.getY());
    }

    /**
     * 让蚂蚁开始爬，检测出杆
     */
    private void handleCreep() {
        Rod rod = gameContext.getRod();
        Vector2D rodPos = rod.getPosition();
        int rodHalfLength = rod.getHalfLength();

        List<Ant> ants = gameContext.getAnts();

        List<Ant> outAnts = new ArrayList<>();
        ants.forEach(ant -> {
            ant.getPosition().add(ant.getVelocity());

            if (ant.getPosition().getX() < rodPos.getX() - rodHalfLength
                    || ant.getPosition().getX() > rodPos.getX() + rodHalfLength) {
                System.out.println("Ant " + ant.getId() + " 出杆了");
                outAnts.add(ant);
            }
        });

        gameContext.getAnts().removeAll(outAnts);
        if (gameContext.getAnts().isEmpty()) {
            creepingGame.terminate();
        }
    }
}
