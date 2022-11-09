package gizmoball.engine.world.listener;

import gizmoball.engine.collision.BasicCollisionDetector;
import gizmoball.engine.collision.manifold.Manifold;
import gizmoball.engine.geometry.Vector2;
import gizmoball.engine.physics.PhysicsBody;
import gizmoball.engine.world.entity.Flipper;
import javafx.util.Pair;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

import static jdk.nashorn.internal.runtime.regexp.joni.Config.log;

@Getter
@Setter
public class FlipperListener implements TickListener{

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
     * @param flippers
     */
    public void updatePosition(List<PhysicsBody> flippers){
        for(PhysicsBody physicsBody : flippers){
            Flipper flipper = (Flipper) physicsBody.getShape();
            double angular = flipper.getAngular();
            //上升状态
            if(flipper.isUp()){
                //还未转到90度，继续旋转
                if(angular < 30){
                    flipper.setAngular(angular + 10);
                    flipper.flip();
                    continue;
                }
                //转至90度，停止
                flipper.setUp(false);
            } else { //下降状态
                //还未归位，继续归位
                if(flipper.getAngular() > 0){
                    flipper.setAngular(angular - 10);
                    flipper.flip();
                }
            }
        }
    }

}
