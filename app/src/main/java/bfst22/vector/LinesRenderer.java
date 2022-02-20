package bfst22.vector;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.GL3;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLEventListener;
import shaders.Location;
import shaders.ShaderProgram;

import java.io.File;
import java.nio.FloatBuffer;

public class LinesRenderer implements GLEventListener {
    private final LinesModel model;
    private final MapCanvas canvas;
    private ShaderProgram shaderProgram;
    private FloatBuffer orthographic;

    public LinesRenderer(LinesModel model, MapCanvas canvas) {
        this.model = model;
        this.canvas = canvas;
    }

    @Override
    public void init(GLAutoDrawable glAutoDrawable) {
        GL3 gl = glAutoDrawable.getGL().getGL3();

        File vertexShader = new File("shaders/default.vs");
        File fragmentShader = new File("shaders/default.fs");

        shaderProgram = new ShaderProgram();
        shaderProgram.init(gl, vertexShader, fragmentShader);

        gl.glBindBuffer(GL3.GL_ARRAY_BUFFER, model.getVBO(LinesModel.VBOType.Vertex));
        gl.glVertexAttribPointer(shaderProgram.getLocation(Location.POSITION), 2, GL3.GL_FLOAT, false, Float.BYTES * 2, 0);
        gl.glEnableVertexAttribArray(shaderProgram.getLocation(Location.POSITION));

        gl.glBindBuffer(GL3.GL_ELEMENT_ARRAY_BUFFER, model.getVBO(LinesModel.VBOType.Index));

        gl.glBindBuffer(GL3.GL_ARRAY_BUFFER, model.getVBO(LinesModel.VBOType.Color));
        gl.glVertexAttribPointer(shaderProgram.getLocation(Location.COLOR), 3, GL3.GL_FLOAT, false, Float.BYTES * 3, 0);
        gl.glEnableVertexAttribArray(shaderProgram.getLocation(Location.COLOR));
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

        gl.glUniformMatrix4fv(shaderProgram.getLocation(Location.TRANS), 1, false, canvas.getTransformBuffer().rewind());
        gl.glUniformMatrix4fv(shaderProgram.getLocation(Location.ORTHOGRAPHIC), 1, false, orthographic.rewind());

        gl.glDrawElements(GL3.GL_TRIANGLES, model.getCount(), GL3.GL_UNSIGNED_INT, 0);

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

        // Recalculate orthographic projection matrix
        this.orthographic = Buffers.newDirectFloatBuffer(new float[]{
                2 / (right - left), 0, 0, 0,
                0, 2 / (top - bottom), 0, 0,
                0, 0, -2 / (far - near), 0,
                -(right + left) / (right - left), -(top + bottom) / (top - bottom), -(far + near) / (far - near), 1
        });
    }
}
