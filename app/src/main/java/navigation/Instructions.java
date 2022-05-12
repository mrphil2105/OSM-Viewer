package navigation;

import java.util.List;

import canvas.ZoomHandler;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
 public class Instructions{
    private List<Road> edges;
    private double preDir = 0.0;
    private double preAbs;
    private Road preEdge;
    private Road currentEdge;
    private Road nextEdge;
    private double lastDistance = 0.0;
    private String name = "";
    private String instructionsString = "";
    private int role;

    public Instructions(List<Road> edges) {
        this.edges = edges;
        getIntructions();
    }

    private void getIntructions() {
        for (int i = 0; i < edges.size(); i++) {
            if (preEdge == null) {
                preEdge = edges.get(0);
                nextEdge = edges.get(i+1);
                instructionsString += "Start at " + preEdge.name() + "\n";
                continue;
            }
            double calc = calc(edges.get(i));
            preDir = calc(preEdge);
            currentEdge = edges.get(i);
            if (i < edges.size() - 1){
                nextEdge = edges.get(i + 1);
            } else {
                instructionsString += "Stay at " + edges.get(i).name() + " for " + getDist((float)currentEdge.from().x(), (float)currentEdge.from().y(), (float)currentEdge.to().x(), (float)currentEdge.to().y())  + "\n";
                break;
            }
            getDir(alignOrientation(preDir, calc) - preDir);
            preEdge = edges.get(i);
        }
        instructionsString += "You have arrived" + "\n";
        Clipboard clipboard = Clipboard.getSystemClipboard();
        ClipboardContent content = new ClipboardContent();
        content.putString(instructionsString);
        clipboard.setContent(content);
    }

    private void getDir(double calc) {
        double abs = Math.abs(calc);
        String dist = getDist((float)currentEdge.from().x(), (float)currentEdge.from().y(), (float)currentEdge.to().x(), (float)currentEdge.to().y());

        if (!preEdge.name().equals("Unnamed way")){
            name = preEdge.name();
            role = preEdge.role();
        } 
        switch (currentEdge.role()){            
            case 1 :
                if (!preEdge.name().equals(currentEdge.name()) && !currentEdge.name().equals("Unnamed way") && !nextEdge.name().equals("Unnamed way") && preEdge.role() != 2){
                    instructionsString += "Stay at " + name + " for " + dist + "and then continue on " + currentEdge.name() + "\n";
                    lastDistance = 0.0;
                } else if (!preEdge.name().equals(currentEdge.name()) && !currentEdge.name().equals("Unnamed way") && !nextEdge.name().equals("Unnamed way") && preEdge.role() == 2){
                    instructionsString += "Merge with " + currentEdge.name() + " in " + dist + "\n";
                    lastDistance = 0.0;
                }
                break;

            case 2 :
                switch(nextEdge.role()){
                    case 3:
                        instructionsString += "Stay at " +  name  + " and in " + dist + "then take the exit towards " + nextEdge.name() + "\n";
                        lastDistance = 0.0;
                        break;
                    case 1:
                        if (name.equals(nextEdge.name())){
                            break;
                        } 
                        else if (role == 1){
                            instructionsString += "Stay at " + name + " for " + dist + "then continue on " + nextEdge.name() + "\n";
                        } else {
                            instructionsString += "Stay at " + name + " and then take the access road in " + dist + "to " +  nextEdge.name() + "\n";
                        }
                        
                        lastDistance = 0.0;
                        break;
                    default:
                        break;
                };
                break;

            case 4 :
                if (preEdge.role() != 4){
                    instructionsString += "Stay at " + name + " and then take the roundabout in " + dist + "\n";
                    lastDistance = 0.0;
                }
                else if (nextEdge.role() != 4) {
                    instructionsString += "Exit the roundabout towards " + nextEdge.name() + "\n";
                    lastDistance = 0.0;
                }   
                break;
            
            default:
                if (preEdge.name().equals("Unnamed way") && currentEdge.name().equals("Unnamed way") && nextEdge.name().equals("Unnamed way")){
                    preAbs += abs;
                }else {
                     if (preAbs > abs && preAbs > 0.7 && !currentEdge.name().equals("Unnamed way")){
                    abs = preAbs;
                    preAbs = 0;
                    }
                }

                if (abs > 0.7 && preEdge.role() != 4 && !currentEdge.name().equals("Unnamed way")) {
                    if (calc > 0){
                        instructionsString += "Stay at " + name + " and then turn left in " + dist + "at " + currentEdge.name() + " \n";
                    } else {
                        instructionsString += "Stay at " + name + " and then turn right in " + dist + "at " + currentEdge.name() + "\n";
                    }  
                    lastDistance = 0.0;
                }                 
                else if (preEdge.role() == 2  && currentEdge.role() != 2){
                    if (calc > 0){
                        instructionsString += "Turn left in " + dist + "at " + currentEdge.name() + "\n";
                    } else {
                        instructionsString += "Turn right in " + dist + "at " + currentEdge.name() + "\n";
                    }
                    lastDistance = 0.0;
                }
                else if (preEdge.role() == 5 && currentEdge.role() != 5) {
                    if (calc > 0){
                        instructionsString += "Stay at " + name + " and then turn left in " + dist + "at " + currentEdge.name() + "\n";
                    } else {
                        instructionsString += "Stay at " + name + " and then turn right in " + dist + "at " + currentEdge.name() + "\n";
                    }
                    lastDistance = 0.0;
                }
                else if (!preEdge.name().equals(currentEdge.name()) && !preEdge.name().equals("Unnamed way") && !currentEdge.name().equals("Unnamed way") && preEdge.role() != 4){
                    instructionsString += "Stay at " + name + " for " + dist + "and then continue on " + currentEdge.name() + "\n";
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
            return dist + " km ";
        }
    }
}


