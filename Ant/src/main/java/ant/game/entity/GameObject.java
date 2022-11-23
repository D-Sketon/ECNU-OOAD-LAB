package ant.game.entity;

import ant.game.physics.Collider;
import ant.game.physics.Vector2D;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 对游戏对象共有属性的抽象
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class GameObject {

    /**
     * 游戏对象的位置信息，使用{@link Vector2D}保存
     */
    protected Vector2D position;

    /**
     * 游戏对象的碰撞体，用于判断对个对象之间是否相撞
     */
    protected Collider collider;
}
