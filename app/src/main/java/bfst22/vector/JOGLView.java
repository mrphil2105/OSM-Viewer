package bfst22.vector;

import com.jogamp.newt.event.MouseEvent;
import com.jogamp.newt.event.MouseListener;
import com.jogamp.newt.javafx.NewtCanvasJFX;
import com.jogamp.newt.NewtFactory;
import com.jogamp.newt.opengl.GLWindow;
import com.jogamp.opengl.*;
import com.jogamp.opengl.util.Animator;
import javafx.application.Platform;
import javafx.geometry.Point2D;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.transform.Affine;
import javafx.stage.Stage;

import java.awt.*;
import java.nio.FloatBuffer;

public class JOGLView {
    private Animator animator;
    private FloatBuffer transformBuffer;
    private final Affine transform;

    public JOGLView(Stage stage, LinesModel model) {
        transform = new Affine();
        recalculateTransform();

        Platform.setImplicitExit(true);
        //final var display = NewtFactory.createDisplay(null, false);
        //final var screen = NewtFactory.createScreen(display, 0);
        final var window = GLWindow.create(model.getCaps());
        window.setSharedAutoDrawable(model.getSharedDrawable());
        final var canvas = new NewtCanvasJFX(window);
        final var pane = new StackPane(canvas);
        final var gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();

        // Resize canvas when pane resizes
        pane.widthProperty().addListener((observable, oldValue, newValue) -> window.setSize(newValue.intValue(), window.getHeight()));
        pane.heightProperty().addListener((observable, oldValue, newValue) -> window.setSize(window.getWidth(), newValue.intValue()));

        // Open at one quarter the size of the display
        canvas.setWidth(gd.getDisplayMode().getWidth() >> 1);
        canvas.setHeight(gd.getDisplayMode().getHeight() >> 1);

        // Stop on application close. Animator keeps running in the background otherwise.
        stage.setOnCloseRequest(event -> animator.stop());

        window.addGLEventListener(new LinesRenderer(model, this));

        stage.setTitle("OSM Viewer (OpenGL)");
        stage.setScene(new Scene(pane));
        stage.show();

        // yes yes this is controller logic, but I don't have time since it's 11:40 right now and this is due in 20 minutes
        window.addMouseListener(new MouseListener() {
            private Point2D prev;

            @Override
            public void mouseClicked(MouseEvent mouseEvent) {

            }

            @Override
            public void mouseEntered(MouseEvent mouseEvent) {

            }

            @Override
            public void mouseExited(MouseEvent mouseEvent) {

            }

            @Override
            public void mousePressed(MouseEvent mouseEvent) {
                prev = new Point2D(mouseEvent.getX(), mouseEvent.getY());
            }

            @Override
            public void mouseReleased(MouseEvent mouseEvent) {

            }

            @Override
            public void mouseMoved(MouseEvent mouseEvent) {

            }

            @Override
            public void mouseDragged(MouseEvent mouseEvent) {
                addXY(
                        (float)(mouseEvent.getX() - prev.getX()),
                        (float)(mouseEvent.getY() - prev.getY())
                );
                prev = new Point2D(mouseEvent.getX(), mouseEvent.getY());
            }

            @Override
            public void mouseWheelMoved(MouseEvent mouseEvent) {
                zoom((float)Math.pow(1.05, mouseEvent.getRotation()[1]), mouseEvent.getX(), mouseEvent.getY());
            }
        });
        window.setVisible(true);
        animator = new Animator(window);
        animator.start();
    }

    public FloatBuffer getTransformBuffer() {
        return transformBuffer;
    }

    public void zoom(float zoom, float x, float y) {
        transform.prependTranslation(-x, -y);
        transform.prependScale(zoom, zoom);
        transform.prependTranslation(x, y);
        recalculateTransform();
    }

    public void addXY(float x, float y) {
        transform.prependTranslation(x, y);
        recalculateTransform();
    }

    private void recalculateTransform() {
        // Extract column major 4x4 matrix from Affine
        transformBuffer = FloatBuffer.allocate(16);
        transformBuffer.put((float)transform.getMxx());
        transformBuffer.put((float)transform.getMxy());
        transformBuffer.put((float)transform.getMxz());
        transformBuffer.put(0);
        transformBuffer.put((float)transform.getMyx());
        transformBuffer.put((float)transform.getMyy());
        transformBuffer.put((float)transform.getMyz());
        transformBuffer.put(0);
        transformBuffer.put((float)transform.getMzx());
        transformBuffer.put((float)transform.getMzy());
        transformBuffer.put((float)transform.getMzz());
        transformBuffer.put(0);
        transformBuffer.put((float)transform.getTx());
        transformBuffer.put((float)transform.getTy());
        transformBuffer.put((float)transform.getTz());
        transformBuffer.put(1);
    }
}
