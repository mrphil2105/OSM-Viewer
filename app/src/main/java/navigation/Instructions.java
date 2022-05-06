package navigation;

import java.util.List;

import canvas.ZoomHandler;
 public class Instructions{
    private List<Road> edges;
    private double preDir = 0.0;
    private Road preEdge;
    private Road currentEdge;
    private Road lastInstruction;
    private Road nextEdge;
    private double distTotal;

    public Instructions(List<Road> edges) {
        this.edges = edges;
        getIntructions();
    }

    private void getIntructions() {
        for (int i = 0; i < edges.size(); i++) {
            if (preEdge == null) {
                preEdge = edges.get(0);
                lastInstruction = edges.get(0);
                nextEdge = edges.get(i+1);
                System.out.println("Start at " + preEdge.name());
                continue;
            }
            double calc = calc(edges.get(i));
            calc = alignOrientation(preDir, calc);
            currentEdge = edges.get(i);
            if (i < edges.size() - 1){
                nextEdge = edges.get(i + 1);
            }
            getDir(calc - preDir);
            preDir = calc;
            preEdge = edges.get(i);
        }
    }

    private void getDir(double calc) {
        double abs = Math.abs(calc);
        String dist = getDist((float)lastInstruction.fromLon(), (float)lastInstruction.fromLat(), (float)currentEdge.fromLon(), (float)currentEdge.fromLat());
        switch (currentEdge.role()){
            case ROUNDABOUT :
                if (preEdge.role() == RoadRole.ROUNDABOUT){
                    break;
                }
                System.out.println("Continue forward for " + dist + "at " + preEdge.name() + " and then take the roundabout");
                lastInstruction = currentEdge;
                break;
            case MOTORWAY_LINK :
                if (preEdge.role() == RoadRole.MOTORWAY) {
                    System.out.println("Continue forward for " + dist + "at " + preEdge.name() + " and then take the exit towards " + nextEdge.name());
                }
                lastInstruction = currentEdge;
                break;
            default:
                if (abs > 0.8 ) {
                    if (preEdge.role() == RoadRole.ROUNDABOUT){
                        System.out.println("Exit the roundabout towards " + currentEdge.name());
                    }
                    else if (calc > 0){
                        System.out.println("Continue forward for " + dist + "at " + preEdge.name() + " and then turn left " + "at " + currentEdge.name());
                    } else {
                        System.out.println("Continue forward for " + dist + "at " + preEdge.name() + " and then turn right " + "at " + currentEdge.name());
                    }
                    lastInstruction = currentEdge;   
                }
                break;
            };
    }

    private double calc(Road edge) {
        double fromLat = edge.fromLat(); 
        double fromLon = edge.fromLon();
        double toLat = edge.toLat();
        double toLon = edge.toLon();
        return (Math.atan2(toLat-fromLat, (toLon-fromLon)));
    }

    private double alignOrientation(double baseOrientation, double orientation) {
        double resultOrientation;
        if (baseOrientation >= 0) {
            if (orientation < -Math.PI + baseOrientation) {
                resultOrientation = orientation + 2 * Math.PI;
            } else {
                resultOrientation = orientation;
            }
        } else if (orientation > + Math.PI + baseOrientation) {
            resultOrientation = orientation - 2 * Math.PI;
        }
        else {
            resultOrientation = orientation;
        }
        return resultOrientation;
    }

    private String getDist(float fromLon, float fromLat, float toLon, float toLat){
        double dist = ZoomHandler.getDistance(fromLon, fromLat, toLon, toLat);
        if (dist < 1000) {
            int distM = (int)Math.round(dist/10.0) * 10;
            return distM + " m ";
        } else {
            dist = (Math.round((dist/1000) * 100.0) / 100.0);
            return (dist) + " km ";
        }
    }
}


