package bfst22.vector;

import java.io.File;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.*;
import shaders.EShaderAttribute;
import shaders.ShaderProgram;

/**
 * Performs the rendering.
 *
 * @author serhiy
 */
public class LinesRenderer implements GLEventListener {

    private final LinesModel model;
    private FloatBuffer vertexBuffer;
    private IntBuffer indexBuffer;
    private FloatBuffer colorBuffer;
    private ShaderProgram shaderProgram;
    private int[] ids;
    private FloatBuffer ortho;

    public LinesRenderer(LinesModel model) {
        this.model = model;
    }

    @Override
    public void init(GLAutoDrawable glAutoDrawable) {
        GL3 gl = glAutoDrawable.getGL().getGL3();

        File vertexShader = new File("shaders/default.vs");
        File fragmentShader = new File("shaders/default.fs");

        shaderProgram = new ShaderProgram();
        if (!shaderProgram.init(gl, vertexShader, fragmentShader)) {
            throw new IllegalStateException("Unable to initiate the shaders!");
        }

        vertexBuffer = Buffers.newDirectFloatBuffer(model.getVertices().length);
        indexBuffer = Buffers.newDirectIntBuffer(model.getIndices().length);
        colorBuffer = Buffers.newDirectFloatBuffer(model.getColors().length);

        vertexBuffer.put(model.getVertices());
        indexBuffer.put(model.getIndices());
        colorBuffer.put(model.getColors());

        this.ids = new int[3];
        gl.glGenBuffers(3, ids, 0);

        gl.glBindBuffer(GL3.GL_ARRAY_BUFFER, ids[0]);
        gl.glBufferData(GL3.GL_ARRAY_BUFFER, vertexBuffer.capacity() * Float.BYTES, vertexBuffer.rewind(), GL.GL_STATIC_DRAW);

        gl.glBindBuffer(GL3.GL_ELEMENT_ARRAY_BUFFER, ids[1]);
        gl.glBufferData(GL3.GL_ELEMENT_ARRAY_BUFFER, indexBuffer.capacity() * Float.BYTES, indexBuffer.rewind(), GL.GL_STATIC_DRAW);

        gl.glBindBuffer(GL3.GL_ARRAY_BUFFER, ids[2]);
        gl.glBufferData(GL3.GL_ARRAY_BUFFER, colorBuffer.capacity() * Float.BYTES, colorBuffer.rewind(), GL.GL_STATIC_DRAW);

        gl.glEnable(GL3.GL_LINE_SMOOTH);
        gl.glEnable(GL3.GL_POLYGON_SMOOTH);
        gl.glHint(GL3.GL_POLYGON_SMOOTH_HINT, GL.GL_NICEST);
        gl.glEnable(GL3.GL_BLEND);
        gl.glBlendFunc(GL3.GL_SRC_ALPHA, GL3.GL_ONE_MINUS_SRC_ALPHA);
        gl.glEnable(GL3.GL_MULTISAMPLE);
    }

    @Override
    public void dispose(GLAutoDrawable glAutoDrawable) {
        GL3 gl = glAutoDrawable.getGL().getGL3();
        shaderProgram.dispose(gl);
    }

    @Override
    public void display(GLAutoDrawable glAutoDrawable) {
        GL3 gl = glAutoDrawable.getGL().getGL3();

        gl.glClearColor(0.0f, 1.0f, 1.0f, 1.0f);
        gl.glClear(GL3.GL_COLOR_BUFFER_BIT | GL3.GL_DEPTH_BUFFER_BIT);

        gl.glUseProgram(shaderProgram.getProgramId());

        gl.glBindBuffer(GL3.GL_ARRAY_BUFFER, ids[2]);
        gl.glEnableVertexAttribArray(shaderProgram.getShaderLocation(EShaderAttribute.COLOR));
        gl.glVertexAttribPointer(shaderProgram.getShaderLocation(EShaderAttribute.COLOR), 3, GL3.GL_FLOAT, false, Float.BYTES * 3, 0);

        gl.glBindBuffer(GL3.GL_ELEMENT_ARRAY_BUFFER, ids[1]);

        gl.glBindBuffer(GL3.GL_ARRAY_BUFFER, ids[0]);
        gl.glEnableVertexAttribArray(shaderProgram.getShaderLocation(EShaderAttribute.POSITION));
        gl.glVertexAttribPointer(shaderProgram.getShaderLocation(EShaderAttribute.POSITION), 2, GL3.GL_FLOAT, false, Float.BYTES * 2, 0);

        gl.glUniformMatrix4fv(shaderProgram.getShaderLocation(EShaderAttribute.TRANS), 1, false, model.getAffinity().rewind());
        gl.glUniformMatrix4fv(shaderProgram.getShaderLocation(EShaderAttribute.ORTHO), 1, false, ortho.rewind());

        gl.glDrawElements(GL3.GL_TRIANGLES, model.getIndices().length, GL3.GL_UNSIGNED_INT, 0);

        gl.glDisableVertexAttribArray(shaderProgram.getShaderLocation(EShaderAttribute.POSITION));
        gl.glDisableVertexAttribArray(shaderProgram.getShaderLocation(EShaderAttribute.COLOR));

        gl.glUseProgram(0);
    }

    @Override
    public void reshape(GLAutoDrawable glAutoDrawable, int x, int y, int width,
                        int height) {
        final float left = 0.0f;
        final float right = width;
        final float top = 0.0f;
        final float bottom = height;
        final float near = 0.0f;
        final float far = 1.0f;

        this.ortho = Buffers.newDirectFloatBuffer(new float[] {
                2 / (right - left), 0, 0, 0,
                0, 2 / (top - bottom), 0, 0,
                0, 0, -2 / (far - near), 0,
                -(right + left) / (right - left), -(top + bottom) / (top - bottom), -(far + near) / (far - near), 1
        });
    }
}
