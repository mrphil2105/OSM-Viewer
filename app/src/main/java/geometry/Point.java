package geometry;

public record Point(float x, float y) {
    public Point(Vector2D vec) {
        this((float) vec.x(), (float) vec.y());
    }

    public float distanceTo(Point other) {
        return (float) Math.sqrt(distanceSquaredTo(other));
    }

    public float distanceSquaredTo(Point other) {
        float dx = this.x - other.x;
        float dy = this.y - other.y;

        return dx * dx + dy * dy;
    }

    // TODO: Find equations that can translate sphere to a flat earth
    public static Point geoToMap(Point point, Rect bounds) {
        return new Point(
                (float) geoToMapX(point.x(), bounds),
                (float) geoToMapY(point.y(), bounds)
        );
    }

    public static double geoToMapX(double x, Rect bounds) {
        return (x - (bounds.left() + bounds.right()) / 2) * 5600;
    }

    public static double geoToMapY(double y, Rect bounds) {
        return (y - (bounds.left() + bounds.right()) / 2) * 10000;
    }
}
