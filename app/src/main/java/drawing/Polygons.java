package drawing;

import collections.Vector2D;
import collections.lists.FloatList;
import collections.lists.IntList;
import com.jogamp.common.nio.Buffers;
import earcut4j.Earcut;
import java.io.Serializable;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.*;
import javafx.scene.paint.Color;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.operation.linemerge.LineMerger;
import osm.OSMObserver;
import osm.elements.OSMMemberWay;
import osm.elements.OSMRelation;
import osm.elements.OSMWay;

public class Polygons implements OSMObserver, Serializable {
    FloatList vertices = new FloatList();
    IntList indices = new IntList();
    FloatList colors = new FloatList();

    public Polygons() {}

    // Credit: https://flassari.is/2008/11/line-line-intersection-in-cplusplus/
    private static Vector2D intersection(Vector2D p1, Vector2D p2, Vector2D p3, Vector2D p4) {
        // Store the values for fast access and easy
        // equations-to-code conversion
        double x1 = p1.x(), x2 = p2.x(), x3 = p3.x(), x4 = p4.x();
        double y1 = p1.y(), y2 = p2.y(), y3 = p3.y(), y4 = p4.y();

        double d = (x1 - x2) * (y3 - y4) - (y1 - y2) * (x3 - x4);
        // If d is zero, there is no intersection
        if (Math.abs(d) < 0.01) return null;

        // Get the x and y
        double pre = (x1 * y2 - y1 * x2), post = (x3 * y4 - y3 * x4);
        double x = (pre * (x3 - x4) - (x1 - x2) * post) / d;
        double y = (pre * (y3 - y4) - (y1 - y2) * post) / d;

        // Check if the x and y coordinates are within both lines (I don't need this)
        // if (x < min(x1, x2) || x > max(x1, x2) ||
        // x < min(x3, x4) || x > max(x3, x4)) return null;
        // if (y < min(y1, y2) || y > max(y1, y2) ||
        // y < min(y3, y4) || y > max(y3, y4)) return null;

        // Return the point of intersection
        return new Vector2D(x, y);
    }

    public void addPolygon(List<Vector2D> points, Color color, float layer) {
        // Copy all coordinates into an array
        var verts = new double[points.size() * 3];
        for (int i = 0; i < points.size(); i++) {
            var p = points.get(i);
            verts[i * 3] = p.x();
            verts[i * 3 + 1] = p.y();
            verts[i * 3 + 2] = layer;
            addVertex(p, color, layer);
        }

        // Calculate indices for each vertex in triangulated polygon
        Earcut.earcut(verts, null, 3).stream()
                .map(
                        i ->
                                (vertices.size() - verts.length) / 3
                                        + i) // Offset each index before adding to indices
                .forEach(indices::add);
    }

    public void addLine(List<Vector2D> points, double width, Color color, float layer) {
        // Lines must exist of at least two points
        if (points.size() < 2) {
            return;
        }

        // To draw a line with triangles, we must find p0-3 and connect them accordingly.
        // Diagram showing what each variable corrosponds to:
        //  p0 ------------ p1
        //   |               |
        // from --- dir --> to
        //   |               |
        //  p3 ------------ p2

        var from = points.get(0);
        var to = points.get(1);
        var dir = to.sub(from);

        // find p0-3 by manipulating vectors
        var p3 = dir.hat().normalize().scale(width);
        var p0 = p3.scale(-1.0f);
        var p1 = p0.add(dir);
        var p2 = p3.add(dir);

        // These points are final, we can add them now
        addVertex(p0.add(from), color, layer);
        addVertex(p3.add(from), color, layer);

        // Loop through remaining points in line, calculating a pair of points in each iteration
        for (int i = 2; i < points.size(); i++) {
            // Where we're going next
            var nextTo = points.get(i);
            var nextDir = nextTo.sub(to);

            // Corners drawn from the next point
            var p3Next = nextDir.hat().normalize().scale(width);
            var p0Next = p3Next.scale(-1.0f);
            var p1Next = p0Next.add(nextDir);
            var p2Next = p3Next.add(nextDir);

            // Add two triangles: p0, p2, p3 and p0, p3, p1
            indices.add(vertices.size() / 3 - 2);
            indices.add(vertices.size() / 3 + 0);
            indices.add(vertices.size() / 3 + 1);
            indices.add(vertices.size() / 3 - 2);
            indices.add(vertices.size() / 3 + 1);
            indices.add(vertices.size() / 3 - 1);

            // Find intersections between previous two lines and next two lines
            var intersect1 = intersection(p0.add(to), p1.add(to), p0Next.add(nextTo), p1Next.add(nextTo));
            var intersect2 = intersection(p3.add(to), p2.add(to), p3Next.add(nextTo), p2Next.add(nextTo));

            // Intersection is null if lines are parallel
            if (intersect1 != null) {
                addVertex(intersect1, color, layer);
            } else {
                addVertex(p1.add(to), color, layer);
            }

            if (intersect2 != null) {
                addVertex(intersect2, color, layer);
            } else {
                addVertex(p2.add(to), color, layer);
            }

            // Move forward one point, setting the "current" points to the "next points"
            to = nextTo;
            p0 = p0Next;
            p1 = p1Next;
            p3 = p3Next;
            p2 = p2Next;
        }

        // Add triangles for the last point
        indices.add(vertices.size() / 3 - 2);
        indices.add(vertices.size() / 3 + 0);
        indices.add(vertices.size() / 3 + 1);
        indices.add(vertices.size() / 3 - 2);
        indices.add(vertices.size() / 3 + 1);
        indices.add(vertices.size() / 3 - 1);

        addVertex(p0.add(to), color, layer);
        addVertex(p3.add(to), color, layer);
    }

    /**
     * Add a vertex with a color and layer into the correct position in `vertices` and `colors`
     *
     * @param vertex
     * @param color
     * @param layer
     */
    private void addVertex(Vector2D vertex, Color color, float layer) {
        vertices.add((float) vertex.x());
        vertices.add((float) vertex.y());
        vertices.add(layer);
        addColor(color);
    }

    private void addColor(Color color) {
        colors.add((float) color.getRed());
        colors.add((float) color.getGreen());
        colors.add((float) color.getBlue());
    }

    public FloatBuffer getVertexBuffer() {
        return Buffers.newDirectFloatBuffer(vertices.toArray());
    }

    public IntBuffer getIndexBuffer() {
        return Buffers.newDirectIntBuffer(indices.toArray());
    }

    public FloatBuffer getColorBuffer() {
        return Buffers.newDirectFloatBuffer(colors.toArray());
    }

    @Override
    public void onWay(OSMWay way) {
        var drawable = Drawable.from(way);
        if (drawable == Drawable.UNKNOWN) return;

        var points = way.nodes().stream().map(n -> new Vector2D(n.lon(), n.lat())).toList();
        switch (drawable.shape) {
            case POLYLINE -> addLine(points, drawable.size, drawable.color, drawable.layer());
            case FILL -> addPolygon(points, drawable.color, drawable.layer());
        }
    }

    @Override
    public void onRelation(OSMRelation relation) {
        var drawable = Drawable.from(relation);
        if (drawable == Drawable.UNKNOWN) return;

        var lines = new ArrayList<Geometry>();
        var geometryFactory = new GeometryFactory();

        Iterable<OSMWay> iter =
                relation.members().stream()
                                .filter(m -> m.role() == OSMMemberWay.Role.OUTER)
                                .map(OSMMemberWay::way)
                        ::iterator;
        for (var way : iter) {
            lines.add(
                    geometryFactory.createLineString(
                            way.nodes().stream()
                                    .map(n -> new Coordinate(n.lon(), n.lat()))
                                    .toArray(Coordinate[]::new)));
        }

        var merger = new LineMerger();
        merger.add(lines);
        Collection<LineString> merged = merger.getMergedLineStrings();

        addPolygon(
                merged.stream()
                        .flatMap(l -> Arrays.stream(l.getCoordinates()).map(c -> new Vector2D(c.x, c.y)))
                        .toList(),
                drawable.color,
                drawable.layer());
    }
}
