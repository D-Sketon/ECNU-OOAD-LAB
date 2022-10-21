package ant.game.entity;

import ant.game.physics.Vector2D;
import lombok.*;
import ant.game.physics.Collider;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = false, of = "id")
public class Ant extends GameObject {

    /**
     * 蚂蚁的ID，用于在游戏中标识唯一的蚂蚁
     */
    private String id;

    /**
     * 蚂蚁的速度
     */
    private Vector2D velocity;

    public Ant(String id) {
        this(id, Vector2D.zero());
    }

    public Ant(String id, Vector2D position) {
        this(id, position, new Collider(Vector2D.zero(), new Vector2D(20,20)));
    }

    public Ant(String id, Vector2D position, Collider collider){
        super(position, collider);
        this.id = id;
    }
}
