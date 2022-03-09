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

        sharedDrawable =
                GLDrawableFactory.getFactory(caps.getGLProfile())
                        .createDummyAutoDrawable(null, true, caps, null);
        sharedDrawable.display();

        sharedDrawable.invoke(
                true,
                glAutoDrawable -> {
                    var gl = glAutoDrawable.getGL().getGL3();

                    var vertexBuffer = polygons.getVertexBuffer();
                    var indexBuffer = polygons.getIndexBuffer();
                    var colorBuffer = polygons.getColorBuffer();
                    count = indexBuffer.capacity();

                    gl.glGenBuffers(vbo.length, vbo, 0);

                    gl.glBindBuffer(GL3.GL_ARRAY_BUFFER, getVBO(Model.VBOType.Vertex));
                    gl.glBufferData(
                            GL3.GL_ARRAY_BUFFER,
                            (long) vertexBuffer.capacity() * Float.BYTES,
                            vertexBuffer.rewind(),
                            GL.GL_STATIC_DRAW);

                    gl.glBindBuffer(GL3.GL_ELEMENT_ARRAY_BUFFER, getVBO(Model.VBOType.Index));
                    gl.glBufferData(
                            GL3.GL_ELEMENT_ARRAY_BUFFER,
                            (long) indexBuffer.capacity() * Float.BYTES,
                            indexBuffer.rewind(),
                            GL.GL_STATIC_DRAW);

                    gl.glBindBuffer(GL3.GL_ARRAY_BUFFER, getVBO(Model.VBOType.Color));
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

    public int getVBO(Model.VBOType type) {
        return vbo[type.ordinal()];
    }

    public int getCount() {
        return count;
    }

    enum VBOType {
        Vertex,
        Index,
        Color
    }
}
