package canvas;

import com.jogamp.newt.event.MouseListener;
import com.jogamp.newt.javafx.NewtCanvasJFX;
import com.jogamp.newt.opengl.GLWindow;
import com.jogamp.opengl.util.Animator;
import java.nio.FloatBuffer;
import javafx.application.Platform;
import javafx.scene.layout.Region;
import javafx.scene.transform.Affine;

public class MapCanvas extends Region {
    private final Affine transform = new Affine();
    private Animator animator;
    private FloatBuffer transformBuffer;
    private GLWindow window;

    public void init(Model model) {
        recalculateTransform();

        // Boilerplate to let us use OpenGL from a JavaFX node hierarchy
        Platform.setImplicitExit(true);
        window = GLWindow.create(model.getCaps());
        window.setSharedAutoDrawable(model.getSharedDrawable());
        final var canvas = new NewtCanvasJFX(window);
        getChildren().add(canvas);

        // Resize window when region resizes
        widthProperty()
                .addListener(
                        (observable, oldValue, newValue) ->
                                window.setSize(newValue.intValue(), window.getHeight()));
        heightProperty()
                .addListener(
                        (observable, oldValue, newValue) ->
                                window.setSize(window.getWidth(), newValue.intValue()));

        canvas.setWidth(getPrefWidth());
        canvas.setHeight(getPrefHeight());

        // Start rendering the model
        window.addGLEventListener(new Renderer(model, this));
        animator = new Animator(window);
        animator.start();
    }

    public void dispose() {
        animator.stop();
    }

    public void addMouseListener(MouseListener mouseListener) {
        window.addMouseListener(mouseListener);
    }

    public FloatBuffer getTransformBuffer() {
        return transformBuffer;
    }

    public void zoom(float zoom, float x, float y) {
        transform.prependTranslation(-x, y);
        transform.prependScale(zoom, zoom);
        transform.prependTranslation(x, -y);
        recalculateTransform();
    }

    public void pan(float dx, float dy) {
        transform.prependTranslation(dx, -dy);
        recalculateTransform();
    }

    private void recalculateTransform() {
        // Extract column major 4x4 matrix from Affine to buffer
        transformBuffer = FloatBuffer.allocate(16);
        transformBuffer.put((float) transform.getMxx());
        transformBuffer.put((float) transform.getMyx());
        transformBuffer.put(0);
        transformBuffer.put(0);
        transformBuffer.put((float) transform.getMxy());
        transformBuffer.put((float) transform.getMyy());
        transformBuffer.put(0);
        transformBuffer.put(0);
        transformBuffer.put(0);
        transformBuffer.put(0);
        transformBuffer.put(1);
        transformBuffer.put(0);
        transformBuffer.put((float) transform.getTx());
        transformBuffer.put((float) transform.getTy());
        transformBuffer.put(0);
        transformBuffer.put(1);
    }
}
