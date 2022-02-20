package bfst22.vector;

public record Vector(float x, float y) {
    public Vector normalize() {
        return scale(1.0f / length());
    }

    public float length() {
        return (float) Math.sqrt(x * x + y * y);
    }

    public Vector scale(float s) {
        return new Vector(x * s, y * s);
    }

    public Vector hat() {
        return new Vector(-y, x);
    }

    public Vector sub(Vector other) {
        return new Vector(x - other.x, y - other.y);
    }

    public Vector add(Vector other) {
        return new Vector(x + other.x, y + other.y);
    }
}
