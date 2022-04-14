package canvas;

import collections.enumflags.ObservableEnumFlags;
import com.jogamp.nativewindow.javafx.JFXAccessor;
import com.jogamp.newt.event.MouseEvent;
import com.jogamp.newt.event.MouseListener;
import com.jogamp.newt.event.WindowAdapter;
import com.jogamp.newt.event.WindowEvent;
import com.jogamp.newt.javafx.NewtCanvasJFX;
import com.jogamp.newt.opengl.GLWindow;
import com.jogamp.opengl.util.Animator;
import drawing.Category;
import geometry.Point;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Point2D;
import javafx.scene.layout.Region;
import javafx.scene.transform.Affine;
import javafx.scene.transform.NonInvertibleTransformException;

public class MapCanvas extends Region implements MouseListener {
    final Affine transform = new Affine();
    private Animator animator;
    private GLWindow window;
    private Renderer renderer;
    private Point2D lastMouse;

    // TODO: Add all if necessary
    public final ObjectProperty<EventHandler<MouseEvent>> mapMouseClickedProperty =
            new SimpleObjectProperty<>();
    public final ObjectProperty<EventHandler<MouseEvent>> mapMouseMovedProperty =
            new SimpleObjectProperty<>();

    public final ObservableEnumFlags<Category> categories = new ObservableEnumFlags<>();

    public void init(Model model) {
        // Boilerplate to let us use OpenGL from a JavaFX node hierarchy
        Platform.setImplicitExit(true);
        window = GLWindow.create(model.getCaps());
        window.setSharedAutoDrawable(model.getSharedDrawable());
        final var canvas = new NewtCanvasJFX(window);
        getChildren().add(canvas);

        // Ugly hack to fix focus
        // https://forum.jogamp.org/NewtCanvasJFX-not-giving-up-focus-td4040705.html
        window.addWindowListener(
                new WindowAdapter() {
                    @Override
                    public void windowGainedFocus(WindowEvent e) {
                        // when heavyweight window gains focus, also tell javafx to give focus to glCanvas
                        canvas.requestFocus();
                    }
                });

        canvas
                .focusedProperty()
                .addListener(
                        (observable, oldValue, newValue) -> {
                            if (!newValue) {
                                JFXAccessor.runOnJFXThread(false, this::giveFocus);
                            }
                        });

        // Resize window when region resizes
        widthProperty()
                .addListener(
                        (observable, oldValue, newValue) ->
                                window.setSize(Math.max(1, newValue.intValue()), window.getHeight()));
        heightProperty()
                .addListener(
                        (observable, oldValue, newValue) ->
                                window.setSize(window.getWidth(), Math.max(1, newValue.intValue())));

        canvas.setWidth(getPrefWidth());
        canvas.setHeight(getPrefHeight());

        window.addMouseListener(this);

        // Start rendering the model
        renderer = new Renderer(model, this);
        window.addGLEventListener(renderer);
        animator = new Animator(window);
        animator.start();
    }

    public void setShader(Renderer.Shader shader) {
        renderer.setShader(shader);
    }

    public void dispose() {
        animator.stop();
    }

    public Point canvasToMap(Point point) {
        try {
            return new Point(transform.inverseTransform(point.x(), point.y()));
        } catch (NonInvertibleTransformException e) {
            throw new RuntimeException(e);
        }
    }

    public void zoom(float zoom, float x, float y) {
        transform.prependTranslation(-x, -y);
        transform.prependScale(zoom, zoom);
        transform.prependTranslation(x, y);
    }

    public void pan(float dx, float dy) {
        transform.prependTranslation(dx, dy);
    }

    public void center(Point center) {
        transform.setTx(-center.x() * transform.getMxx() + getWidth() / 2);
        transform.setTy(-center.y() * transform.getMyy() + getHeight() / 2);
    }

    public void zoomTo(Point point) {
        setZoom(25);
        center(point);
    }

    public void zoomTo(Point point, float zoom) {
        setZoom(zoom);
        center(point);
    }

    public void setZoom(float zoom) {
        transform.setMxx(zoom);
        transform.setMyy(zoom);
    }

    public void giveFocus() {
        if (window.isVisible()) {
            window.setVisible(false);
            window.setVisible(true);
        }
    }

    public Renderer getRenderer() {
        return renderer;
    }

    @Override
    public void mouseClicked(MouseEvent mouseEvent) {
        handle(mapMouseClickedProperty, mouseEvent);
    }

    @Override
    public void mouseEntered(MouseEvent mouseEvent) {}

    @Override
    public void mouseExited(MouseEvent mouseEvent) {}

    @Override
    public void mousePressed(MouseEvent mouseEvent) {
        lastMouse = new Point2D(mouseEvent.getX(), mouseEvent.getY());
    }

    @Override
    public void mouseReleased(MouseEvent mouseEvent) {}

    @Override
    public void mouseMoved(MouseEvent mouseEvent) {
        handle(mapMouseMovedProperty, mouseEvent);
    }

    @Override
    public void mouseDragged(MouseEvent mouseEvent) {
        pan(
                (float) (mouseEvent.getX() - lastMouse.getX()),
                (float) (mouseEvent.getY() - lastMouse.getY()));
        lastMouse = new Point2D(mouseEvent.getX(), mouseEvent.getY());
    }

    @Override
    public void mouseWheelMoved(MouseEvent mouseEvent) {
        if (mouseEvent.getRotation()[0] == 0.0) {
            zoom(
                    (float) Math.pow(1.05, mouseEvent.getRotation()[1]),
                    mouseEvent.getX(),
                    mouseEvent.getY());
        } else {
            zoom(
                    (float) Math.pow(1.15, mouseEvent.getRotation()[0]),
                    mouseEvent.getX(),
                    mouseEvent.getY());
        }
    }

    private <T> void handle(ObjectProperty<EventHandler<T>> prop, T event) {
        if (prop.get() != null) {
            prop.get().handle(event);
        }
    }
}
