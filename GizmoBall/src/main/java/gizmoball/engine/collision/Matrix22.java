package gizmoball.engine.collision;


import gizmoball.engine.geometry.Epsilon;
import gizmoball.engine.geometry.Vector2;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Matrix22 {
    /**
     * 0,0 元素
     */
    public double m00;

    /**
     * 0,1 元素
     */
    public double m01;

    /**
     * 1,0 元素
     */
    public double m10;

    /**
     * 1,1 元素
     */
    public double m11;

    /**
     * 将本{@link Matrix22}和被加{@link Matrix22}相加，返回本{@link Matrix22}
     *
     * @param matrix 被加的{@link Matrix22}
     * @return Matrix22
     */
    public Matrix22 add(Matrix22 matrix) {
        this.m00 += matrix.m00;
        this.m01 += matrix.m01;
        this.m10 += matrix.m10;
        this.m11 += matrix.m11;
        return this;
    }

    /**
     * 将本{@link Matrix22}和被乘{@link Vector2}相乘，返回相乘结果{@link Vector2}
     *
     * @param vector 被乘的{@link Vector2}
     * @return Vector2
     */
    public Vector2 product(Vector2 vector) {
        Vector2 copy = vector.copy();
        double x = copy.x;
        double y = copy.y;
        copy.x = this.m00 * x + this.m01 * y;
        copy.y = this.m10 * x + this.m11 * y;
        return copy;
    }

    /**
     * 返回本{@link Matrix22}的行列式
     *
     * @return double
     */
    public double determinant() {
        return this.m00 * this.m11 - this.m01 * this.m10;
    }

    /**
     * 将本{@link Matrix22}取逆矩阵，并返回
     *
     * @return Matrix22
     */
    public Matrix22 invert() {
        double det = this.determinant();
        if (Math.abs(det) > Epsilon.E) {
            det = 1.0 / det;
        }
        double a = this.m00;
        double b = this.m01;
        double c = this.m10;
        double d = this.m11;
        this.m00 = det * d;
        this.m01 = -det * b;
        this.m10 = -det * c;
        this.m11 = det * a;
        return this;
    }

    /**
     * 将本{@link Matrix22}取逆矩阵，并返回一个新矩阵
     *
     * @return Matrix22
     */
    public Matrix22 getInverse() {
        Matrix22 matrix22 = new Matrix22(this.m00, this.m01, this.m10, this.m11);
        return matrix22.invert();
    }

}
