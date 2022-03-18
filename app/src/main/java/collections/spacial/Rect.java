package collections.spacial;

public record Rect(float xMin, float yMin, float xMax, float yMax) {
    public float distanceTo(Point point) {
        return (float) Math.sqrt(this.distanceSquaredTo(point));
    }

    public float distanceSquaredTo(Point point) {
        float dx = 0, dy = 0;

        if (point.x() < xMin) dx = point.x() - xMin;
        else if (point.x() > xMax) dx = point.x() - xMax;

        if (point.y() < yMin) dy = point.y() - yMin;
        else if (point.y() > yMax) dy = point.y() - yMax;

        return dx * dx + dy * dy;
    }

    public boolean contains(Point point) {
        return point.x() >= xMin && point.x() <= xMax && point.y() >= yMin && point.y() <= yMax;
    }

    public boolean intersects(Rect other) {
        return this.xMax >= other.xMin
                && this.yMax >= other.yMin
                && other.xMax >= this.xMin
                && other.yMax >= this.yMin;
    }
}
