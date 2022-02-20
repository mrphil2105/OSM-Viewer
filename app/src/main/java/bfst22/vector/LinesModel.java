package bfst22.vector;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.*;
import shaders.ShaderProgram;

import java.io.File;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

public class LinesModel {
    private static final float lineWidth = 0.1f;
    private final FloatBuffer vertexBuffer;
    private final IntBuffer indexBuffer;
    private final FloatBuffer colorBuffer;
    private final GLCapabilities caps;
    private final GLAutoDrawable sharedDrawable;
    private final int[] vbo = new int[VBOType.values().length];

    public LinesModel(String filename) throws IOException {

        var array = Files.lines(Paths.get(filename))
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
            var start = new Vector((float) array[arrayIndex], (float) array[arrayIndex + 1]);
            var end = new Vector((float) array[arrayIndex + 2], (float) array[arrayIndex + 3]);
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
            indices[indexIndex + 3] = arrayIndex + 0;
            indices[indexIndex + 4] = arrayIndex + 2;
            indices[indexIndex + 5] = arrayIndex + 3;
            colors[colorIndex + 0] = 0.0f;
            colors[colorIndex + 1] = 0.0f;
            colors[colorIndex + 2] = 0.0f;
        }

        vertexBuffer = Buffers.newDirectFloatBuffer(vertices);
        indexBuffer = Buffers.newDirectIntBuffer(indices);
        colorBuffer = Buffers.newDirectFloatBuffer(colors);

        caps = new GLCapabilities(GLProfile.getMaxFixedFunc(true));
        // 8x anti-aliasing
        caps.setSampleBuffers(true);
        caps.setNumSamples(8);

        sharedDrawable = GLDrawableFactory.getFactory(caps.getGLProfile()).createDummyAutoDrawable(null, true, caps, null);
        sharedDrawable.display();

        sharedDrawable.invoke(true, glAutoDrawable -> {
            var gl = glAutoDrawable.getGL().getGL3();

            File vertexShader = new File("shaders/default.vs");
            File fragmentShader = new File("shaders/default.fs");

            var shaderProgram = new ShaderProgram();
            if (!shaderProgram.init(gl, vertexShader, fragmentShader)) {
                throw new IllegalStateException("Unable to initiate the shaders!");
            }

            gl.glGenBuffers(vbo.length, vbo, 0);

            gl.glBindBuffer(GL3.GL_ARRAY_BUFFER, getVBO(VBOType.Vertex));
            gl.glBufferData(GL3.GL_ARRAY_BUFFER, (long) vertexBuffer.capacity() * Float.BYTES, vertexBuffer.rewind(), GL.GL_STATIC_DRAW);

            gl.glBindBuffer(GL3.GL_ELEMENT_ARRAY_BUFFER, getVBO(VBOType.Index));
            gl.glBufferData(GL3.GL_ELEMENT_ARRAY_BUFFER, (long) indexBuffer.capacity() * Float.BYTES, indexBuffer.rewind(), GL.GL_STATIC_DRAW);

            gl.glBindBuffer(GL3.GL_ARRAY_BUFFER, getVBO(VBOType.Color));
            gl.glBufferData(GL3.GL_ARRAY_BUFFER, (long) colorBuffer.capacity() * Float.BYTES, colorBuffer.rewind(), GL.GL_STATIC_DRAW);

            gl.glEnable(GL3.GL_MULTISAMPLE);

            return true;
        });

        sharedDrawable.display();
    }

    public GLCapabilities getCaps() {
        return caps;
    }

    public GLAutoDrawable getSharedDrawable() {
        return sharedDrawable;
    }

    public int getVBO(VBOType type) {
        return vbo[type.ordinal()];
    }

    public int getCount() {
        return indexBuffer.capacity();
    }

    enum VBOType {
        Vertex,
        Index,
        Color
    }
}
