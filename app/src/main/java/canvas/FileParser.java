package canvas;

import Search.AddressDatabase;
import drawing.Polygons;
import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;
import javax.xml.stream.XMLStreamException;
import osm.OSMReader;

public class FileParser {

    public static ReadResult readFile(String filename) throws IOException, XMLStreamException {
        Polygons polygons;
        AddressDatabase addresses= new AddressDatabase();;
        if (filename.matches(".*(\\.osm|\\.xml)(\\.zip)?$")) {
            var reader = new OSMReader();
            polygons = new Polygons();
            reader.addObserver(polygons);
            reader.addObserver(addresses);
            reader.parse(getInputStream(filename));

            filename = filename.split("\\.")[0] + ".ser.zip";
            serializeObject(polygons, filename);
        } else if (filename.endsWith(".ser") || filename.endsWith(".ser.zip")) {
            polygons = deserializeToPolygons(filename);
        } else {
            System.out.println("LSDKFJSDLKFJLSDKFJ");
            System.out.println(filename);
            throw new IllegalArgumentException(
                    "Only .osm, .xml, .ser or any of those zipped are allowed");
        }
        return new ReadResult(polygons,addresses);
    }

    static InputStream getInputStream(String filename) throws IOException {

        if (filename.endsWith(".zip")) {
            var zipFile = new ZipFile(filename);
            var entry = zipFile.entries().nextElement();
            return zipFile.getInputStream(entry);
        }

        return new BufferedInputStream(new FileInputStream(filename));
    }

    static void serializeObject(Serializable serializable, String filename) {

        try (var zipStream =
                new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(filename))); ) {
            zipStream.putNextEntry(new ZipEntry(filename));

            try (var outputStream = new ObjectOutputStream(zipStream)) {
                outputStream.writeObject(serializable);
            } catch (IOException e) {
                throw new RuntimeException(
                        "Could not serialize object: " + e.getMessage()); // TODO: mere præcis besked
            }

        } catch (IOException e) {
            throw new RuntimeException("Could not serialize object: " + e.getMessage()); // TODO
        }
    }

    static Polygons deserializeToPolygons(String filename) {
        try (InputStream inputStream = getInputStream(filename);
                ObjectInputStream objectInputStream = new ObjectInputStream(inputStream)) {

            return (Polygons) objectInputStream.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException("Could not read serialized file: " + e.getMessage());
        }
    }
}
