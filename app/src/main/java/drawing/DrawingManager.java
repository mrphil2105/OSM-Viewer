package drawing;

import geometry.Vector2D;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import javafx.util.Pair;

public class DrawingManager {
    private record DrawingInfo(
            Drawing drawing, int indicesStart, int verticesStart, int drawablesStart) {
        private DrawingInfo(Drawing drawing) {
            this(drawing, 0, 0, 0);
        }

        @Override
        public boolean equals(Object o) {
            if (o == this) return true;
            if (o instanceof DrawingInfo drawingInfo) return drawing() == drawingInfo.drawing();
            if (o instanceof Drawing drawing) return drawing() == drawing;
            return false;
        }
    }

    private final List<DrawingInfo> drawings = new ArrayList<>();
    private final List<Pair<Integer, Drawing>> drawingCache = new ArrayList<>();
    private final Drawing drawing = new Drawing();
    private int cacheByteSize;

    private DrawingInfo createDrawingInfo(Drawing drawing) {
        return new DrawingInfo(
                drawing,
                this.drawing.indices().size(),
                this.drawing.vertices().size(),
                this.drawing.drawables().size());
    }

    /**
     * Helper method that creates a drawing, adds it to the manager, and returns a reference to the
     * newly created drawing.
     *
     * @param points
     * @param drawable
     * @return
     */
    public Drawing draw(List<Vector2D> points, Drawable drawable) {
        return draw(points, drawable, 0);
    }

    /**
     * Helper method that creates a drawing, adds it to the manager, and returns a reference to the
     * newly created drawing.
     *
     * @param points
     * @param drawable
     * @return
     */
    public Drawing draw(List<Vector2D> points, Drawable drawable, int offset) {
        var drawing = Drawing.create(points, drawable, offset);
        draw(drawing);
        return drawing;
    }

    /**
     * Add a drawing to the manager.
     *
     * @param drawing Drawing to add.
     */
    public void draw(Drawing drawing) {
        flush();
        drawInternal(drawing);
    }

    private void drawInternal(Drawing drawing) {
        drawings.add(createDrawingInfo(drawing));
        this.drawing.draw(drawing);
    }

    /**
     * Helper method that creates a drawing, adds it orderly to the manager, and returns a reference
     * to the newly created drawing.
     *
     * @param points
     * @param drawable
     * @return
     */
    public Drawing drawOrdered(List<Vector2D> points, Drawable drawable) {
        return drawOrdered(points, drawable, 0);
    }

    /**
     * Helper method that creates a drawing, adds it orderly to the manager, and returns a reference
     * to the newly created drawing.
     *
     * @param points
     * @param drawable
     * @param offset
     * @return
     */
    public Drawing drawOrdered(List<Vector2D> points, Drawable drawable, int offset) {
        var drawing = Drawing.create(points, drawable, 0);
        drawOrdered(drawing, drawable.ordinal());
        return drawing;
    }

    /**
     * Add a drawing to the manager, but in a specific order relative to other drawings.
     *
     * @param drawing Drawing to add.
     * @param order Relative order to other drawings.
     */
    public void drawOrdered(Drawing drawing, int order) {
        cacheByteSize += drawing.byteSize();
        drawingCache.add(new Pair<>(order, drawing));
    }

    private void flush() {
        if (drawingCache.isEmpty()) return;

        drawingCache.sort(Comparator.comparingInt(Pair::getKey));
        for (var pair : drawingCache) {
            drawInternal(pair.getValue());
        }
        drawingCache.clear();
        cacheByteSize = 0;
    }

    /** Remove all drawings, but retain allocated memory. */
    public void clear() {
        drawingCache.clear();
        drawings.clear();
        drawing.clear();
    }

    /**
     * Clear a single drawing from the manager. This operation takes linear time in the worst case and
     * constant time if the drawing being removed was the latest added. One should prefer to clear
     * drawings in a LIFO order.
     *
     * @param drawing Exact reference to the Drawing to clear.
     */
    public void clear(Drawing drawing) {
        flush();

        var idx = drawings.indexOf(new DrawingInfo(drawing));
        if (idx == -1) return;

        // Reset state to how it was before the drawing was added
        var info = drawings.get(idx);
        this.drawing.indices().limit(info.indicesStart());
        this.drawing.vertices().limit(info.verticesStart());
        this.drawing.drawables().limit(info.drawablesStart());

        // Shift every drawing to the right of idx left by 1, rewriting history as if the drawing was
        // never there
        for (int i = idx; i < drawings.size() - 1; i++) {
            var d = drawings.get(i + 1).drawing();
            this.drawing.draw(d);
            drawings.set(i, createDrawingInfo(d));
        }

        drawings.remove(drawings.size() - 1);
    }

    public int byteSize() {
        return cacheByteSize + drawing.byteSize();
    }

    /**
     * Reference to underlying drawing. Do not mutate.
     *
     * @return
     */
    public Drawing drawing() {
        flush();
        return drawing;
    }
}
