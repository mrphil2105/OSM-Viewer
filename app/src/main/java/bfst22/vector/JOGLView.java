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
import javafx.stage.Stage;

import java.awt.*;

public class JOGLView {
    private Animator animator;

    public JOGLView(Stage stage, LinesModel model) {
        Platform.setImplicitExit(true);
        final var display = NewtFactory.createDisplay(null, false);
        final var screen = NewtFactory.createScreen(display, 0);
        final var caps = new GLCapabilities(GLProfile.getMaxFixedFunc(true));

        // 8x anti-aliasing
        caps.setSampleBuffers(true);
        caps.setNumSamples(8);

        final var window = GLWindow.create(screen, caps);
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

        window.display();
        window.addGLEventListener(new LinesRenderer(model));

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
                model.addXY(
                        (float)(mouseEvent.getX() - prev.getX()),
                        (float)(mouseEvent.getY() - prev.getY())
                );
                prev = new Point2D(mouseEvent.getX(), mouseEvent.getY());
            }

            @Override
            public void mouseWheelMoved(MouseEvent mouseEvent) {
                model.zoom((float)Math.pow(1.05, mouseEvent.getRotation()[1]), mouseEvent.getX(), mouseEvent.getY());
            }
        });
        animator = new Animator(window);
        animator.start();
    }
}
