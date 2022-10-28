package gizmoball.engine.collision;

import lombok.Data;

@Data
public class Interval {
    protected double min;

    protected double max;

    public Interval(double min, double max) {
        this.min = min;
        this.max = max;
    }

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

}
