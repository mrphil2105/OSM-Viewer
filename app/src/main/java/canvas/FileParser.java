package canvas;

import collections.Polygons;
import osm.OSMReader;

import javax.xml.stream.XMLStreamException;
import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public class FileParser {

    public static Polygons readFile(String filename) throws IOException, XMLStreamException {
        // TODO: Clean up file reading/writing. Also, I was using Serializable before it
        // was cool...


        Polygons polygons;

        if (filename.endsWith(".osm")
                || filename.endsWith(".xml")
                || filename.endsWith(".osm.zip")
                || filename.endsWith(".xml.zip")) {

            InputStream stream = new BufferedInputStream(new FileInputStream(filename));
            if (filename.endsWith(".zip")) {
                var zipFile = new ZipFile(filename);
                var entry = zipFile.entries().nextElement();
                stream = zipFile.getInputStream(entry);
            }

            var reader = new OSMReader(stream);
            polygons = reader.createPolygons();
            filename = filename.split("\\.")[0] + ".ser.zip";

            serializeObject(polygons, filename);

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
            throw new IllegalArgumentException(
                    "Only .osm, .xml, .ser or any of those zipped are allowed");
        }
        return null;
    }

    static void serializeObject(Serializable serializable, String filename) throws IOException{
        //TODO: where does file get created???

        var zipStream = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(filename)));
        zipStream.putNextEntry(new ZipEntry(filename));
        var outputStream = new ObjectOutputStream(zipStream);
        outputStream.writeObject(serializable);
        outputStream.flush();
        outputStream.close();
    }
}

