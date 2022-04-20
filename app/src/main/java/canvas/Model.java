package canvas;

import Search.AddressDatabase;
import com.jogamp.opengl.*;
import drawing.Drawable;
import geometry.Point;
import geometry.Rect;
import io.FileParser;
import io.PolygonsReader;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import navigation.Dijkstra;
import navigation.EdgeRole;
import pointsOfInterest.PointOfInterest;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Point2D;
import navigation.NearestNeighbor;

public class Model {

    private final GLCapabilities caps;
    private final GLAutoDrawable sharedDrawable;
    private VBOWrapper[] vbo;
    private final IntBuffer tex = IntBuffer.allocate(TexType.values().length);
    public final Rect bounds;
    private int indexCount;
    AddressDatabase addresses;
    private List<PointOfInterest> pointsOfInterest;

    private final NearestNeighbor nearestNeighbor;
    private final StringProperty nearestRoad = new SimpleStringProperty("none");

    private final Dijkstra dijkstra;
    private final ObservableList<Point> routePoints = FXCollections.observableArrayList();

    public Model(String filename) throws Exception {
        caps = new GLCapabilities(GLProfile.getMaxFixedFunc(true));
        // 8x anti-aliasing
        caps.setSampleBuffers(true);
        caps.setNumSamples(8);

        // sharedDrawable is the object we communicate with OpenGL through
        sharedDrawable =
                GLDrawableFactory.getFactory(caps.getGLProfile())
                        .createDummyAutoDrawable(null, true, caps, null);
        sharedDrawable.display();

        try (var result = FileParser.readFile(filename)) {
            bounds = result.bounds().read().getRect();
            loadPolygons(result.polygons());
            nearestNeighbor = result.nearestNeighbor().read();
            dijkstra = result.dijkstra().read();
            addresses = result.addresses().read();
            addresses.buildTries();
        }
        pointsOfInterest=new ArrayList<>();
    }

    private void loadPolygons(PolygonsReader reader) {
        // Run once. We upload our various buffers to the GPU, registering them with OpenGL
        sharedDrawable.invoke(
                true,
                glAutoDrawable -> {
                    var gl = glAutoDrawable.getGL().getGL3();

                    indexCount = reader.getIndexCount();
                    var vertexCount = reader.getVertexCount();
                    var drawableCount = reader.getDrawableCount();

                    // Pre-allocate buffers with correct size
                    var indexVBO =
                            new VBOWrapper(
                                    glAutoDrawable, GL3.GL_ELEMENT_ARRAY_BUFFER, (long) indexCount * Integer.BYTES);
                    var vertexVBO =
                            new VBOWrapper(glAutoDrawable, GL3.GL_ARRAY_BUFFER, (long) vertexCount * Float.BYTES);
                    var drawableVBO =
                            new VBOWrapper(
                                    glAutoDrawable, GL3.GL_ARRAY_BUFFER, (long) drawableCount * Byte.BYTES);
                    vbo = new VBOWrapper[] {indexVBO, vertexVBO, drawableVBO};

                    // Get new id's for textures
                    gl.glGenTextures(tex.capacity(), tex);

                    // Upload COLOR_MAP as 1D RGBA texture
                    gl.glActiveTexture(GL3.GL_TEXTURE0);
                    gl.glBindTexture(GL3.GL_TEXTURE_1D, getTex(TexType.COLOR_MAP));
                    gl.glTexParameteri(GL3.GL_TEXTURE_1D, GL3.GL_TEXTURE_MAG_FILTER, GL3.GL_NEAREST);
                    gl.glTexParameteri(GL3.GL_TEXTURE_1D, GL3.GL_TEXTURE_MIN_FILTER, GL3.GL_NEAREST);
                    gl.glTexImage1D(
                            GL3.GL_TEXTURE_1D,
                            0,
                            GL3.GL_RGBA,
                            Drawable.values().length,
                            0,
                            GL3.GL_RGBA,
                            GL3.GL_FLOAT,
                            Drawable.COLOR_MAP.rewind());

                    gl.glActiveTexture(GL3.GL_TEXTURE1);
                    gl.glBindTexture(GL3.GL_TEXTURE_1D, getTex(TexType.MAP));
                    gl.glTexParameteri(GL3.GL_TEXTURE_1D, GL3.GL_TEXTURE_MAG_FILTER, GL3.GL_NEAREST);
                    gl.glTexParameteri(GL3.GL_TEXTURE_1D, GL3.GL_TEXTURE_MIN_FILTER, GL3.GL_NEAREST);
                    gl.glTexImage1D(
                            GL3.GL_TEXTURE_1D,
                            0,
                            GL3.GL_RG32UI,
                            Drawable.values().length,
                            0,
                            GL3.GL_RG_INTEGER,
                            GL3.GL_UNSIGNED_INT,
                            Drawable.MAP.rewind());

                    var curIndex = 0;
                    var curVertex = 0;
                    var curDrawable = 0;

                    for (var drawing : reader.read()) {
                        // Upload chunk
                        indexVBO.set(
                                IntBuffer.wrap(drawing.indices().getArray()), curIndex, drawing.indices().size());
                        vertexVBO.set(
                                FloatBuffer.wrap(drawing.vertices().getArray()),
                                curVertex,
                                drawing.vertices().size());
                        drawableVBO.set(
                                ByteBuffer.wrap(drawing.drawables().getArray()),
                                curDrawable,
                                drawing.drawables().size());

                        curIndex += drawing.indices().size();
                        curVertex += drawing.vertices().size();
                        curDrawable += drawing.drawables().size();
                    }

                    System.gc();

                    return true;
                });
    }

    public GLCapabilities getCaps() {
        return caps;
    }

    public GLAutoDrawable getSharedDrawable() {
        return sharedDrawable;
    }

    /**
     * Get the generated buffer with the given type
     *
     * @param type
     * @return Buffer wrapper
     */
    public VBOWrapper getVBO(Model.VBOType type) {
        return vbo[type.ordinal()];
    }

    public int getTex(Model.TexType type) {
        return tex.get(type.ordinal());
    }

    /**
     * Get the amount of vertices
     *
     * @return How many vertices are stored in the model
     */
    public int getCount() {
        return indexCount;
    }

    public StringProperty nearestRoadProperty() {
        return nearestRoad;
    }

    public String getNearestRoad() {
        return nearestRoad.get();
    }

    public Point getNearestPoint(Point query) {
        return nearestNeighbor.nearestTo(query);
    }

    public void setQueryPoint(Point query) {
        var road = nearestNeighbor.nearestRoad(query);
        nearestRoadProperty().set(road);
    }

    public ObservableList<Point> getRoutePoints() {
        return routePoints;
    }

    public void calculateBestRoute(Point from, Point to) {
        // TODO: Allow user to set edge role.
        var shortestPath = dijkstra.shortestPath(from, to, EdgeRole.CAR);

        if (shortestPath == null) {
            routePoints.clear();
            System.out.println("No path between " + from + " and " + to + ".");

            return;
        }

        var routePoints = shortestPath.stream()
            .map(Point::geoToMap)
            .toList();
        this.routePoints.setAll(routePoints);
    }

    enum VBOType {
        INDEX,
        VERTEX,
        DRAWABLE,
    }

    enum TexType {
        COLOR_MAP,
        MAP,
    }

    public AddressDatabase getAddresses() {
        return addresses;
    }

    public List<PointOfInterest> getPointsOfInterest() {
        return pointsOfInterest;
    }
}
