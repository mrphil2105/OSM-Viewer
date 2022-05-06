package navigation;

import java.io.Serializable;

import geometry.Point;

public record Road(String name, Point from, Point to, int role)
        implements Serializable {   
}
