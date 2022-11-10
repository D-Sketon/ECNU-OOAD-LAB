package gizmoball.engine.collision;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 投影间隔，使用于SatDetector
 */
@Data
@AllArgsConstructor
public class Interval {
    /**
     * 最小值
     */
    protected double min;

    /**
     * 最大值
     */
    protected double max;

    /**
     * 判断两个{@link Interval}是否发生重叠
     *
     * @param interval 传入{@link Interval}
     * @return boolean
     */
    public boolean overlaps(Interval interval) {
        return !(this.min > interval.max || interval.min > this.max);
    }

    /**
     * 返回两个{@link Interval}重叠长度，如果没有重叠则返回0
     *
     * @param interval 传入{@link Interval}
     * @return double
     */
    public double getOverlap(Interval interval) {
        if (this.overlaps(interval)) {
            return Math.min(this.max, interval.max) - Math.max(this.min, interval.min);
        }
        return 0;
    }

    /**
     * 判断被传入{@link Interval}是否完全包含于本{@link Interval}
     *
     * @param interval 传入{@link Interval}
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
