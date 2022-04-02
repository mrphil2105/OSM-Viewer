package canvas;

public class ScaleBar {
    private final float R = 6371; //Earth radius in km


    public float getScaleBarDistance(float lon1, float lat1, float lon2, float lat2){
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


