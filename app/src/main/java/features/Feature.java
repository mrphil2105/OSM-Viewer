package features;

import Search.AddressDatabase;
import io.*;
import java.io.ObjectInputStream;
import java.util.function.Function;
import navigation.Dijkstra;
import navigation.NearestNeighbor;
import util.ThrowingFunction;

public enum Feature {
    DRAWING("Visuals", (o) -> new PolygonsWriter(), PolygonsReader::new),
    NEAREST_NEIGHBOR(
            "Nearest Neighbor",
            (o) -> new ObjectWriter<>((NearestNeighbor) o),
            ObjectReader<NearestNeighbor>::new),
    PATHFINDING("Pathfinding", (o) -> new ObjectWriter<>((Dijkstra) o), ObjectReader<Dijkstra>::new),
    ADDRESS_SEARCH(
            "Address Search",
            (o) -> new ObjectWriter<>((AddressDatabase) o),
            ObjectReader<AddressDatabase>::new);

    private final String displayName;
    private final ThrowingFunction<Object, Writer> writer;
    private final Function<ObjectInputStream, Reader> reader;

    Feature(
            String displayName,
            ThrowingFunction<Object, Writer> writer,
            Function<ObjectInputStream, Reader> reader) {
        this.displayName = displayName;
        this.writer = writer;
        this.reader = reader;
    }

    public Writer createWriter() {
        return createWriter(null);
    }

    public Writer createWriter(Object arg) {
        try {
            return writer.apply(arg);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Reader createReader(ObjectInputStream stream) {
        return reader.apply(stream);
    }

    @Override
    public String toString() {
        return displayName;
    }
}
