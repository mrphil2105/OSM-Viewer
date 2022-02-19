package bfst22.vector;

import com.jogamp.common.nio.Buffers;

import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

public class LinesModel {
    private final float[] verticesBuffer;
    private final int[] indicesBuffer;
    private final float[] colorBuffer;
    private static final float lineWidth = 0.2f;
    private float zoom = 1;
    private float x = 0;
    private float y = 0;
    private FloatBuffer affinity;

    public LinesModel(String filename) throws IOException {
        recalculateAffinity();

        var array  = Files.lines(Paths.get(filename))
                    .flatMapToDouble(l -> Arrays.stream(l.split(" "))
                        .skip(1)
                        .mapToDouble(n -> Double.parseDouble(n))).toArray();

        var vertices = new float[array.length * 2];
        var indices = new int[array.length / 4 * 6];
        var colors = new float[array.length / 4 * 3];
        for (int i = 0; i < array.length / 4; i++) {
            var arrayIndex = i * 4;
            var bufferIndex = i * 8;
            var indexIndex = i * 6; // lol
            var colorIndex = i * 3;
            var start = new Vector((float)array[arrayIndex], (float)array[arrayIndex + 1]);
            var end = new Vector((float)array[arrayIndex + 2], (float)array[arrayIndex + 3]);
            var vec = end.sub(start);
            var p0 = vec.hat().normalize().scale(lineWidth / 2);
            var p3 = p0.scale(-1.0f);
            var p1 = p0.add(vec);
            var p2 = p3.add(vec);
            p0 = p0.add(start);
            p1 = p1.add(start);
            p2 = p2.add(start);
            p3 = p3.add(start);
            vertices[bufferIndex + 0] = p0.x();
            vertices[bufferIndex + 1] = p0.y();
            vertices[bufferIndex + 2] = p1.x();
            vertices[bufferIndex + 3] = p1.y();
            vertices[bufferIndex + 4] = p2.x();
            vertices[bufferIndex + 5] = p2.y();
            vertices[bufferIndex + 6] = p3.x();
            vertices[bufferIndex + 7] = p3.y();
            indices[indexIndex + 0] = arrayIndex + 0;
            indices[indexIndex + 1] = arrayIndex + 1;
            indices[indexIndex + 2] = arrayIndex + 2;
            indices[indexIndex + 3] = arrayIndex + 2;
            indices[indexIndex + 4] = arrayIndex + 0;
            indices[indexIndex + 5] = arrayIndex + 3;
            colors[colorIndex + 0] = 0.0f;
            colors[colorIndex + 1] = 0.0f;
            colors[colorIndex + 2] = 0.0f;
        }

        verticesBuffer = vertices;
        indicesBuffer = indices;
        colorBuffer = colors;
    }



    public float[] getVertices() {
        return verticesBuffer;
    }

    public int[] getIndices() {
        return indicesBuffer;
    }

    public float[] getColors() {
        return colorBuffer;
    }

    public FloatBuffer getAffinity() {
        return affinity;
    }

    public void zoom(float zoom) {
        this.zoom += zoom;
        recalculateAffinity();
    }

    public void addXY(float x, float y) {
        this.x += x;
        this.y += y;
        recalculateAffinity();
    }

    private void recalculateAffinity() {
        affinity = Buffers.newDirectFloatBuffer(new float[] {
                zoom, 0, 0, 0,
                0, zoom, 0, 0,
                0, 0, zoom, 0,
                x, y, 0, 1
        });
    }
}
