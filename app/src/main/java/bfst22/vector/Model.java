package bfst22.vector;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.io.IOException;

import static java.util.stream.Collectors.toList;

public class Model implements Iterable<Line> {
    List<Line> lines;
    List<Runnable> observers = new ArrayList<>();

    public Model(String filename) throws IOException {
        lines = Files.lines(Paths.get(filename))
            .map(Line::new)
            .collect(toList());
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
    public Iterator<Line> iterator() {
        return lines.iterator();
    }

    public void add(Line line) {
        lines.add(line);
        notifyObservers();
    }
}
