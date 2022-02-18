package bfst22.vector;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import static java.util.stream.Collectors.toList;

public class Model implements Iterable<Drawable> {
    float minlat, minlon, maxlat, maxlon;
    List<Drawable> lines = new ArrayList<>();
    List<Runnable> observers = new ArrayList<>();

    public Model(String filename) throws IOException, XMLStreamException, FactoryConfigurationError {
        if (filename.endsWith(".osm")) {
            loadOSM(filename);
        } else {
            lines = Files.lines(Paths.get(filename))
                .map(Line::new)
                .collect(toList());
        }
    }

    private void loadOSM(String filename) throws FileNotFoundException, XMLStreamException, FactoryConfigurationError {
        var reader = XMLInputFactory.newInstance().createXMLStreamReader(new BufferedInputStream(new FileInputStream(filename)));
        var id2node = new HashMap<Long, OSMNode>();
        var nodes = new ArrayList<OSMNode>();
        while (reader.hasNext()) {
            switch (reader.next()) {
                case XMLStreamConstants.START_ELEMENT:
                    var name = reader.getLocalName();
                    switch (name) {
                        case "bounds":
                            maxlat = -Float.parseFloat(reader.getAttributeValue(null, "minlat"));
                            minlon = 0.56f * Float.parseFloat(reader.getAttributeValue(null, "minlon"));
                            minlat = -Float.parseFloat(reader.getAttributeValue(null, "maxlat"));
                            maxlon = 0.56f * Float.parseFloat(reader.getAttributeValue(null, "maxlon"));
                            break;
                        case "node":
                            var id = Long.parseLong(reader.getAttributeValue(null, "id"));
                            var lat = Float.parseFloat(reader.getAttributeValue(null, "lat"));
                            var lon = Float.parseFloat(reader.getAttributeValue(null, "lon"));
                            id2node.put(id, new OSMNode(0.56f * lon, -lat));
                            break;
                        case "nd":
                            var ref = Long.parseLong(reader.getAttributeValue(null, "ref"));
                            nodes.add(id2node.get(ref));
                            break;
                    }
                    break;
                case XMLStreamConstants.END_ELEMENT:
                    switch (reader.getLocalName()) {
                        case "way":
                            // if (!nodes.isEmpty()) {
                                var way = new OSMWay(nodes);
                                lines.add(way);
                                nodes.clear();
                            // }
                            break;
                    }
                    break;
            }
        }
    }

    public void addObserver(Runnable observer) {
        observers.add(observer);
    }

    public void notifyObservers() {
        for (var observer : observers) {
            observer.run();
        }
    }

    @Override
    public Iterator<Drawable> iterator() {
        return lines.iterator();
    }

    public void add(Line line) {
        lines.add(line);
        notifyObservers();
    }
}
