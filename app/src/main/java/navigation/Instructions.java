package navigation;

import java.util.List;

import canvas.MapCanvas;
import canvas.Renderer;
import drawing.Drawable;
import drawing.Drawing;
import geometry.Point;
import geometry.Vector2D;

public class Instructions{
    private List<Point> points;
    private double preDir = 0.0;
    private static Point prePoint;
    private static MapCanvas canvas;

    public Instructions(List<Point> points, MapCanvas canvas){
        this.canvas = canvas;
        this.points = points;
        getIntructions();
    }

    public void getIntructions(){
        for (Point point : points) {
            
            if (prePoint == null){
                prePoint = points.get(0);
                var drawing = Drawing.create(new Vector2D(Point.geoToMap(point)), Drawable.POINT);
                canvas.getRenderer().draw(drawing); 
                continue;
            }
            double calc = calc((double)prePoint.y(), (double)prePoint.x(), (double)point.y(), (double)point.x());
            calc = alignOrientation(preDir, calc);
            getDir(calc - preDir);
            preDir = calc;
            prePoint = point;
        }
    }
    public static void getDir(double calc){
        System.out.println(calc);
        double abs = Math.abs(calc);
        if (abs < 0.2){
            System.out.println("continue");
            var drawing = Drawing.create(new Vector2D(Point.geoToMap(prePoint)), Drawable.POINTCONTINUE);
            canvas.getRenderer().draw(drawing); 
        }
        else if (abs < 0.8){
            if (calc > 0){
                System.out.println("slight left");
            } else {
                System.out.println("slight right");
            } 
            var drawing = Drawing.create(new Vector2D(Point.geoToMap(prePoint)), Drawable.POINTSLIGHT);
            canvas.getRenderer().draw(drawing);           
        }
        else if (abs < 1.8){
            if (calc > 0){
                System.out.println("left");
            } else {
                System.out.println("right");
            }
            var drawing = Drawing.create(new Vector2D(Point.geoToMap(prePoint)), Drawable.POINTTURN);
            canvas.getRenderer().draw(drawing);  
        }
        else if (calc > 0){
            
                System.out.println("sharp left");
                var drawing = Drawing.create(new Vector2D(Point.geoToMap(prePoint)), Drawable.POINTSHARP);
                canvas.getRenderer().draw(drawing);  
        }else {
                System.out.println("sharp right");       
                var drawing = Drawing.create(new Vector2D(Point.geoToMap(prePoint)), Drawable.POINTSHARP);
                canvas.getRenderer().draw(drawing);           
            }
        
    }

    public static double calc(double fromLat, double fromLon, double toLat, double toLon){
        return (Math.atan2(toLat-fromLat, (toLon-fromLon)));
    }
    public static double alignOrientation(double baseOrientation, double orientation) {
        double resultOrientation;
        if (baseOrientation >= 0) {
            if (orientation < -Math.PI + baseOrientation)
                resultOrientation = orientation + 2 * Math.PI;
            else
                resultOrientation = orientation;

        } else if (orientation > +Math.PI + baseOrientation)
            resultOrientation = orientation - 2 * Math.PI;
        else
            resultOrientation = orientation;
        return resultOrientation;
    }

}


