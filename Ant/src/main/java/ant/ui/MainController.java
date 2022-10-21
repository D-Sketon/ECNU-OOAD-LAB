package ant.ui;

import ant.game.core.GameStatus;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.TranslateTransition;
import javafx.application.Application;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.*;

public class MainController extends Application {

    private final int COUNT = 5;

    private final GridPane gridPane;
    private final StackPane stackPane;

    private final ComboBox<Double> comboBox;
    private final Button button;
    private final Button stopButton;
    private final TextField minTimeText;
    private final TextField maxTimeText;
    private final Text completion;
    private final Text ticks;
    private final RadioButton radioSingle;
    private final RadioButton radioAll;

    private final List<ImageView> antsList;
    private final List<Text> positionList;
    private final List<ImageView> crashList;
    private final Queue<String> collisionMessageQueue;
    // 用于延迟处理碰撞
    private boolean collisionDelayFlag;

    // 手动/自动模式
    private int mode;
    private GameStatus gameStatus;
    private final int[] direction;
    private double speed;
    private final int[] initPos = {-360, -210, -120, 30, 300};
    private final int[] initRealPos = {30, 80, 110, 160, 250};
    private final String[] initUrl = {"red.png", "blue.png", "brown.png", "green.png", "black.png"};

    private final PlayRoom uiPlayRoom;

    public MainController() {
        this.uiPlayRoom = new PlayRoom(this);
        this.gridPane = new GridPane();
        this.stackPane = new StackPane();

        this.antsList = new ArrayList<>();
        this.positionList = new ArrayList<>();
        this.crashList = new ArrayList<>();
        this.collisionMessageQueue = new LinkedList<>();
        this.collisionDelayFlag = false;
        this.gameStatus = GameStatus.READY;

        List<Double> list = Arrays.asList(0.5, 1.0, 1.5, 2.0, 4.0);
        ObservableList<Double> obList = FXCollections.observableArrayList(list);
        this.comboBox = new ComboBox<>(obList);

        this.button = new Button("开始");
        this.stopButton = new Button("停止");
        this.minTimeText = new TextField("0");
        this.maxTimeText = new TextField("0");
        this.completion = new Text("0");
        this.ticks = new Text("0");
        this.direction = new int[]{1, 1, 1, 1, 1};
        this.speed = 1.0;
        this.mode = 0;
        this.radioSingle = new RadioButton("自选情形");
        this.radioSingle.setUserData("1");
        this.radioAll = new RadioButton("运行全部");
        this.radioAll.setUserData("0");
    }

    @Override
    public void start(Stage primaryStage) {
        // 程序基本信息
        primaryStage.getIcons().add(new Image("blue.png"));
        primaryStage.setTitle("蚂蚁爬杆");

        // 程序关闭时应该结束所有线程
        primaryStage.setOnCloseRequest(event -> System.exit(0));

        gridPane.setHgap(20);
        gridPane.setVgap(30);
        gridPane.setPrefSize(1280, 800);
        gridPane.setStyle("-fx-background-image: url(background.png);-fx-font-size: 18;-fx-font-weight: bold;");

        Label antSpeed = new Label("倍速");
        gridPane.add(antSpeed, 3, 3);

        comboBox.setMinWidth(100);
        comboBox.getSelectionModel().select(1);
        comboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (gameStatus != GameStatus.RUNNING)
                speed = newValue;
        });
        gridPane.add(comboBox, 4, 3);

        button.setMinWidth(100);
        button.setOnMouseClicked(event -> runGame());
        gridPane.add(button, 6, 3);
        stopButton.setMinWidth(100);
        stopButton.setDisable(true);
        stopButton.setOnMouseClicked(event -> terminateGame());
        gridPane.add(stopButton, 6, 4);

        Label minTime = new Label("最小时间");
        gridPane.add(minTime, 3, 7);

        Label maxTime = new Label("最大时间");
        gridPane.add(maxTime, 5, 7);

        minTimeText.setEditable(false);
        minTimeText.setMaxWidth(100);
        gridPane.add(minTimeText, 4, 7);

        maxTimeText.setEditable(false);
        maxTimeText.setMaxWidth(100);
        gridPane.add(maxTimeText, 6, 7);

        radioAll.setSelected(true);
        gridPane.add(radioSingle, 3, 5);
        gridPane.add(radioAll, 5, 5);
        ToggleGroup radioGroup = new ToggleGroup();
        radioSingle.setToggleGroup(radioGroup);
        radioAll.setToggleGroup(radioGroup);

        radioGroup.selectedToggleProperty().addListener(
                (ObservableValue<? extends Toggle> ov, Toggle old_Toggle,
                 Toggle new_Toggle) -> {
                    if (radioGroup.getSelectedToggle() != null) {
                        mode = Integer.parseInt((String) radioGroup.getSelectedToggle().getUserData());
                    }
                });

        Label finish = new Label("当前轮次");
        gridPane.add(finish, 12, 3);
        completion.setWrappingWidth(60);
        gridPane.add(completion, 14, 3);

        Label timeticker = new Label("当前时间");
        gridPane.add(timeticker, 12, 5);
        ticks.setWrappingWidth(60);
        gridPane.add(ticks, 14, 5);

        initAntUi();

        gridPane.add(stackPane, 4, 14, 20, 1);
        Image image = new Image("rod.png");
        ImageView bar = new ImageView(image);
        bar.setFitHeight(10);
        bar.setFitWidth(900);
        stackPane.getChildren().add(bar);

        Scene scene = new Scene(gridPane, 1280, 800);

        primaryStage.setScene(scene);
        primaryStage.setWidth(1280);
        primaryStage.setHeight(800);
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    private void initAntUi() {
        for (int i = 0; i < COUNT; i++) {
            Image ant = new Image(initUrl[i]);
            ImageView antImageView = new ImageView(ant);
            antImageView.setFitHeight(30);
            antImageView.setFitWidth(30);
            antImageView.setTranslateX(initPos[i]);
            antImageView.setTranslateY(-20);
            int finalI = i;
            antImageView.setOnMouseClicked(event -> turnoverHandler(finalI));
            antsList.add(antImageView);
            stackPane.getChildren().add(antImageView);

            Label antPosition = new Label(i + 1 + "号蚂蚁");
            gridPane.add(antPosition, 18, 3 + i);

            Text positionText = new Text(String.valueOf(initRealPos[i]));
            positionList.add(positionText);
            gridPane.add(positionText, 22, 3 + i);

            Image crash = new Image("crash.png");
            ImageView crashImageView = new ImageView(crash);
            crashImageView.setFitHeight(40);
            crashImageView.setFitWidth(50);
            crashImageView.setTranslateX(antImageView.getTranslateX());
            crashImageView.setTranslateY(antImageView.getTranslateY() - 20);
            crashImageView.setOpacity(0);
            crashList.add(crashImageView);
            stackPane.getChildren().add(crashImageView);
        }
    }

    /**
     * 用于在手动模式下控制蚂蚁的方向
     *
     * @param id 蚂蚁id
     */
    private void turnoverHandler(int id) {
        //手动模式
        if (mode == 0)
            return;
        //运行状态下不可改变蚂蚁方向
        if (gameStatus == GameStatus.RUNNING)
            return;
        // 发送消息，第i个蚂蚁要掉头
        double rotate = antsList.get(id).getRotate();
        int newRotate = ((int) rotate + 180) % 360;
        antsList.get(id).setRotate(newRotate);
        direction[id] *= -1;
    }

    /**
     * 启动游戏
     */
    private void runGame() {
        comboBox.setDisable(true);
        radioAll.setDisable(true);
        radioSingle.setDisable(true);
        stopButton.setDisable(false);
        if (gameStatus == GameStatus.RUNNING) {
            button.setText("继续");
            gameStatus = GameStatus.SUSPENDED;
            uiPlayRoom.suspendGame();
        } else if (gameStatus == GameStatus.SUSPENDED) {
            gameStatus = GameStatus.RUNNING;
            uiPlayRoom.resumeGame();
            button.setText("暂停");
        } else {
            this.gameStatus = GameStatus.RUNNING;
            button.setText("暂停");
            if (mode == 1)
                uiPlayRoom.newSingleGame(speed, direction);
            else
                uiPlayRoom.newGame(speed);
        }
    }

    /**
     * 终止游戏
     */
    private void terminateGame() {
        // 暂停状态下不可直接终止游戏
        if (gameStatus == GameStatus.SUSPENDED) {
            uiPlayRoom.resumeGame();
        }
        uiPlayRoom.terminateGame();
        button.setText("开始");
        stopButton.setDisable(true);
        minTimeText.setText("0");
        maxTimeText.setText("0");
    }

    /**
     * 碰撞处理器，用于处理消息队列中的碰撞请求
     */
    private void collisionHandler() {
        while (!collisionMessageQueue.isEmpty()) {
            int id = Integer.parseInt(collisionMessageQueue.poll());
            ImageView ant = antsList.get(id);
            ant.setRotate((ant.getRotate() + 180) % 360);
            direction[id] *= -1;
            ImageView crash = crashList.get(id);
            crash.setTranslateX(ant.getTranslateX());
            FadeTransition fadeTransition = new FadeTransition();
            fadeTransition.setDuration(Duration.millis(500 / speed));
            fadeTransition.setFromValue(1);
            fadeTransition.setToValue(0);
            fadeTransition.setNode(crash);
            fadeTransition.play();
        }
    }

    /**
     * 碰撞处理，将碰撞消息添加到消息队列中
     *
     * @param antId 蚂蚁id
     */
    public void collideGame(String antId) {
        collisionMessageQueue.add(antId);
    }

    /**
     * 更新游戏状态
     *
     * @param currentPosition 蚂蚁当前位置
     * @param newTick         当前时间刻
     */
    public void updateGame(Map<String, Double> currentPosition, Long newTick) {
        // 更新蚂蚁实时位置
        for (int i = 0; i < COUNT; i++) {
            ImageView ant = antsList.get(i);
            Text text = positionList.get(i);
            Double aDouble = currentPosition.get(String.valueOf(i));
            if (aDouble != null) {
                TranslateTransition translateTransition = new TranslateTransition();
                translateTransition.setInterpolator(Interpolator.LINEAR);
                translateTransition.setDuration(Duration.millis(250 / this.speed));
                translateTransition.setFromX(ant.getTranslateX());
                translateTransition.setToX(currentPosition.get(String.valueOf(i)) * 3 - 450);
                translateTransition.setNode(ant);
                translateTransition.play();
                text.setText(String.valueOf(currentPosition.get(String.valueOf(i))));
            } else {
                ant.setVisible(false);
                ant.setTranslateX(initPos[i]);
                text.setText("出去了");
            }
        }

        // 更新时间
        ticks.setText(newTick.toString());

        // 由于动画的存在，导致UI中蚂蚁的位置要落后Playroom中计算出的位置，所以碰撞也需要延后1tick
        if (collisionDelayFlag) {
            collisionDelayFlag = false;
            collisionHandler();
        }
        if (!collisionMessageQueue.isEmpty()) {
            collisionDelayFlag = true;
        }
    }

    /**
     * 游戏的初始化。控制蚂蚁初始方向
     *
     * @param direction 蚂蚁方向
     * @param gameId    游戏id
     */
    public void initGame(Map<String, Integer> direction, String gameId) {
        completion.setText(gameId);
        for (Map.Entry<String, Integer> entry : direction.entrySet()) {
            int id = Integer.parseInt(entry.getKey());
            ImageView ant = antsList.get(id);
            if (entry.getValue() > 0) {
                ant.setRotate(0);
            } else {
                ant.setRotate(180);
            }
            ant.setVisible(true);
            ant.setTranslateX(initPos[id]);
            // 更新标签位置信息
            Text position = positionList.get(id);
            position.setText(String.valueOf(initRealPos[id]));
        }
    }

    /**
     * 每次模拟结束后更新最大和最小时间刻
     *
     * @param maxTicks 最大时间刻
     * @param minTicks 最小时间刻
     */
    public void updateTicks(Long maxTicks, Long minTicks) {
        minTimeText.setText(String.valueOf(minTicks));
        maxTimeText.setText(String.valueOf(maxTicks));

    }

    /**
     * 结束游戏
     */
    public void endGame() {
        comboBox.setDisable(false);
        radioAll.setDisable(false);
        radioSingle.setDisable(false);
        button.setText("开始");
        stopButton.setDisable(true);
        gameStatus = GameStatus.TERMINATED;
        for (int id = 0; id < COUNT; id++) {
            ImageView ant = antsList.get(id);
            ant.setRotate(0);
            ant.setTranslateX(initPos[id]);
            ant.setVisible(true);
            // 更新标签位置信息
            Text position = positionList.get(id);
            position.setText(String.valueOf(initRealPos[id]));
        }
    }

    /**
     * 游戏入口
     *
     * @param args 外部参数用于初始化javafx
     */
    public static void main(String[] args) {
        launch(args);
    }

}
