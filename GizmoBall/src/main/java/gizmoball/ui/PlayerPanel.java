package gizmoball.ui;

import gizmoball.engine.AbstractWorld;
import gizmoball.engine.Settings;
import gizmoball.engine.geometry.AABB;
import gizmoball.engine.geometry.Transform;
import gizmoball.engine.geometry.Vector2;
import gizmoball.engine.geometry.shape.AbstractShape;
import gizmoball.engine.geometry.shape.Polygon;
import gizmoball.engine.physics.PhysicsBody;
import gizmoball.game.entity.Ball;
import gizmoball.game.entity.Flipper;
import gizmoball.ui.component.*;
import gizmoball.ui.visualize.DefaultCanvasRenderer;
import gizmoball.ui.visualize.GizmoPhysicsBody;
import javafx.application.Application;
import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.transform.Affine;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Pair;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Slf4j
public class PlayerPanel extends Application implements Initializable {

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
    HBox upperHBox;

    @FXML
    HBox lowerHBox;

    @FXML
    javafx.scene.shape.Rectangle gizmoOutlineRectangle;

    @FXML
    MenuItem menuItemLoad;

    @FXML
    MenuItem menuItemSave;

    @FXML
    MenuItem menuItemClear;

    @FXML
    MenuItem menuItemAbout;

    @FXML
    ImageView previewImageView;

    @FXML
    AnchorPane anchorPane;

    /**
     * 区别于生产模式
     */
    private static final boolean DEV_MODE = false;

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
    private GizmoPhysicsBody selectedBody;

    private GizmoOpHandler gizmoOpHandler;

    /**
     * 拖拽传参的key
     */
    private static final DataFormat GIZMO_TYPE_DATA = new DataFormat("gizmo");

    private static Vector2 preferredSize;

    private Stage primaryStage;

    /**
     * 线程相关
     */
    private final ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();

    private final ScheduledFuture<?>[] scheduledFuture = new ScheduledFuture<?>[1];

    private Runnable r;

    private static final DraggableGizmoComponent[] gizmos = {
            new DraggableGizmoComponent("icons/rectangle.png", "rectangle", GizmoType.RECTANGLE),
            new DraggableGizmoComponent("icons/circle.png", "circle", GizmoType.CIRCLE),
            new DraggableGizmoComponent("icons/triangle.png", "triangle", GizmoType.TRIANGLE),
            new DraggableGizmoComponent("icons/black_hole.png", "black hole", GizmoType.BLACK_HOLE),
            new DraggableGizmoComponent("icons/ball.png", "ball", GizmoType.BALL),
            new DraggableGizmoComponent("icons/pipe.png", "pipe", GizmoType.PIPE),
            new DraggableGizmoComponent("icons/curved_pipe.png", "curved pipe", GizmoType.CURVED_PIPE),
            new DraggableGizmoComponent("icons/left_flipper.png", "left flipper", GizmoType.LEFT_FLIPPER),
            new DraggableGizmoComponent("icons/right_flipper.png", "right flipper", GizmoType.RIGHT_FLIPPER),
    };

    private static final CommandComponent[] gizmoOps = {
            new CommandComponent("icons/delete.png", "delete", GizmoCommand.REMOVE),
            new CommandComponent("icons/zoom_out.png", "zoom out", GizmoCommand.ZOOM_OUT),
            new CommandComponent("icons/zoom_in.png", "zoom in", GizmoCommand.ZOOM_IN),
            new CommandComponent("icons/rotate_right.png", "rotate right", GizmoCommand.ROTATE_RIGHT),

            new CommandComponent("icons/move_up.png", "move up", GizmoCommand.MOVE_UP),
            new CommandComponent("icons/move_right.png", "move right", GizmoCommand.MOVE_RIGHT),
            new CommandComponent("icons/move_down.png", "move down", GizmoCommand.MOVE_DOWN),
            new CommandComponent("icons/move_left.png", "move left", GizmoCommand.MOVE_LEFT),
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
        primaryStage.getIcons().add(new Image("icons/ball.png"));
        primaryStage.setTitle("GizmoBall");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
        this.primaryStage = primaryStage;
        root.requestFocus();
    }

    private void initGizmoGridPane() {
        for (int i = 0; i < gizmos.length; i++) {
            DraggableGizmoComponent gizmo = gizmos[i];
            gizmoGridPane.add(gizmo.createVBox(), i % 3, i / 3); // 3行3列
            // 添加拖拽事件监听器
            // 拖拽传参为gizmo的类型
            int finalI = i;
            gizmo.getImageWrapper().setOnDragDetected(event -> {
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

    private void initGizmoOp() {
        // 初始物件操作
        for (CommandComponent gizmoOp : gizmoOps) {
            gizmoOp.createVBox().setMaxWidth(70);
            gizmoOp.getImageWrapper().setOnMouseClicked(event -> {
                bindGizmoOp(gizmoOp.getGizmoCommand());
            });
        }
    }

    private void bindGizmoOp(GizmoCommand command) {
        if (selectedBody == null || !inDesign) {
            return;
        }
        try {
            boolean success = gizmoOpHandler.handleCommand(command, selectedBody);
            if (success) {
                if (command == GizmoCommand.REMOVE) {
                    selectedBody = null;
                }
                highlightSelectedBody();
                drawGizmo(gizmoCanvas.getGraphicsContext2D());
            }
        } catch (Exception e) {
            Toast.makeText(primaryStage, "操作物件失败: " + e.getMessage(), 2000, 500, 500);
            log.error("操作物件失败: {}", e.getMessage());
        }
    }

    private void initGameOpHBox() {
        r = () -> {
            try {
                world.tick();
                Platform.runLater(() -> drawGizmo(gizmoCanvas.getGraphicsContext2D()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        };

        // 开始游戏
        ImageLabelComponent play = gameOps[0];
        play.createVBox();
        play.getImageWrapper().setOnMouseClicked(event -> {
            startGame();
        });

        // 暂停游戏（设计模式）
        ImageLabelComponent design = gameOps[1];
        design.createVBox();
        design.getImageWrapper().setOnMouseClicked(event -> {
            designGame();
        });
    }

    private void startGame() {
        if (!inDesign) {
            return;
        }
        selectedBody = null;
        highlightSelectedBody();
        inDesign = false;
        world.snapshot();
        scheduledFuture[0] = scheduledExecutorService.scheduleAtFixedRate(r, 0, (long) (1000.0 / Settings.TICKS_PER_SECOND),
                TimeUnit.MILLISECONDS);
    }

    private void designGame() {
        if (inDesign) {
            return;
        }
        selectedBody = null;
        highlightSelectedBody();
        inDesign = true;
        scheduledFuture[0].cancel(true);

        try {
            world.restore();
        } catch (RuntimeException e) {
            Toast.makeText(primaryStage, e.getMessage(), 2000, 500, 500);
            log.error("恢复游戏失败: {}", e.getMessage());
        }
        drawGizmo(gizmoCanvas.getGraphicsContext2D());
    }

    /**
     * 初始化游戏操作按钮
     */
    private void initGizmoOpHBox() {
        // 初始物件操作
        initGizmoOp();
        // 初始化游戏操作（开始，设计）
        initGameOpHBox();

        for (int i = 0; i < 4; i++) {
            upperHBox.getChildren().add(gizmoOps[i].getVBox());
        }

        upperHBox.setSpacing(20);
        lowerHBox.getChildren().add(gameOps[0].getVBox());

        BorderPane borderPane = new BorderPane();
        BorderPane.setAlignment(gizmoOps[4].getImageWrapper(), Pos.CENTER);
        BorderPane.setAlignment(gizmoOps[5].getImageWrapper(), Pos.CENTER_RIGHT);
        BorderPane.setAlignment(gizmoOps[6].getImageWrapper(), Pos.CENTER);
        BorderPane.setAlignment(gizmoOps[7].getImageWrapper(), Pos.CENTER_LEFT);

        borderPane.setTop(gizmoOps[4].getImageWrapper());
        borderPane.setRight(gizmoOps[5].getImageWrapper());
        borderPane.setBottom(gizmoOps[6].getImageWrapper());
        borderPane.setLeft(gizmoOps[7].getImageWrapper());

        Pane pane = new Pane();
        pane.setPrefWidth(60);
        pane.setPrefHeight(60);
        borderPane.setCenter(pane);
        lowerHBox.getChildren().add(borderPane);

        lowerHBox.getChildren().add(gameOps[1].getVBox());
    }

    /**
     * 初始化世界
     */
    private void initWorld() {
        double worldWidth = gizmoCanvas.getWidth();
        double worldHeight = gizmoCanvas.getHeight();
        world = new GridWorld(AbstractWorld.EARTH_GRAVITY, (int) worldWidth, (int) worldHeight, 30);
        preferredSize = new Vector2(world.getGridSize(), world.getGridSize());
        gizmoOpHandler = new GizmoOpHandler(world);
    }

    private void initMenuItem() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("选择文件");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Gizmo", "*.json"),
                new FileChooser.ExtensionFilter("All Files", "*.*"));
        final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH.mm.ss");

        menuItemLoad.setOnAction(event -> {
            if (!inDesign) return;
            fileChooser.setInitialDirectory(new File("."));
            File file = fileChooser.showOpenDialog(primaryStage);
            if (file != null) {
                try {
                    world.restore(file);
                    drawGizmo(gizmoCanvas.getGraphicsContext2D());
                } catch (Exception e) {
                    Toast.makeText(primaryStage, "加载文件失败: " + e.getMessage(), 1500, 200, 200);
                    log.error("加载文件失败: {}", e.getMessage());
                }
            }
        });

        menuItemSave.setOnAction(event -> {
            if (!inDesign) return;
            // set current time as filename
            String time = LocalDateTime.now().format(formatter);

            fileChooser.setInitialFileName("gizmo" + time + ".json");
            File file = fileChooser.showSaveDialog(primaryStage);
            if (file != null) {
                try {
                    world.snapshot(file);
                } catch (Exception e) {
                    Toast.makeText(primaryStage, "保存文件失败: " + e.getMessage(), 1500, 200, 200);
                    log.error("保存文件失败: {}", e.getMessage());
                }
            }
        });

        menuItemClear.setOnAction(event -> {
            //判断处于设计模式
            if (inDesign) {
                world.removeAllBodies();
                world.initBoundary();
                drawGizmo(gizmoCanvas.getGraphicsContext2D());
            }
        });

        menuItemAbout.setOnAction(event -> {
            if (!inDesign) {
                return;
            }
            Dialog<Pair<String, String>> dialog = new Dialog<>();
            dialog.setTitle("About");
            Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
            stage.getIcons().add(new Image("icons/ball.png"));

            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new Insets(10, 10, 10, 10));

            Label content0 = new Label("ECNU OOAD Lab3 Group 07");
            Label content1 = new Label("Gizmo Ball powered by Javafx");

            Label githubRepository = new Label("Github repository:");
            Hyperlink repository = new Hyperlink("https://github.com/D-Sketon/ECNU-OOAD-LAB");
            repository.setOnAction(e -> {
                HostServices hostServices = PlayerPanel.this.getHostServices();
                hostServices.showDocument(repository.getText());
            });
            Label author = new Label("Author:");

            Hyperlink author0 = new Hyperlink("D-Sketon");
            author0.setOnAction(e -> {
                HostServices hostServices = PlayerPanel.this.getHostServices();
                hostServices.showDocument("https://github.com/D-Sketon");
            });

            Hyperlink author1 = new Hyperlink("Uzemiu");
            author1.setOnAction(e -> {
                HostServices hostServices = PlayerPanel.this.getHostServices();
                hostServices.showDocument("https://github.com/Uzemiu");
            });

            Hyperlink author2 = new Hyperlink("re20051");
            author2.setOnAction(e -> {
                HostServices hostServices = PlayerPanel.this.getHostServices();
                hostServices.showDocument("https://github.com/re20051");
            });
            grid.add(content0, 1, 0);
            grid.add(content1, 1, 2);
            grid.add(githubRepository, 0, 3);
            grid.add(repository, 1, 3);
            grid.add(author, 0, 4);
            grid.add(author0, 1, 4);
            grid.add(author1, 1, 5);
            grid.add(author2, 1, 6);
            dialog.getDialogPane().setContent(grid);

            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK);
            dialog.showAndWait();

        });
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initWorld();
        // 初始化界面
        initGizmoGridPane();
        initGizmoOpHBox();
        initCanvas();
        initMenuItem();

        anchorPane.setOnMouseClicked(event -> {
            gizmoCanvas.requestFocus();
        });
        anchorPane.setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case LEFT:
                    world.flipperUp(Flipper.Direction.LEFT);
                    break;
                case RIGHT:
                    world.flipperUp(Flipper.Direction.RIGHT);
                    break;
                case F1:
                    isDebugMode = !isDebugMode;
                    break;
                case DELETE:
                    bindGizmoOp(GizmoCommand.REMOVE);
                    break;
                case W:
                    bindGizmoOp(GizmoCommand.MOVE_UP);
                    break;
                case A:
                    bindGizmoOp(GizmoCommand.MOVE_LEFT);
                    break;
                case S:
                    bindGizmoOp(GizmoCommand.MOVE_DOWN);
                    break;
                case D:
                    bindGizmoOp(GizmoCommand.MOVE_RIGHT);
                    break;
                case SHIFT:
                    bindGizmoOp(GizmoCommand.ROTATE_LEFT);
                    break;
                case CONTROL:
                    bindGizmoOp(GizmoCommand.ROTATE_RIGHT);
                    break;
                case ENTER:
                    if (inDesign) {
                        startGame();
                    } else {
                        designGame();
                    }
                    break;
            }
        });

        anchorPane.setOnKeyReleased(event -> {
            switch (event.getCode()) {
                case LEFT:
                    world.flipperDown(Flipper.Direction.LEFT);
                    break;
                case RIGHT:
                    world.flipperDown(Flipper.Direction.RIGHT);
                    break;
            }
        });

        anchorPane.setOnScroll(event -> {
            double deltaY = event.getDeltaY();
            if (deltaY > 0) {
                bindGizmoOp(GizmoCommand.ZOOM_OUT);
            } else if (deltaY < 0) {
                bindGizmoOp(GizmoCommand.ZOOM_IN);
            }
        });
    }

    // ------------canvas-----------------

    /**
     * 高亮当前选中物体
     */
    protected void highlightSelectedBody() {
        if (selectedBody == null) {
            gizmoOutlineRectangle.setVisible(false);
            return;
        }
        AABB aabb = selectedBody.getShape().createAABB();
        GeometryUtil.padToSquare(aabb);
        gizmoOutlineRectangle.setX(aabb.minX);
        gizmoOutlineRectangle.setY(world.boundaryAABB.maxY - aabb.maxY);
        gizmoOutlineRectangle.setWidth(aabb.maxX - aabb.minX);
        gizmoOutlineRectangle.setHeight(aabb.maxY - aabb.minY);
        gizmoOutlineRectangle.setVisible(true);
    }

    private void initCanvas() {
        // 设置坐标系转换
        GraphicsContext gc = gizmoCanvas.getGraphicsContext2D();
        Affine affine = new Affine();
        // 正常设置
        affine.appendScale(1, -1);
        affine.appendTranslation(0, -gizmoCanvas.getHeight());
        gc.setTransform(affine);

        // 增加拖拽监听器
        Canvas target = gizmoCanvas;
        target.setOnMouseClicked(event -> {
            target.requestFocus();
            if (event.getButton() == MouseButton.PRIMARY) {
                double x = event.getX();
                double y = world.boundaryAABB.maxY - event.getY();
                // 获取当前index
                int[] index = world.getGridIndex(x, y);
                if (index != null) {
                    selectedBody = world.gizmoGridBodies[index[0]][index[1]];
                    highlightSelectedBody();
                }
            }
        });
        // 拖到画布上时显示物件预览/能否拖拽
        target.setOnDragOver(event -> {
            if (event.getGestureSource() != target) {

                Dragboard db = event.getDragboard();
                int gizmoIndex = (int) db.getContent(GIZMO_TYPE_DATA);
                DraggableGizmoComponent gizmo = gizmos[gizmoIndex];

                // 显示预览图片
                double x = event.getX();
                double y = event.getY();
                int[] index = world.getGridIndex(x, y);
                if (index != null) {
                    PhysicsBody[][] gridBodies = world.gizmoGridBodies;
                    int i = index[0];
                    int j = gridBodies[0].length - index[1] - 1;
                    if (gridBodies[i][j] == null) {
                        previewImageView.setVisible(true);
                        previewImageView.setImage(gizmo.getImage());
                        previewImageView.setLayoutX(index[0] * world.getGridSize());
                        previewImageView.setLayoutY(index[1] * world.getGridSize());
                        event.acceptTransferModes(TransferMode.COPY);
                    }
                }
            }
            event.consume();
        });
        target.setOnDragExited(event -> {
            previewImageView.setVisible(false);
            event.consume();
        });
        target.setOnDragDropped(event -> {
            if (!DEV_MODE && !inDesign) {
                return;
            }
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
            GizmoPhysicsBody physicsBody = gizmo.createPhysicsBody(preferredSize, transformedCenter);
            try {
                gizmoOpHandler.addGizmo(physicsBody);
            } catch (Exception e) {
                Toast.makeText(primaryStage, e.getMessage(), 1500, 200, 200);
            }

            drawGizmo(gc);
            previewImageView.setVisible(false);
            event.setDropCompleted(true);
            event.consume();
        });

        drawGizmo(gc);
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

    private static final DefaultCanvasRenderer canvasRenderer = DefaultCanvasRenderer.INSTANCE;

    private void drawGizmo(GraphicsContext gc) {
        clearCanvas(gc);
        drawGrid(gc);

        List<GizmoPhysicsBody> bodies = world.getBodies();
        for (GizmoPhysicsBody physicsBody : bodies) {
            if (physicsBody instanceof GizmoPhysicsBody) {
                physicsBody.drawToCanvas(gc);
                if (isDebugMode) {
                    if (physicsBody.getShape() instanceof Ball) {
                        Vector2 linearVelocity = physicsBody.getLinearVelocity().copy();
                        Vector2 normalized = linearVelocity.copy().right().getNormalized();
                        double angularVelocity = physicsBody.getAngularVelocity() * 20;
                        Transform transform = physicsBody.getShape().getTransform();
                        gc.setStroke(Color.GREEN);
                        gc.strokeLine(transform.x, transform.y, transform.x + linearVelocity.x, transform.y + linearVelocity.y);
                        gc.setStroke(Color.RED);
                        gc.strokeLine(transform.x, transform.y, transform.x + normalized.x * angularVelocity, transform.y + normalized.y * angularVelocity);
                    } else {
                        AbstractShape shape = physicsBody.getShape();
                        if (shape instanceof Polygon) {
                            Polygon shape1 = (Polygon) shape;
                            Vector2[] normals = shape1.getNormals();
                            Transform transform = physicsBody.getShape().getTransform();
                            for (Vector2 normal : normals) {
                                Vector2 multiply = normal.copy().multiply(30);
                                Vector2 transformed = transform.getTransformed(multiply);
                                gc.setStroke(Color.YELLOW);
                                gc.strokeLine(transform.x, transform.y, transformed.x, transformed.y);
                            }
                        }
                    }
                }
            } else {
                canvasRenderer.drawToCanvas(gc, physicsBody);
            }
        }
    }

    private boolean isDebugMode = false;
}
