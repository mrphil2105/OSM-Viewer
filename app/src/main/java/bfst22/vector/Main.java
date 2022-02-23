package bfst22.vector;

import java.io.IOException;

import javax.xml.stream.XMLStreamException;

import javafx.application.Application;
import osm.OSMReader;

public class Main {
    public static void main(String[] args) throws IOException, XMLStreamException {
        var reader = new OSMReader(args[0]);
        //Application.launch(App.class, args);
    }
}
