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
	/** The element at 0,0 */
	public double m00;

	/** The element at 0,1 */
	public double m01;

	/** The element at 1,0 */
	public double m10;

	/** The element at 1,1 */
	public double m11;

	/**
	 * Adds the given {@link Matrix22} to this {@link Matrix22}
	 * returning this {@link Matrix22}.
	 * <pre>
	 * this = this + m
	 * </pre>
	 * @param matrix the {@link Matrix22} to add
	 * @return {@link Matrix22} this matrix
	 */
	public Matrix22 add(Matrix22 matrix) {
		this.m00 += matrix.m00;
		this.m01 += matrix.m01;
		this.m10 += matrix.m10;
		this.m11 += matrix.m11;
		return this;
	}


	/**
	 * Multiplies this {@link Matrix22} by the given {@link Vector2} and
	 * places the result in the given {@link Vector2}.
	 * <pre>
	 * v = this * v
	 * </pre>
	 * @param vector the {@link Vector2} to multiply
	 * @return {@link Vector2} the vector result
	 */
	public Vector2 multiply(Vector2 vector) {
		double x = vector.x;
		double y = vector.y;
		vector.x = this.m00 * x + this.m01 * y;
		vector.y = this.m10 * x + this.m11 * y;
		return vector;
	}

	/**
	 * Multiplies this {@link Matrix22} by the given {@link Vector2} returning
	 * the result in a new {@link Vector2}.
	 * <pre>
	 * r = this * v
	 * </pre>
	 * @param vector the {@link Vector2} to multiply
	 * @return {@link Vector2} the vector result
	 */
	public Vector2 product(Vector2 vector) {
		return this.multiply(vector.copy());
	}

	/**
	 * Returns the determinant of this {@link Matrix22}.
	 * @return double
	 */
	public double determinant() {
		return this.m00 * this.m11 - this.m01 * this.m10;
	}
	
	/**
	 * Performs the inverse of this {@link Matrix22} and places the
	 * result in this {@link Matrix22}.
	 * @return {@link Matrix22} this matrix
	 */
	public Matrix22 invert() {
		// get the determinant
		double det = this.determinant();
		// check for zero determinant
		if (Math.abs(det) > Epsilon.E) {
			det = 1.0 / det;
		}
		double a = this.m00;
		double b = this.m01;
		double c = this.m10;
		double d = this.m11;
		this.m00 =  det * d;
		this.m01 = -det * b;
		this.m10 = -det * c;
		this.m11 =  det * a;
		return this;
	}
	/**
	 * Returns a new {@link Matrix22} containing the inverse of this {@link Matrix22}.
	 * @return {@link Matrix22} a new matrix containing the result
	 */
	public Matrix22 getInverse() {
		Matrix22 matrix22 = new Matrix22(this.m00, this.m01, this.m10, this.m11);
		return matrix22.invert();
	}

}
