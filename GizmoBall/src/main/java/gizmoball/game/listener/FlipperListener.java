package gizmoball.game.listener;

import gizmoball.engine.collision.detector.BasicCollisionDetector;
import gizmoball.engine.collision.manifold.Manifold;
import gizmoball.engine.physics.PhysicsBody;
import gizmoball.game.entity.Flipper;
import javafx.util.Pair;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

import static gizmoball.game.GizmoSettings.DEFAULT_FLIPPER_ANGULAR;
import static gizmoball.game.GizmoSettings.DEFAULT_FLIPPER_ROTATION;

@Getter
@Setter
public class FlipperListener implements TickListener {

    private final List<PhysicsBody> balls;

    private final List<PhysicsBody> flippers;

    private final BasicCollisionDetector basicCollisionDetector = new BasicCollisionDetector();

    public FlipperListener(List<PhysicsBody> balls, List<PhysicsBody> flippers) {
        this.balls = balls;
        this.flippers = flippers;
    }


    @Override
    public List<Pair<Manifold, Pair<PhysicsBody, PhysicsBody>>> tick() {
        updatePosition(flippers);
        return basicCollisionDetector.detect(balls, flippers, new ArrayList<>());
    }

    /**
     * 每个tick更新挡板位置
     *
     * @param flippers
     */
    private void updatePosition(List<PhysicsBody> flippers) {
        for (PhysicsBody physicsBody : flippers) {
            Flipper flipper = (Flipper) physicsBody.getShape();
            double angular = flipper.getAngular();
            //上升状态
            if (flipper.isUp()) {
                //还未转到30度，继续旋转
                if (angular < 30) {
                    setUpVelocity(physicsBody);
                    flipper.flip(DEFAULT_FLIPPER_ROTATION);
                    continue;
                }
                //转至30度，停止
                physicsBody.setAngularVelocity(0);
            } else { //下降状态
                //还未归位，继续归位
                if (flipper.getAngular() > 0) {
                    setDownVelocity(physicsBody);
                    flipper.flip(-DEFAULT_FLIPPER_ROTATION);
                } else if (flipper.getAngular() == 0) {
                    physicsBody.setAngularVelocity(0);
                }
            }
        }
    }

    private void setUpVelocity(PhysicsBody physicsBody) {
        Flipper flipper = (Flipper) physicsBody.getShape();
        if (flipper.getDirection() == Flipper.Direction.LEFT) {
            physicsBody.setAngularVelocity(DEFAULT_FLIPPER_ANGULAR);
        } else {
            physicsBody.setAngularVelocity(-DEFAULT_FLIPPER_ANGULAR);
        }
    }

    private void setDownVelocity(PhysicsBody physicsBody) {
        Flipper flipper = (Flipper) physicsBody.getShape();
        if (flipper.getDirection() == Flipper.Direction.LEFT) {
            physicsBody.setAngularVelocity(-DEFAULT_FLIPPER_ANGULAR);
        } else {
            physicsBody.setAngularVelocity(DEFAULT_FLIPPER_ANGULAR);
        }
    }

}
