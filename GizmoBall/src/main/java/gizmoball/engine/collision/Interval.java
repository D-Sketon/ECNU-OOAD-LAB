package gizmoball.engine.collision;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Interval {
    protected double min;

    protected double max;

    /**
     * Returns true if the two {@link Interval}s overlap.
     *
     * @param interval the {@link Interval}
     * @return boolean
     */
    public boolean overlaps(Interval interval) {
        return !(this.min > interval.max || interval.min > this.max);
    }

    /**
     * Returns the amount of overlap between this {@link Interval} and the given
     * {@link Interval}.
     * <p>
     * This method tests to if the {@link Interval}s overlap first.  If they do then
     * the overlap is returned, if they do not then 0 is returned.
     *
     * @param interval the {@link Interval}
     * @return double
     */
    public double getOverlap(Interval interval) {
        if (this.overlaps(interval)) {
            return Math.min(this.max, interval.max) - Math.max(this.min, interval.min);
        }
        return 0;
    }

    /**
     * Returns true if the given {@link Interval} is contained in this {@link Interval} exclusively.
     *
     * @param interval the {@link Interval}
     * @return boolean
     */
    public boolean containsExclusive(Interval interval) {
        return interval.min > this.min && interval.max < this.max;
    }

    /**
     * 夹逼，消除double误差
     *
     * @param value 原值
     * @param left  下界
     * @param right 上界
     * @return double
     */
    public static double sandwich(double value, double left, double right) {
        return (value <= right && value >= left) ? value : (value < left ? left : right);
    }

}
