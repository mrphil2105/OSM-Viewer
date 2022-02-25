package bfst22.vector;

import javafx.application.Application;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException, XMLStreamException {
        //var reader = new OSMReader(args[0]);
        Application.launch(App.class, args);
    }
}
