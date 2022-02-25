package bfst22.vector;

import collections.Polygons;
import com.jogamp.opengl.*;
import osm.OSMReader;

import javax.xml.stream.XMLStreamException;
import java.io.*;
import java.nio.Buffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class Model {
    private final FloatBuffer vertexBuffer;
    private final IntBuffer indexBuffer;
    private final FloatBuffer colorBuffer;
    private final GLCapabilities caps;
    private final GLAutoDrawable sharedDrawable;
    private final int[] vbo = new int[Model.VBOType.values().length];

    public Model(String filename) throws IOException, XMLStreamException {
        Polygons polygons;

        // TODO: Clean up file reading/writing.  Also, I was using Serializable before it was cool...
        if (filename.endsWith(".osm") ||
                filename.endsWith(".xml") ||
                filename.endsWith(".osm.zip") ||
                filename.endsWith(".xml.zip")) {
            polygons = new Polygons();

            InputStream stream = new BufferedInputStream(new FileInputStream(filename));
            if (filename.endsWith(".zip")) {
                var zipFile = new ZipFile(filename);
                var entry = zipFile.entries().nextElement();
                stream = zipFile.getInputStream(entry);
            }
            var reader = new OSMReader(stream);

            var drawables = reader.getWays().values().stream().filter(w -> !w.tags().isEmpty()).map(Drawable::new).filter(d -> d.getType() != null).sorted().toList();

            for (var drawable : drawables) {
                switch (drawable.getType().shape) {
                    case Polyline -> polygons.addLines(drawable.points, drawable.getType().size, drawable.getType().color);
                    case Fill -> polygons.addPolygon(drawable.points, drawable.getType().color);
                }
            }

            filename = filename.split("\\.")[0] + ".ser.zip";
            var zipStream = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(filename)));
            zipStream.putNextEntry(new ZipEntry(filename));
            var outputStream = new ObjectOutputStream(zipStream);
            outputStream.writeObject(polygons);
            outputStream.flush();
            outputStream.close();
        } else if (filename.endsWith(".ser") || filename.endsWith(".ser.zip")) {
            try {
                InputStream stream;
                if (filename.endsWith(".zip")) {
                    var zipFile = new ZipFile(filename);
                    var entry = zipFile.entries().nextElement();
                    stream = zipFile.getInputStream(entry);
                } else {
                    stream = new BufferedInputStream(new FileInputStream(filename));
                }

                stream = new ObjectInputStream(stream);
                polygons = (Polygons) ((ObjectInputStream) stream).readObject();
                stream.close();
            } catch (IOException | ClassNotFoundException e) {
                throw new RuntimeException("Could not read serialized file: " + e.getMessage());
            }
        } else {
            throw new IllegalArgumentException("Only .osm, .xml, .ser or any of those zipped are allowed");
        }

        vertexBuffer = polygons.getVertexBuffer();
        indexBuffer = polygons.getIndexBuffer();
        colorBuffer = polygons.getColorBuffer();

        caps = new GLCapabilities(GLProfile.getMaxFixedFunc(true));
        // 8x anti-aliasing
        caps.setSampleBuffers(true);
        caps.setNumSamples(8);

        sharedDrawable = GLDrawableFactory.getFactory(caps.getGLProfile()).createDummyAutoDrawable(null, true, caps, null);
        sharedDrawable.display();

        sharedDrawable.invoke(true, glAutoDrawable -> {
            var gl = glAutoDrawable.getGL().getGL3();

            gl.glGenBuffers(vbo.length, vbo, 0);

            gl.glBindBuffer(GL3.GL_ARRAY_BUFFER, getVBO(Model.VBOType.Vertex));
            gl.glBufferData(GL3.GL_ARRAY_BUFFER, (long) vertexBuffer.capacity() * Float.BYTES, vertexBuffer.rewind(), GL.GL_STATIC_DRAW);

            gl.glBindBuffer(GL3.GL_ELEMENT_ARRAY_BUFFER, getVBO(Model.VBOType.Index));
            gl.glBufferData(GL3.GL_ELEMENT_ARRAY_BUFFER, (long) indexBuffer.capacity() * Float.BYTES, indexBuffer.rewind(), GL.GL_STATIC_DRAW);

            gl.glBindBuffer(GL3.GL_ARRAY_BUFFER, getVBO(Model.VBOType.Color));
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

    public int getVBO(Model.VBOType type) {
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
