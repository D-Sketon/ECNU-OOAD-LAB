package ant.game.physics;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Vector2D {

    private double x;

    private double y;

    public void add(Vector2D v) {
        x += v.x;
        y += v.y;
    }

    public void subtract(Vector2D v) {
        x -= v.x;
        y -= v.y;
    }

    public static Vector2D add(Vector2D v1, Vector2D v2) {
        return new Vector2D(v1.getX() + v2.getX(), v1.getY() + v2.getY());
    }

    public static Vector2D subtract(Vector2D v1, Vector2D v2) {
        return new Vector2D(v1.getX() - v2.getX(), v1.getY() - v2.getY());
    }

    public static Vector2D zero() {
        return new Vector2D(0f, 0f);
    }

}
