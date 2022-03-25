package collections.spacial;

import java.io.Serializable;

public record Point(float x, float y) implements Serializable {
    public float distanceTo(Point other) {
        return (float) Math.sqrt(distanceSquaredTo(other));
    }

    public float distanceSquaredTo(Point other) {
        float dx = this.x - other.x;
        float dy = this.y - other.y;

        return dx * dx + dy * dy;
    }
}
