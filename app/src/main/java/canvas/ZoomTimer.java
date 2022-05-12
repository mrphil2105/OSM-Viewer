package canvas;

import javafx.scene.transform.Affine;

import java.util.Timer;
import java.util.TimerTask;

public class ZoomTimer extends TimerTask {

    Affine transform;
    float finalZoom;
    float increments;
    Timer timer;

    public ZoomTimer(Affine transform, float finalZoom, float increments, Timer timer){
        this.transform=transform;
        this.finalZoom=finalZoom;
        this.increments=increments;
        this.timer=timer;

    }

    @Override
    public void run() {
        System.out.println("zoom");
        transform.setMxx(finalZoom);
        transform.setMyy(finalZoom);
        if (transform.getMxx()>=finalZoom){
            timer.cancel();
        }
    }
}
