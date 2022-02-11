package bfst22.vector;

import javafx.scene.paint.Color;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.transform.Affine;
import javafx.stage.Stage;

public class FancyView extends View {
    public FancyView(Model model, Stage stage) {
        super(model, stage);
    }

    void repaint() {
        var gc = canvas.getGraphicsContext2D();
        gc.save();
        gc.setTransform(new Affine());
        gc.setFill(Color.GREEN);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        gc.setTransform(trans);
        gc.setLineWidth(12);
        gc.setLineCap(StrokeLineCap.ROUND);
        gc.setStroke(Color.BROWN);
        for (var line : model)
            line.draw(gc);
        gc.setLineWidth(10);
        gc.setLineCap(StrokeLineCap.ROUND);
        gc.setStroke(Color.BLACK);
        for (var line : model)
            line.draw(gc);
        gc.setLineWidth(1);
        gc.setLineDashes(2, 5);
        gc.setLineCap(StrokeLineCap.SQUARE);
        gc.setStroke(Color.WHITE);
        for (var line : model)
            line.draw(gc);
        gc.restore();
    }
}
