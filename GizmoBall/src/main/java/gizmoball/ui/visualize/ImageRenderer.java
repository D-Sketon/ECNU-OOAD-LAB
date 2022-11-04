package gizmoball.ui.visualize;

import gizmoball.engine.geometry.AABB;
import gizmoball.engine.geometry.Transform;
import gizmoball.engine.geometry.shape.AbstractShape;
import gizmoball.engine.physics.PhysicsBody;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.transform.Affine;

public class ImageRenderer implements CanvasRenderer {

    private final Image image;

    public ImageRenderer(String resource){
        this(new Image(ImageRenderer.class.getClassLoader().getResourceAsStream(resource)));
    }

    public ImageRenderer(Image image) {
        this.image = upsideDown(image);
    }

    /**
     * 上下翻转图片
     * @param image /
     * @return /
     */
    public static Image upsideDown(Image image) {
        int w = (int) image.getWidth();
        int h = (int) image.getHeight();
        PixelReader pixelReader = image.getPixelReader();
        WritableImage writableImage = new WritableImage(w, h);
        PixelWriter pixelWriter = writableImage.getPixelWriter();

        for (int i = 0; i < h; i++) {
            for (int j = 0; j < w; j++) {
                pixelWriter.setArgb(j, h - 1 - i, pixelReader.getArgb(j, i));
            }
        }

        return writableImage;
    }
    @Override
    public void drawToCanvas(GraphicsContext gc, PhysicsBody body) {
        AbstractShape shape = body.getShape();
        Transform transform = shape.getTransform();
        int scale = shape.getRate();

        final int gridSize = 30; // TODO how to get gridSize or render without gridSize

        Affine affine = new Affine();
        affine.appendRotation(transform.getAngle(), transform.x, transform.y);
        gc.save();
        gc.transform(affine);
        gc.drawImage(image,
                transform.getX() - gridSize / 2.0 * scale,
                transform.getY() - gridSize / 2.0 * scale,
                gridSize * scale, gridSize * scale);
        gc.restore();
    }

}
