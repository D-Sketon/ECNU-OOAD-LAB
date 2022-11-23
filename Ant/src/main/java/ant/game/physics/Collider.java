package ant.game.physics;

import ant.game.entity.GameObject;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.function.BiConsumer;

@Data
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class Collider {

    private Vector2D centerOffset;
    private Vector2D halfExtends;

    private BiConsumer<GameObject, GameObject> onCollide;

    public Collider(Vector2D center, Vector2D halfExtends) {
        this(center, halfExtends, (o1, o2) -> {
        });
    }
}
