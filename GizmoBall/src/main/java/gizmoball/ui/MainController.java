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
import gizmoball.ui.component.*;
import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.input.*;
import javafx.scene.layout.BorderPane;
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

    private static final boolean DEV_MODE = true;

    /**
     * 游戏世界
     */
    private GridWorld world;

    /**
     * 是否处于编辑模式
     */
    private boolean inDesign = true;

    /**
     * 当前选中的组件
     */
    private PhysicsBody selectedBody;

    private GizmoOpHandler gizmoOpHandler;

    /**
     * 拖拽传参的key
     */
    private static final DataFormat GIZMO_TYPE_DATA = new DataFormat("gizmo");

    private static Vector2 preferredSize;

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

    private static final CommandComponent[] gizmoOps = {
            new CommandComponent("icons/delete.png", "delete", GizmoCommand.REMOVE),
            new CommandComponent("icons/zoom_out.png", "zoom out", GizmoCommand.ZOOM_OUT),
            new CommandComponent("icons/zoom_in.png", "zoom in", GizmoCommand.ZOOM_IN),
            new CommandComponent("icons/rotate_right.png", "rotate right", GizmoCommand.ROTATE_RIGHT),

//            new CommandComponent("icons/rotate_left.png", "rotate left", GizmoCommand.ROTATE_LEFT),
            new CommandComponent("icons/move_up.png", "move up", GizmoCommand.MOVE_UP),
            new CommandComponent("icons/move_down.png", "move down", GizmoCommand.MOVE_DOWN),
            new CommandComponent("icons/move_left.png", "move left", GizmoCommand.MOVE_LEFT),
            new CommandComponent("icons/move_right.png", "move right", GizmoCommand.MOVE_RIGHT),

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
                if (!DEV_MODE && !inDesign) {
                    return;
                }
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
        for (CommandComponent gizmoOp : gizmoOps) {
            gizmoOp.createVBox();
            gizmoOp.getImageView().setOnMouseClicked(event -> {
                if (selectedBody == null) {
                    return;
                }
                try {
                    boolean success = gizmoOpHandler.handleCommand(gizmoOp.getGizmoCommand(), selectedBody);
                    if (success){
                        if(gizmoOp.getGizmoCommand() == GizmoCommand.REMOVE) {
                            selectedBody = null;
                        }
                        drawGizmo(gizmoCanvas.getGraphicsContext2D());
                    }
                } catch (Exception e) {
                    // TODO toast
                    System.err.println(e.getMessage());
                    e.printStackTrace();
                }
            });
        }

        BorderPane borderPane = new BorderPane();
        for (int i = 4; i < gizmoOps.length; i++) {

        }

        for (CommandComponent gizmoOp : gizmoOps) {
            gizmoOpHBox.getChildren().add(gizmoOp.getVBox());
        }
    }

    private void initGameOpHBox() {
        ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        Runnable r = () -> {
            try {
                world.tick();
                drawGizmo(gizmoCanvas.getGraphicsContext2D());
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
        double worldWidth = gizmoCanvas.getWidth();
        double worldHeight = gizmoCanvas.getHeight();
        world = new GridWorld(World.SUN_GRAVITY, (int) worldWidth, (int) worldHeight, 30);
        preferredSize = new Vector2(world.getGridSize(), world.getGridSize());
        gizmoOpHandler = new GizmoOpHandler(world);

        {
            // init border
            Rectangle bottomRectangle = new Rectangle(worldWidth / 2, worldHeight / 2);
            bottomRectangle.getTransform().setX(bottomRectangle.getHalfWidth());
            bottomRectangle.getTransform().setY(-bottomRectangle.getHalfHeight());
            PhysicsBody bottomBorder = new PhysicsBody(bottomRectangle);
            bottomBorder.setMass(new Mass(new Vector2(),0.0,0.0));
            world.addBodies(bottomBorder);

            Rectangle topRectangle = new Rectangle(worldWidth / 2, worldHeight / 2);
            topRectangle.getTransform().setX(topRectangle.getHalfWidth());
            topRectangle.getTransform().setY(worldHeight + topRectangle.getHalfHeight());
            PhysicsBody topBorder = new PhysicsBody(topRectangle);
            topBorder.setMass(new Mass(new Vector2(),0.0,0.0));
            world.addBodies(topBorder);

            Rectangle leftRectangle = new Rectangle(worldWidth / 2, worldHeight / 2);
            leftRectangle.getTransform().setX(-leftRectangle.getHalfWidth());
            leftRectangle.getTransform().setY(leftRectangle.getHalfHeight());
            PhysicsBody leftBorder = new PhysicsBody(leftRectangle);
            leftBorder.setMass(new Mass(new Vector2(),0.0,0.0));
            world.addBodies(leftBorder);

            Rectangle rightRectangle = new Rectangle(worldWidth / 2, worldHeight / 2);
            rightRectangle.getTransform().setX(worldWidth + rightRectangle.getHalfWidth());
            rightRectangle.getTransform().setY(rightRectangle.getHalfHeight());
            PhysicsBody rightBorder = new PhysicsBody(rightRectangle);
            rightBorder.setMass(new Mass(new Vector2(),0.0,0.0));
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
        // 正常设置
        affine.appendScale(1, -1);
        affine.appendTranslation(0, -gizmoCanvas.getHeight());
        // 显示边界 但是拖拽对齐会有问题，调试用
//        affine.appendScale(.9, -.9);
//        affine.appendTranslation(gizmoCanvas.getWidth() * .05, -gizmoCanvas.getHeight() * 1.05);
        gc.setTransform(affine);

        // 增加拖拽监听器
        Canvas target = gizmoCanvas;
        target.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY) {
                double x = event.getX();
                double y = world.boundaryAABB.maxY - event.getY();
                // 获取当前index
                int[] index = world.getGridIndex(x, y);
                if (index != null) {
                    selectedBody = world.gizmoGridBodies[index[0]][index[1]];
                    System.out.println(selectedBody);
                }
            }
        });
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

            int gridSize = world.getGridSize();
            Vector2 transformedCenter = new Vector2(event.getX(), world.boundaryAABB.maxY - event.getY());
            // 以鼠标所在的点创建一个格子大小的AABB
            AABB centerAABB = new AABB(-gridSize / 2.0, -gridSize / 2.0, gridSize / 2.0, gridSize / 2.0);
            centerAABB.translate(transformedCenter);
            // 移到边界内
            Vector2 offsetToBoundary = GeometryUtil.offsetToBoundary(centerAABB, world.boundaryAABB);
            transformedCenter.add(offsetToBoundary);
            centerAABB.translate(offsetToBoundary);
            // 对齐到网格
            Vector2 snapped = GeometryUtil.snapToGrid(centerAABB, gridSize, gridSize);
            transformedCenter.add(snapped);

            PhysicsBody physicsBody = gizmo.createPhysicsBody(preferredSize, transformedCenter);
            physicsBody.setMass(physicsBody.getShape().createMass(10));
//            physicsBody.setMass(new Mass(new Vector2(transformedCenter), 10, 1));
            gizmoOpHandler.addGizmo(physicsBody);

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
        int gridSize = world.getGridSize();
        gc.setStroke(Color.GRAY);
        gc.setLineWidth(1);
        for (int i = 0; i < gizmoCanvas.getWidth(); i += gridSize) {
            gc.strokeLine(i, 0, i, gizmoCanvas.getHeight());
        }
        for (int i = 0; i < gizmoCanvas.getHeight(); i += gridSize) {
            gc.strokeLine(0, i, gizmoCanvas.getWidth(), i);
        }
    }

    public static Image imageMisro(Image image) {
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

    private void drawGizmo(GraphicsContext gc) {
        clearCanvas(gc);
        drawGrid(gc);

        List<PhysicsBody> bodies = world.getBodies();
        for (PhysicsBody physicsBody : bodies) {
            AbstractShape shape = physicsBody.getShape();
            Transform transform = shape.getTransform();
            if (physicsBody instanceof ImagePhysicsBody) {
                ImagePhysicsBody body = (ImagePhysicsBody) physicsBody;
                int scale = body.getShape().getRate();


                gc.save();
                int gridSize = world.getGridSize();
                Affine affine = new Affine();
                affine.appendRotation(transform.getAngle(), transform.x, transform.y);
                gc.transform(affine);
                gc.drawImage(imageMisro(body.getImage()),
                        transform.getX() - gridSize / 2.0 * scale,
                        transform.getY() - gridSize / 2.0 * scale,
                        gridSize * scale, gridSize * scale);
                gc.restore();


//                gc.drawImage(imageMisro(body.getImage()),
//                        transform.getX() - GRID_SIZE / 2.0 * scale,
//                        transform.getY() - GRID_SIZE / 2.0 * scale,
//                        GRID_SIZE * scale, GRID_SIZE * scale);

            } else if (shape instanceof Polygon) {
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
