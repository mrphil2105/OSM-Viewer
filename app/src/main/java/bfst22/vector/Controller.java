package bfst22.vector;

import javafx.geometry.Point2D;

public class Controller {
    private Point2D lastMouse;

    public Controller(Model model, View view) {
        view.canvas.setOnMousePressed(e -> {
            lastMouse = new Point2D(e.getX(), e.getY());
        });
        view.canvas.setOnMouseDragged(e -> {
            if (e.isPrimaryButtonDown()) {
                var curMouse = new Point2D(e.getX(), e.getY());
                var curModel = view.mouseToModel(curMouse);
                var lastModel = view.mouseToModel(lastMouse);
                model.add(new Line(lastModel, curModel));
            } else {
                var dx = e.getX() - lastMouse.getX();
                var dy = e.getY() - lastMouse.getY();
                view.pan(dx, dy);
            }
            lastMouse = new Point2D(e.getX(), e.getY());
        });
        view.canvas.setOnScroll(e -> {
            var factor = e.getDeltaY();
            view.zoom(Math.pow(1.05, factor), e.getX(), e.getY());
        });
    }
}
