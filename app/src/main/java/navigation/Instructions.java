package navigation;

import java.util.List;

import canvas.ZoomHandler;
 public class Instructions{
    private List<Road> edges;
    private double preDir = 0.0;
    private Road preEdge;
    private Road currentEdge;
    private Road nextEdge;
    private double lastDistance = 0.0;
    private String name = "";

    public Instructions(List<Road> edges) {
        this.edges = edges;
        getIntructions();
    }

    private void getIntructions() {
        for (int i = 0; i < edges.size(); i++) {
            if (preEdge == null) {
                preEdge = edges.get(0);
                nextEdge = edges.get(i+1);
                System.out.println("Start at " + preEdge.name());
                continue;
            }
            double calc = calc(edges.get(i));
            calc = alignOrientation(preDir, calc);
            currentEdge = edges.get(i);
            if (i < edges.size() - 1){
                nextEdge = edges.get(i + 1);
            } else {
                System.out.println("Stay at " + edges.get(i).name() + " for " + getDist((float)currentEdge.from().x(), (float)currentEdge.from().y(), (float)currentEdge.to().x(), (float)currentEdge.to().y()));
            }
            getDir(calc - preDir);
            preDir = calc;
            preEdge = edges.get(i);
        }
        System.out.println("You have arrived");
    }

    private void getDir(double calc) {
        double abs = Math.abs(calc);
        String dist = getDist((float)currentEdge.from().x(), (float)currentEdge.from().y(), (float)currentEdge.to().x(), (float)currentEdge.to().y());
        switch (currentEdge.role()){            
            case 1 :
                if (!preEdge.name().equals(currentEdge.name()) && !preEdge.name().equals(("")) && !currentEdge.name().equals("")){
                    System.out.println("Stay at " + preEdge.name() + " for " + dist + "and then take " + currentEdge.name());
                    lastDistance = 0.0;
                }
                break;

            case 2 :
                if (preEdge.role() != 2){
                        name = preEdge.name();
                } 
                switch(nextEdge.role()){
                    case 3:
                        System.out.println("Stay at " +  name + " for " + dist + "and then take the exit towards " + nextEdge.name());
                        lastDistance = 0.0;
                        break;
                    case 1:
                        if (name.equals(nextEdge.name())){
                            break;
                        }
                        System.out.println("Stay at " + name + " for " + dist + "and then take " + nextEdge.name());
                        lastDistance = 0.0;
                        break;
                    default:
                        break;
                };
                
                break;

            case 4 :
                if (preEdge.role() != 4){
                    System.out.println("Stay at " + preEdge.name() + " for " + dist + "and then take the roundabout");
                    lastDistance = 0.0;
                }
                else if (nextEdge.role() != 4) {
                    System.out.println("Exit the roundabout towards " + nextEdge.name());
                    lastDistance = 0.0;
                }               
                
                break;
            
            default:
                if (abs > 0.7 && preEdge.role() != 4) {
                    if (calc > 0){
                        System.out.println("Stay at " + preEdge.name() + " for " + dist + "and then turn left " + "at " + currentEdge.name());
                    } else {
                        System.out.println("Stay at " + preEdge.name() + " for " + dist + "and then turn right " + "at " + currentEdge.name());
                    }  
                    lastDistance = 0.0;
                }
                break;
            };
    }

    private double calc(Road edge) {
        double fromLat = edge.from().y();
        double fromLon = edge.from().x();
        double toLat = edge.to().y();
        double toLon = edge.to().x();
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
        double dist = ZoomHandler.getDistance(fromLon, fromLat, toLon, toLat) + lastDistance;
        lastDistance = dist;
        if (dist < 1000) {
            int distM = (int)Math.round(dist/10.0) * 10;
            return distM + " m ";
        } else {
            dist = (Math.round((dist/1000) * 100.0) / 100.0);
            return (dist) + " km ";
        }
    }
}


