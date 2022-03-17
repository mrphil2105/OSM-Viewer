package canvas;

import Search.AddressDatabase;
import drawing.Polygons;

public record ReadResult(Polygons polygons, AddressDatabase addresses) {
}
