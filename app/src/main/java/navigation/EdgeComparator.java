package navigation;

import java.util.Comparator;

class EdgeComparator implements Comparator<Edge> {
    private EdgeRole mode = EdgeRole.CAR;

    public void setMode(EdgeRole mode) {
        this.mode = mode;
    }

    @Override
    public int compare(Edge first, Edge second) {
        float firstWeight = first.distance();
        float secondWeight = second.distance();

        if (mode == EdgeRole.CAR) {
            firstWeight /= first.maxSpeed();
            secondWeight /= first.maxSpeed();
        }

        return Float.compare(firstWeight, secondWeight);
    }
}
