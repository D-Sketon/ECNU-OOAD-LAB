package gizmoball.ui;

import gizmoball.engine.geometry.AABB;
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
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.transform.Affine;
import javafx.stage.Stage;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

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

    /**
     * 游戏世界
     */
    private World world;

    /**
     * 是否处于编辑模式
     */
    private boolean inDesign = true;

    /**
     * 边界AABB
     */
    public AABB boundaryAABB;

    /**
     * 拖拽传参的key
     */
    private static final DataFormat GIZMO_TYPE_DATA = new DataFormat("gizmo");

    public static final int GRID_SIZE = 30;

    private static final Vector2 PREFERRED_SIZE = new Vector2(GRID_SIZE, GRID_SIZE);


    private static final DraggableGizmoComponent[] gizmos = {
            new DraggableGizmoComponent("icons/rectangle.png", "rectangle", GizmoType.RECTANGLE),
            new DraggableGizmoComponent("icons/circle.png", "circle", GizmoType.CIRCLE),
            new DraggableGizmoComponent("icons/triangle.png", "triangle", GizmoType.TRIANGLE),
            new DraggableGizmoComponent("icons/black_hole.png", "black hole", GizmoType.BLACK_HOLE),
            new DraggableGizmoComponent("icons/ball.png", "ball", GizmoType.BALL),
            new DraggableGizmoComponent("icons/rail.png", "rail", GizmoType.PIPE),
            new DraggableGizmoComponent("icons/quarter_circle.png", "quarter circle", GizmoType.CURVED_PIPE),
            // TODO 左右挡板
    };

    private static final ImageLabelComponent[] gizmoOps = {
            new ImageLabelComponent("icons/rotate_right.svg", "rotate right"),
            new ImageLabelComponent("icons/delete.svg", "delete"),
            new ImageLabelComponent("icons/zoom_out.svg", "zoom out"),
            new ImageLabelComponent("icons/zoom_in.svg", "zoom in"),
    };

    private static final ImageLabelComponent[] gameOps = {
            new ImageLabelComponent("icons/play.png", "play"),
            new ImageLabelComponent("icons/design.png", "design"),
    };

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getClassLoader().getResource("main.fxml"));
        primaryStage.setOnCloseRequest(e -> {
            System.exit(0);
        });
        primaryStage.setTitle("GizmoBall");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }

    private void initGizmoGridPane() {
        for (int i = 0; i < gizmos.length; i++) {
            DraggableGizmoComponent gizmo = gizmos[i];
            gizmoGridPane.add(gizmo.createVBox(), i % 3, i / 3);
            // 添加拖拽事件监听器
            // 拖拽传参为gizmo的类型
            int finalI = i;
            gizmo.getImageView().setOnDragDetected(event -> {
                Dragboard db = gizmo.getImageView().startDragAndDrop(TransferMode.ANY);
                db.setDragView(gizmo.getImageView().getImage());
                ClipboardContent content = new ClipboardContent();
                content.put(GIZMO_TYPE_DATA, finalI);
                db.setContent(content);
                event.consume();
            });

        }
    }

    private void initGizmoOpHBox() {
        for (ImageLabelComponent gizmoOp : gizmoOps) {
            gizmoOpHBox.getChildren().add(gizmoOp.createVBox());
        }
    }

    private void initGameOpHBox() {
        ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        Runnable r = () -> {
            try {
                world.tick();
                Platform.runLater(() -> drawGizmo(gizmoCanvas.getGraphicsContext2D()));
            } catch (Exception e) {
                e.printStackTrace();
            }

        };

        // 停止/暂停游戏
        final ScheduledFuture<?>[] scheduledFuture = new ScheduledFuture<?>[1];
        for (ImageLabelComponent gameOp : gameOps) {
            gameOpHBox.getChildren().add(gameOp.createVBox());
            gameOp.getImageView().setCursor(Cursor.HAND);
            gameOp.getImageView().setOnMouseClicked(event -> {
                if (gameOp.getLabel().getText().equals("play")) {
                    inDesign = false;
                    scheduledFuture[0] = scheduledExecutorService.scheduleAtFixedRate(r, 0, 16, TimeUnit.MILLISECONDS);
                } else {
                    inDesign = true;
                    scheduledFuture[0].cancel(true);
                }
            });
        }
    }

    /**
     * 初始化世界
     */
    private void initWorld() {
        world = new World(World.EARTH_GRAVITY);
        {
            // init border
            double worldWidth = gizmoCanvas.getWidth();
            double worldHeight = gizmoCanvas.getHeight();
            boundaryAABB = new AABB(0, 0, worldWidth, worldHeight);

            Rectangle bottomRectangle = new Rectangle(worldWidth / 2, worldHeight / 2, new Transform());
            bottomRectangle.getTransform().setX(bottomRectangle.getHalfWidth());
            bottomRectangle.getTransform().setY(-bottomRectangle.getHalfHeight());
            PhysicsBody bottomBorder = new PhysicsBody(bottomRectangle);
            bottomBorder.getMass().setMass(0);
            bottomBorder.getMass().setCenter(new Vector2());
            world.addBodies(bottomBorder);

            Rectangle topRectangle = new Rectangle(worldWidth / 2, worldHeight / 2, new Transform());
            topRectangle.getTransform().setX(topRectangle.getHalfWidth());
            topRectangle.getTransform().setY(worldHeight + topRectangle.getHalfHeight());
            PhysicsBody topBorder = new PhysicsBody(topRectangle);
            topBorder.getMass().setMass(0);
            topBorder.getMass().setCenter(new Vector2());
            world.addBodies(topBorder);

            Rectangle leftRectangle = new Rectangle(worldWidth / 2, worldHeight / 2, new Transform());
            leftRectangle.getTransform().setX(-leftRectangle.getHalfWidth());
            leftRectangle.getTransform().setY(leftRectangle.getHalfHeight());
            PhysicsBody leftBorder = new PhysicsBody(leftRectangle);
            leftBorder.getMass().setMass(0);
            leftBorder.getMass().setCenter(new Vector2());
            world.addBodies(leftBorder);

            Rectangle rightRectangle = new Rectangle(worldWidth / 2, worldHeight / 2, new Transform());
            rightRectangle.getTransform().setX(worldWidth + rightRectangle.getHalfWidth());
            rightRectangle.getTransform().setY(rightRectangle.getHalfHeight());
            PhysicsBody rightBorder = new PhysicsBody(rightRectangle);
            rightBorder.getMass().setMass(0);
            rightBorder.getMass().setCenter(new Vector2());
            world.addBodies(rightBorder);
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initWorld();
        // 初始化界面
        initGizmoGridPane();
        initGizmoOpHBox();
        initGameOpHBox();
        GraphicsContext gc = initCanvas();
    }

    //------------canvas-----------------

    private GraphicsContext initCanvas() {
        // 设置坐标系转换
        GraphicsContext gc = gizmoCanvas.getGraphicsContext2D();
        Affine affine = new Affine();
        affine.appendScale(1, -1);
        affine.appendTranslation(0, -gizmoCanvas.getHeight());
        gc.setTransform(affine);

        // 增加拖拽监听器
        Canvas target = gizmoCanvas;
        target.setOnDragOver(event -> {
            if (event.getGestureSource() != target) {
                event.acceptTransferModes(TransferMode.COPY);
            }
            event.consume();
        });
        target.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            int gizmoIndex = (int) db.getContent(GIZMO_TYPE_DATA);
            DraggableGizmoComponent gizmo = gizmos[gizmoIndex];

            Vector2 transformedCenter = new Vector2(event.getX(), boundaryAABB.maxY - event.getY());
            // 以鼠标所在的点创建一个格子大小的AABB
            AABB centerAABB = new AABB(-GRID_SIZE / 2.0, -GRID_SIZE / 2.0, GRID_SIZE / 2.0, GRID_SIZE / 2.0);
            centerAABB.translate(transformedCenter);
            // 移到边界内
            Vector2 offsetToBoundary = GeometryUtil.offsetToBoundary(centerAABB, boundaryAABB);
            transformedCenter.add(offsetToBoundary);
            centerAABB.translate(offsetToBoundary);
            // 对齐到网格
            Vector2 snapped = GeometryUtil.snapToGrid(centerAABB, GRID_SIZE, GRID_SIZE);
            transformedCenter.add(snapped);

            PhysicsBody physicsBody = gizmo.createPhysicsBody(PREFERRED_SIZE, transformedCenter);
            // todo
            physicsBody.setMass(physicsBody.getShape().createMass(10));
            System.out.println(physicsBody.getShape());
            physicsBody.setRestitution(0.5);
            //physicsBody.setRestitution(20);
            world.addBodies(physicsBody);

            drawGizmo(gc);
            event.setDropCompleted(true);
            event.consume();
        });

        drawGizmo(gc);

        return gc;
    }

    private void clearCanvas(GraphicsContext gc) {
        gc.clearRect(0, 0, gizmoCanvas.getWidth(), gizmoCanvas.getHeight());
    }

    private void drawGrid(GraphicsContext gc) {
        gc.setStroke(Color.GRAY);
        gc.setLineWidth(1);
        for (int i = 0; i < gizmoCanvas.getWidth(); i += GRID_SIZE) {
            gc.strokeLine(i, 0, i, gizmoCanvas.getHeight());
        }
        for (int i = 0; i < gizmoCanvas.getHeight(); i += GRID_SIZE) {
            gc.strokeLine(0, i, gizmoCanvas.getWidth(), i);
        }
    }

    private void drawGizmo(GraphicsContext gc) {
        clearCanvas(gc);
        drawGrid(gc);

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
                    Vector2 transformed = transform.getTransformed(vertices[i]);
                    xpoints[i] = transformed.x;
                    ypoints[i] = transformed.y;
                }
                gc.fillPolygon(xpoints, ypoints, vertices.length);
            } else if (shape instanceof Circle) {
                Circle circle = (Circle) shape;
                gc.setFill(Color.BLUE);
                gc.fillOval(circle.getTransform().getX() - circle.getRadius(),
                        circle.getTransform().getY() - circle.getRadius(),
                        circle.getRadius() * 2, circle.getRadius() * 2);
            }
        }
    }
}
