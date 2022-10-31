package gizmoball.ui;

import de.codecentric.centerdevice.javafxsvg.SvgImageLoaderFactory;
import gizmoball.engine.geometry.Geometry;
import gizmoball.engine.geometry.Transform;
import gizmoball.engine.geometry.Vector2;
import gizmoball.engine.geometry.shape.AbstractShape;
import gizmoball.engine.geometry.shape.Circle;
import gizmoball.engine.geometry.shape.Polygon;
import gizmoball.engine.geometry.shape.Rectangle;
import gizmoball.engine.physics.Mass;
import gizmoball.engine.physics.PhysicsBody;
import gizmoball.engine.world.World;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.transform.Affine;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class MainController extends Application implements Initializable {

    /**
     * 渲染弹球游戏界面的canvas
     */
    @FXML
    Canvas gizmoCanvas;

    /**
     * 游戏组件的面板（球，方，角，管道...）
     */
    @FXML
    GridPane gizmoGridPane;

    /**
     * 操作游戏组件的面板（删除，缩放，旋转...）
     */
    @FXML
    HBox gizmoOpHBox;

    /**
     * 游戏操作面板（设计，开始...）
     */
    @FXML
    HBox gameOpHBox;

    private World world;

    private static final ImageLabelButton[] gizmos = {
            new ImageLabelButton("icons/rectangle.png", "rectangle"),
            new ImageLabelButton("icons/circle.png", "circle"),
            new ImageLabelButton("icons/triangle.png", "triangle"),
            new ImageLabelButton("icons/black_hole.png", "black hole"),
            new ImageLabelButton("icons/ball.png", "ball"),
            new ImageLabelButton("icons/rail.png", "rail"),
            new ImageLabelButton("icons/quarter_circle.png", "quarter circle"),
    };

    private static final ImageLabelButton[] gizmoOps = {
            new ImageLabelButton("icons/rotate_right.svg", "rotate right"),
            new ImageLabelButton("icons/delete.svg", "delete"),
            new ImageLabelButton("icons/zoom_out.svg", "zoom out"),
            new ImageLabelButton("icons/zoom_in.svg", "zoom in"),
    };

    private static final ImageLabelButton[] gameOps = {
            new ImageLabelButton("icons/play.png", "play"),
            new ImageLabelButton("icons/design.png", "design"),
    };

    private final int STAGE_WIDTH = 900;
    private final int STAGE_HEIGHT = 600;

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getClassLoader().getResource("main.fxml"));
        primaryStage.setTitle("GizmoBall");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }

    private void initGizmoGridPane() {
        for (int i = 0; i < gizmos.length; i++) {
            ImageLabelButton gizmo = gizmos[i];
            gizmoGridPane.add(gizmo.createHBox(), i % 3, i / 3);
        }
    }

    private void initGizmoOpHBox() {
        for (ImageLabelButton gizmoOp : gizmoOps) {
            gizmoOpHBox.getChildren().add(gizmoOp.createHBox());
        }
    }

    private void initGameOpHBox() {
        for (ImageLabelButton gameOp : gameOps) {
            gameOpHBox.getChildren().add(gameOp.createHBox());
        }
    }


    private void initWorld() {
        world = new World(World.EARTH_GRAVITY);
        {
            // init border
            double worldWidth = gizmoCanvas.getWidth();
            double worldHeight = gizmoCanvas.getHeight();

            Rectangle bottomRectangle = new Rectangle(worldWidth, worldHeight, new Transform());
            bottomRectangle.getTransform().setX(bottomRectangle.getWidth() / 2);
            bottomRectangle.getTransform().setY(-bottomRectangle.getHeight() / 2);
            PhysicsBody bottomBorder = new PhysicsBody(bottomRectangle);
            bottomBorder.getMass().setMass(0);
            world.addBodies(bottomBorder);

            Rectangle topRectangle = new Rectangle(worldWidth, worldHeight, new Transform());
            topRectangle.getTransform().setX(topRectangle.getWidth() / 2);
            topRectangle.getTransform().setY(worldHeight + topRectangle.getHeight() / 2);
            PhysicsBody topBorder = new PhysicsBody(topRectangle);
            bottomBorder.getMass().setMass(0);
            world.addBodies(topBorder);

            Rectangle leftRectangle = new Rectangle(worldWidth, worldHeight, new Transform());
            leftRectangle.getTransform().setX(-leftRectangle.getWidth() / 2);
            leftRectangle.getTransform().setY(leftRectangle.getHeight() / 2);
            PhysicsBody leftBorder = new PhysicsBody(leftRectangle);
            bottomBorder.getMass().setMass(0);
            world.addBodies(leftBorder);

            Rectangle rightRectangle = new Rectangle(worldWidth, worldHeight, new Transform());
            rightRectangle.getTransform().setX(worldWidth + rightRectangle.getWidth() / 2);
            rightRectangle.getTransform().setY(rightRectangle.getHeight() / 2);
            PhysicsBody rightBorder = new PhysicsBody(rightRectangle);
            bottomBorder.getMass().setMass(0);
            world.addBodies(rightBorder);
        }
        {
            // add custom item

        }
    }


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        SvgImageLoaderFactory.install();
        initWorld();
        initGizmoGridPane();
        initGizmoOpHBox();
        initGameOpHBox();


        Canvas target = gizmoCanvas;
        target.setOnDragOver(new EventHandler<DragEvent>() {
            public void handle(DragEvent event) {
                /* data is dragged over the target */
                /* accept it only if it is not dragged from the same node
                 * and if it has a string data */
                if (event.getGestureSource() != target &&
                        event.getDragboard().hasString()) {
                    /* allow for moving */
                    event.acceptTransferModes(TransferMode.MOVE);
                }

                event.consume();
            }
        });
        target.setOnDragDropped(new EventHandler<DragEvent>() {
            public void handle(DragEvent event) {
                /* data dropped */
                /* if there is a string data on dragboard, read it and use it */
                Dragboard db = event.getDragboard();
                boolean success = false;

                System.out.println(event.getX() + " " + event.getY());
                if (db.hasString()) {
                    success = true;
                }
                /* let the source know whether the string was successfully
                 * transferred and used */
                event.setDropCompleted(success);

                event.consume();
            }
        });


        GraphicsContext gc = gizmoCanvas.getGraphicsContext2D();
        Affine affine = new Affine();
        affine.appendScale(1, -1);
        affine.appendTranslation(0, -gizmoCanvas.getHeight());
        gc.setTransform(affine);


        gc.setFill(Color.RED);
        gc.fillRect(0, 0, 100, 100);
        gc.setFill(Color.BLUE);
        gc.fillOval(0, 0, 100, 100);
    }

    private void drawGizmo(GraphicsContext gc) {
        List<PhysicsBody> bodies = world.getBodies();
        for (PhysicsBody body : bodies) {
            AbstractShape shape = body.getShape();
            Transform transform = shape.getTransform();
            if (shape instanceof Polygon) {
                // 画多边形
                Polygon polygon = (Polygon) shape;
                gc.setFill(Color.RED);

                Vector2[] vertices = polygon.getVertices();
                double[] xpoints = new double[vertices.length];
                double[] ypoints = new double[vertices.length];
                for (int i = 0; i < vertices.length; i++) {
                    xpoints[i] = transform.getTransformedX(new Vector2(vertices[i].x, vertices[i].y));
                }
            } else if (shape instanceof Circle) {
                Circle circle = (Circle) shape;
                gc.setFill(Color.BLUE);
                gc.fillOval(circle.getTransform().getX(), circle.getTransform().getY(), circle.getRadius(), circle.getRadius());
            }
        }
    }
}
