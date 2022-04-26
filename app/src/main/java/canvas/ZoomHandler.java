package canvas;

import geometry.Point;
import geometry.Rect;

public class ZoomHandler {
    private Rect bounds;
    private MapCanvas canvas;
    private float currentScale;
    private final float R = 6371; //Earth radius in km
    private float max;
    private boolean isX;

    public ZoomHandler(Rect bounds, MapCanvas canvas){
        this.bounds = bounds;
        this.canvas = canvas;
        currentScale = (float) (getScaleBarDistance() * (100/canvas.getPrefWidth()));
        if (1280/(Point.geoToMap(bounds.getBottomRight()).x() - Point.geoToMap(bounds.getTopLeft()).x()) > 720/(Point.geoToMap(bounds.getTopLeft()).y() - Point.geoToMap(bounds.getBottomRight()).y())){
            max = (Point.geoToMap(bounds.getTopLeft()).y() - Point.geoToMap(bounds.getBottomRight()).y());
            isX = false;
        }else {
            max = (Point.geoToMap(bounds.getBottomRight()).x() - Point.geoToMap(bounds.getTopLeft()).x());
            isX = true;
        } 
    }
    
    public String getZoomString(){
        float zoom;
        if (isX){
            zoom = (20/max + 1 - ((canvas.canvasToMap(new Point(1280, 0)).x() - canvas.canvasToMap(new Point(0, 0)).x())/max))*100;
        }else {
            zoom = (20/max + 1 - ((canvas.canvasToMap(new Point(0, 720)).y() - canvas.canvasToMap(new Point(0, 0)).y())/max))*100;
        }
        return Float.toString((float) (Math.round((zoom)*10.0)/10.0)) + "%";
    }

    public float getMaxZoom(){
        if (isX){
            return 1280/(max + 20);
        } else {
            return 720/(max + 20);
        }
    }

    public float getMinZoom(){
        if (isX){
            return 1280/20;
        } else {
            return 720/20;
        }
    }
        
   public String getScaleString(){
        var newScale = (float) (currentScale *  (1/canvas.getZoom()));
        if (newScale < 1000){
            return Float.toString((float) (Math.round(newScale*100.0)/100.0)) + "m";
        } else {
            return Float.toString((float) (Math.round((newScale/1000)*100.0)/100.0)) + "km";
        } 
   }

   public float getStartZoom(){
        return (float) ((1280/(Point.geoToMap(bounds.getBottomRight()).x() - Point.geoToMap(bounds.getTopLeft()).x())));
    } 

    private float getScaleBarDistance(){
        float lon1 = bounds.getBottomLeft().x();
        float lat1 = bounds.getBottomLeft().y();
        float lon2 = bounds.getBottomRight().x();
        float lat2 = bounds.getBottomRight().y();
        //Haversine method
        var deltaLat = degreeToRadian(lat2-lat1);
        var deltaLon = degreeToRadian(lon2-lon1);
        var a = 
            Math.sin(deltaLat/2) * Math.sin(deltaLat/2) + 
            Math.cos(degreeToRadian(lat1)) * Math.cos(degreeToRadian(lat2))*
            Math.sin(deltaLon/2) * Math.sin(deltaLon/2)
            ;
        var c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        var d = R * c;
        return (float) (d * 1000); //convert to metres
    }

    private float degreeToRadian(float degree){
        return (float) (degree * (Math.PI/180));
    }
}
