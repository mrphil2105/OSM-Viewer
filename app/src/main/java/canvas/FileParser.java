package canvas;

import drawing.Polygons;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;
import javax.xml.stream.XMLStreamException;
import navigation.Dijkstra;
import osm.OSMReader;

// TODO: Use a custom exception type instead of RuntimeException for when parsing fails.
public class FileParser {

    public static ReadResult readFile(String filename) throws IOException, XMLStreamException {
        Polygons polygons;
        Dijkstra dijkstra;

        if (filename.matches(".*(\\.osm|\\.xml)(\\.zip)?$")) {
            var reader = new OSMReader();
            polygons = new Polygons();
            dijkstra = new Dijkstra();
            reader.addObserver(polygons);
            reader.addObserver(dijkstra);
            reader.parse(getInputStream(filename));

            filename = filename.split("\\.")[0] + ".zip";
            serializeObjects(filename, polygons, dijkstra);
        } else if (filename.endsWith(".zip")) {
            var objects = deserializeObjects(filename, Polygons.class, Dijkstra.class);
            polygons = (Polygons) objects.get(0);
            dijkstra = (Dijkstra) objects.get(1);
        } else {
            System.out.println(filename);
            throw new IllegalArgumentException(
                    "Only .osm, .xml, .ser or any of those zipped are allowed");
        }

        return new ReadResult(polygons, dijkstra);
    }

    private static InputStream getInputStream(String filename) throws IOException {
        if (filename.endsWith(".zip")) {
            var zipFile = new ZipFile(filename);
            var entry = zipFile.entries().nextElement();
            return zipFile.getInputStream(entry);
        }

        return new BufferedInputStream(new FileInputStream(filename));
    }

    private static void serializeObjects(String filename, Serializable... serializableArr) {
        try (var zipStream =
                new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(filename)))) {
            for (var serializable : serializableArr) {
                var className = serializable.getClass().getSimpleName();

                zipStream.putNextEntry(new ZipEntry(className + ".ser"));

                // Do not close the ObjectOutputStream as it closes the ZipOutputStream.
                var objectStream = new ObjectOutputStream(zipStream);
                objectStream.writeObject(serializable);
            }
        } catch (IOException e) {
            throw new RuntimeException("Could not serialize object: " + e.getMessage());
        }
    }

    @SuppressWarnings("rawtypes")
    private static List<Serializable> deserializeObjects(String filename, Class... classes) {
        var objects = new ArrayList<Serializable>();

        try (var zipFile = new ZipFile(filename)) {
            var classNames = Arrays.stream(classes).map(Class::getSimpleName).toList();

            for (var className : classNames) {
                var entryName = className + ".ser";
                var entry = zipFile.getEntry(entryName);

                if (entry == null) {
                    throw new RuntimeException("Zip entry with name '" + entryName + "' is not present.");
                }

                var inputStream = zipFile.getInputStream(entry);
                var objectStream = new ObjectInputStream(inputStream);
                var object = objectStream.readObject();

                objects.add((Serializable) object);
            }
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException("Could not read serialized file: " + e.getMessage());
        }

        return objects;
    }
}
