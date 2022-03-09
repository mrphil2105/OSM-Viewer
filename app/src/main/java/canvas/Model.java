package canvas;

import collections.Polygons;
import com.jogamp.opengl.*;
import java.io.*;
import javax.xml.stream.XMLStreamException;

public class Model {
    final GLCapabilities caps;
    final GLAutoDrawable sharedDrawable;
    final int[] vbo = new int[Model.VBOType.values().length];
    int count;

    public Model(String filename) throws IOException, XMLStreamException {

        final Polygons polygons = FileParser.readFile(filename);

        caps = new GLCapabilities(GLProfile.getMaxFixedFunc(true));
        // 8x anti-aliasing
        caps.setSampleBuffers(true);
        caps.setNumSamples(8);

        // sharedDrawable is the object we communicate with OpenGL through
        sharedDrawable =
                GLDrawableFactory.getFactory(caps.getGLProfile())
                        .createDummyAutoDrawable(null, true, caps, null);
        sharedDrawable.display();

        // Run once. We upload our various buffers to the GPU, registering them with OpenGL
        sharedDrawable.invoke(
                true,
                glAutoDrawable -> {
                    var gl = glAutoDrawable.getGL().getGL3();

                    var vertexBuffer = polygons.getVertexBuffer();
                    var indexBuffer = polygons.getIndexBuffer();
                    var colorBuffer = polygons.getColorBuffer();
                    count = indexBuffer.capacity();

                    // Get new id's for the buffers
                    gl.glGenBuffers(vbo.length, vbo, 0);

                    // Set the vertex buffer as the current buffer
                    gl.glBindBuffer(GL3.GL_ARRAY_BUFFER, getVBO(Model.VBOType.Vertex));
                    // Set the data for the current buffer to the data of the vertex buffer
                    gl.glBufferData(
                            GL3.GL_ARRAY_BUFFER,
                            (long) vertexBuffer.capacity() * Float.BYTES, // Allocate this many bytes for the buffer
                            vertexBuffer.rewind(),
                            GL.GL_STATIC_DRAW);

                    // Set the index buffer as the current index buffer
                    gl.glBindBuffer(GL3.GL_ELEMENT_ARRAY_BUFFER, getVBO(Model.VBOType.Index));
                    // Set the data for the current index buffer to the data of the index buffer
                    gl.glBufferData(
                            GL3.GL_ELEMENT_ARRAY_BUFFER,
                            (long) indexBuffer.capacity() * Integer.BYTES,
                            indexBuffer.rewind(),
                            GL.GL_STATIC_DRAW);

                    // Set the color buffer as the current buffer. This unsets the vertex buffer as the current one
                    // and, since we have already set all the data for the vertex buffer, this is fine.
                    gl.glBindBuffer(GL3.GL_ARRAY_BUFFER, getVBO(Model.VBOType.Color));
                    // Set the data for the current buffer to the data of the color buffer
                    gl.glBufferData(
                            GL3.GL_ARRAY_BUFFER,
                            (long) colorBuffer.capacity() * Float.BYTES,
                            colorBuffer.rewind(),
                            GL.GL_STATIC_DRAW);

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

    /**
     * Get the generated id of the buffer with the given type
     * @param type
     * @return Buffer id as seen from OpenGL
     */
    public int getVBO(Model.VBOType type) {
        return vbo[type.ordinal()];
    }

    /**
     * Get the amount of vertices
     * @return How many vertices are stored in the model
     */
    public int getCount() {
        return count;
    }

    enum VBOType {
        Vertex,
        Index,
        Color
    }
}
