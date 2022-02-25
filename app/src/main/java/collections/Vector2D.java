package collections;

public record Vector2D(float x, float y) {
    public Vector2D normalize() {
        return scale(1.0f / length());
    }

    public float length() {
        return (float) Math.sqrt(x * x + y * y);
    }

    public Vector2D scale(float s) {
        return new Vector2D(x * s, y * s);
    }

    public Vector2D hat() {
        return new Vector2D(-y, x);
    }

    public Vector2D sub(Vector2D other) {
        return new Vector2D(x - other.x, y - other.y);
    }

    public Vector2D add(Vector2D other) {
        return new Vector2D(x + other.x, y + other.y);
    }
}
