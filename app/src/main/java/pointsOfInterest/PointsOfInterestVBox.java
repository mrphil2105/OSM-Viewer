package pointsOfInterest;

import javafx.scene.Node;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class PointsOfInterestVBox extends VBox {

    private List<PointOfInterest> pointsOfInterest;

    public void init(){
        pointsOfInterest=new ArrayList<>();
    }

    public PointsOfInterestVBox(){

    }

    public void update(List<PointOfInterest> points){
        for (PointOfInterest point : points){
            if (!pointsOfInterest.contains(point)){
                addPointOfInterest(point);
            }
        }
        for (PointOfInterest point : pointsOfInterest){
            if (!points.contains(point)){
                removePointOfInterest(point);
            }
        }



    }

    public void addPointOfInterest(PointOfInterest point){
        pointsOfInterest.add(point);
        getChildren().add(new PointsOfInterestHBox(point));

    }

    public void removePointOfInterest(PointOfInterest point){
        pointsOfInterest.remove(point);
        for (Node n :getChildren()){
            if (((PointsOfInterestHBox)n).getPointOfInterest() == point ){
                getChildren().remove(n);
            }
        }


    }




}
