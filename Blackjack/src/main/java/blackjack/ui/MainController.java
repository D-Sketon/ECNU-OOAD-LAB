package blackjack.ui;

import blackjack.adapter.LocalGameAdapter;
import blackjack.adapter.RemoteGameAdapter;
import blackjack.entity.Card;
import blackjack.entity.Hand;
import blackjack.enums.GameEvent;
import blackjack.enums.GameMode;
import blackjack.enums.PlayerStatus;
import blackjack.enums.Suit;
import blackjack.game.GameServer;
import blackjack.game.GameClient;
import javafx.animation.*;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.transform.Rotate;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.util.Pair;
import lombok.Setter;

import java.util.*;
import java.util.function.Consumer;

/**
 * 程序主窗口
 */
public class MainController extends Application {
    @Setter
    private GameMode mode;
    @Setter
    private String ip;

    private ConnectController connectController;

    private final StackPane mainPane = new StackPane();
    private final StackPane titlePane = new StackPane();
    private Label balanceContent;
    private Button startButton;
    private Button betButton;

    private final DropShadow dropShadow = new DropShadow(1, 0, 0, Color.BLACK);
    private final DropShadow buttonShadow = new DropShadow();
    private final Duration duration500 = Duration.millis(500);
    private final Duration duration250 = Duration.millis(250);

    private final int CARD_WIDTH = 90;
    private final int CARD_HEIGHT = 130;
    private final int STAGE_WIDTH = 1280;
    private final int STAGE_HEIGHT = 800;
    private final int[] CARD_INIT_X = {-510, -280, 0, 280, 510};
    private final int[] CARD_INIT_Y = {0, 90, 120, 90, 0};
    private final int[] CHIP_INIT_X = {-510, -280, 0, 280, 510};
    private final int[] CHIP_INIT_Y = {100, 190, 220, 190, 100};
    private final int[] POINT_INIT_X = {-510, -280, 0, 280, 510};
    private final int[] POINT_INIT_Y = {-80, 10, 40, 10, -80};
    private final int[] CHIP_VALUE = {500, 200, 100, 50};
    private final int DECK_X = -400;
    private final int DECK_Y = -310;
    private final int SPLIT_OFFSET = 50;
    private final int DEALER_OFFSET = -200;
    private final int CARD_OFFSET = 20;
    private final int PLAYER_OFFSET = 210;
    private final int INVISIBLE_OFFSET_Y = 500;

    private final List<List<List<ImageView>>> playerCardList;
    private final List<List<List<ImageView>>> playerChipList;
    private final List<List<Label>> playerPointList;
    private final List<ImageView> dealCardList;
    private final ImageView[] playerList;
    private final PlayerStatus[] playerStatusList;
    private final Map<GameEvent, Button> buttonMap;

    private final CountdownTimer countdownTimer = new CountdownTimer();

    private final static String BUTTON_STYLE = "-fx-background-color:grey;" +
            "-fx-background-radius: 10;" +
            "-fx-text-fill:#FFFFF0;" +
            "-fx-border-radius:10;" +
            "-fx-border-color:#000000;" +
            "-fx-border-width:3;" +
            "-fx-border-insets:-1;";

    private boolean isDealerDealFinish = false;
    private boolean isInitDealFinish = false;

    private int currentBalance = 1000;

    private GameClient gameClient;
    private GameServer gameServer;

    private Stage primaryStage;
    private Scene mainScene;
    private Scene titleScene;

    private final Queue<Pair<GameEvent, Object>> messageQueue = new LinkedList<>();
    private final Queue<Pair<Integer, Card>> cardQueue = new LinkedList<>();
    private final Queue<Pair<Integer, Integer>> pointLabelQueue = new LinkedList<>();

    static class ChipParam {
        int playerIndex;
        int handIndex;
        int bet;
        int realBet;

        public ChipParam(int playerIndex, int handIndex, int bet, int realBet) {
            this.playerIndex = playerIndex;
            this.handIndex = handIndex;
            this.bet = bet;
            this.realBet = realBet;
        }
    }

    static class CountdownTimer extends Label {
        private int i = 20;

        private boolean started;

        private Timeline timeline;

        public CountdownTimer() {
            setText("20");
        }

        public void start(Consumer<?> startFun, Consumer<?> endFun) {
            if (started) {
                return;
            }
            setText("20");
            i = 20;
            timeline = new Timeline();
            KeyFrame kf = new KeyFrame(Duration.seconds(0),
                    event -> {
                        String s = String.valueOf(i--);
                        if (s.length() == 1) s = "0" + s;
                        setText(s);
                        if (i <= 0) {
                            stop(endFun);
                            if (startFun != null) {
                                startFun.accept(null);
                            }
                        }
                    });
            timeline.getKeyFrames().addAll(kf, new KeyFrame(Duration.seconds(1)));

            timeline.setCycleCount(Animation.INDEFINITE);
            timeline.play();

            started = true;
        }

        public void stop(Consumer<?> endFun) {
            if (started) {
                started = false;
                timeline.stop();
                if (endFun != null) {
                    endFun.accept(null);
                }
            }
        }
    }

    public MainController() {
        playerList = new ImageView[5];
        playerStatusList = new PlayerStatus[5];
        for (int i = 0; i < 5; i++) {
            playerStatusList[i] = PlayerStatus.IDLE;
        }
        dealCardList = new ArrayList<>();
        playerCardList = new ArrayList<>();
        playerChipList = new ArrayList<>();
        playerPointList = new ArrayList<>();
        buttonMap = new HashMap<>();
        for (int i = 0; i < 5; i++) {
            List<List<ImageView>> hands = new ArrayList<>();
            hands.add(new ArrayList<>());
            hands.add(new ArrayList<>());
            playerCardList.add(hands);
            List<List<ImageView>> chips = new ArrayList<>();
            chips.add(new ArrayList<>());
            chips.add(new ArrayList<>());
            playerChipList.add(chips);
            List<Label> label = new ArrayList<>();
            playerPointList.add(label);
        }
    }

    /**
     * 初始化游戏，本地加入游戏
     */
    private void initLocalGame() {
        gameServer = new GameServer();
        LocalGameAdapter localGameAdapter = new LocalGameAdapter(gameServer);
        gameClient = new GameClient(this, localGameAdapter);
        gameServer.joinGameLocal(gameClient);
    }

    /**
     * 初始化游戏，远程加入游戏
     */
    private void initRemoteClientGame() {
        RemoteGameAdapter remoteGameAdapter = new RemoteGameAdapter();
        gameClient = new GameClient(this, remoteGameAdapter);
        remoteGameAdapter.setSelf(gameClient);
        new Thread(() -> {
            try {
                gameClient.getClientOnline().initNetty(gameClient, ip);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, "Client Netty Thread").start();
    }

    /**
     * 初始化游戏，局域网内创建服务器，同时本地加入游戏
     */
    private void initRemoteServerGame() {
        gameServer = new GameServer();
        new Thread(() -> gameServer.startServer(), "Server Netty Thread").start();
        LocalGameAdapter localGameAdapter = new LocalGameAdapter(gameServer);
        gameClient = new GameClient(this, localGameAdapter);
        gameServer.joinGameLocal(gameClient);
    }

    /**
     * 初始化TitlePane
     */
    private void initTitlePane() {
        titlePane.setStyle(
                "-fx-background-image: url(desk.png);-fx-background-position: center;-fx-background-size: 1280 800");
        titlePane.setPrefSize(STAGE_WIDTH, STAGE_HEIGHT);

        Label label = new Label("BLACKJACK");
        label.setTranslateY(-200);
        label.setTranslateX(0);
        label.setStyle("-fx-font-size: 100px;" +
                "-fx-font-family: 'Arial Black';" +
                "-fx-fill: #818181;" +
                "-fx-effect: innershadow(three-pass-box,rgba(0,0,0,0.7),6, 0.0,0 ,2);");
        label.setPrefSize(700, 80);

        Button localButton = new Button("独 乐 乐");
        localButton.setTranslateY(0);
        localButton.setTranslateX(0);
        localButton.setStyle(BUTTON_STYLE);
        localButton.setFont(new Font(25));
        localButton.setPrefSize(350, 80);
        localButton.setOnAction((event) -> {
            mode = GameMode.PLAY_ALONE;
            this.startGame();
            primaryStage.setScene(mainScene);
        });
        localButton.addEventHandler(MouseEvent.MOUSE_ENTERED, (MouseEvent e) -> localButton.setEffect(buttonShadow));
        localButton.addEventHandler(MouseEvent.MOUSE_EXITED, (MouseEvent e) -> localButton.setEffect(null));

        Button remoteButton = new Button("众 乐 乐");
        remoteButton.setTranslateY(200);
        remoteButton.setTranslateX(0);
        remoteButton.setStyle(BUTTON_STYLE);
        remoteButton.setFont(new Font(25));
        remoteButton.setPrefSize(350, 80);
        remoteButton.setOnAction((event) -> {
            new Thread(() -> {
                try {
                    Platform.runLater(() -> {
                        connectController = new ConnectController();
                        connectController.setMainController(this);
                        connectController.display("欢迎");
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }, "Client Netty Thread").start();
        });
        remoteButton.addEventHandler(MouseEvent.MOUSE_ENTERED, (MouseEvent e) -> remoteButton.setEffect(buttonShadow));
        remoteButton.addEventHandler(MouseEvent.MOUSE_EXITED, (MouseEvent e) -> remoteButton.setEffect(null));

        titlePane.getChildren().add(label);
        titlePane.getChildren().add(remoteButton);
        titlePane.getChildren().add(localButton);
    }

    /**
     * 初始化MainPane
     */
    private void initMainPane() {
        mainPane.setStyle(
                "-fx-background-image: url(desk.png);-fx-background-position: center;-fx-background-size: 1280 800");
        mainPane.setPrefSize(STAGE_WIDTH, STAGE_HEIGHT);

        countdownTimer.setFont(new Font(25));
        countdownTimer.setStyle("-fx-padding: 20; -fx-background-color:green;-fx-background-radius: 50%;-fx-font-weight: bold;");
        countdownTimer.setTextFill(Color.WHITE);
        countdownTimer.setTranslateX(325);
        countdownTimer.setTranslateY(-340);
        countdownTimer.setVisible(false);
        mainPane.getChildren().add(countdownTimer);

        startButton = new Button("开始游戏");
        startButton.setStyle(BUTTON_STYLE);
        startButton.setOnAction((e) -> {
            for (PlayerStatus playerStatus : playerStatusList) {
                if (playerStatus == PlayerStatus.AFTER_JOIN) {
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setContentText("有玩家未下注！");
                    alert.show();
                    return;
                }
            }
            isDealerDealFinish = false;
            isInitDealFinish = false;
            gameServer.startGame();
            startButton.setVisible(false);
        });
        mainPane.getChildren().add(startButton);

        Button hitButton = new Button("发牌");
        hitButton.setTranslateY(350);
        hitButton.setTranslateX(500);
        hitButton.setVisible(false);
        hitButton.setStyle(BUTTON_STYLE);
        hitButton.setOnAction((event) -> {
            gameClient.onPlayerHit();
            hideButton();
            countdownTimer.stop(dummy -> countdownTimer.setVisible(false));
        });
        mainPane.getChildren().add(hitButton);
        buttonMap.put(GameEvent.HIT, hitButton);

        Button splitButton = new Button("分牌");
        splitButton.setTranslateY(350);
        splitButton.setTranslateX(600);
        splitButton.setVisible(false);
        splitButton.setStyle(BUTTON_STYLE);
        splitButton.setOnAction((event) -> {
            gameClient.onPlayerSplit();
            hideButton();
            countdownTimer.stop(dummy -> countdownTimer.setVisible(false));
        });
        mainPane.getChildren().add(splitButton);
        buttonMap.put(GameEvent.SPLIT, splitButton);

        Button standButton = new Button("停牌");
        standButton.setTranslateY(350);
        standButton.setTranslateX(550);
        standButton.setVisible(false);
        standButton.setStyle(BUTTON_STYLE);
        standButton.setOnAction((event) -> {
            gameClient.onPlayerStand();
            hideButton();
            countdownTimer.stop(dummy -> countdownTimer.setVisible(false));
        });
        mainPane.getChildren().add(standButton);
        buttonMap.put(GameEvent.STAND, standButton);

        Button doubleButton = new Button("加倍");
        doubleButton.setTranslateY(300);
        doubleButton.setTranslateX(600);
        doubleButton.setVisible(false);
        doubleButton.setStyle(BUTTON_STYLE);
        doubleButton.setOnAction((event) -> {
            gameClient.onPlayerDouble();
            hideButton();
            countdownTimer.stop(dummy -> countdownTimer.setVisible(false));
        });
        mainPane.getChildren().add(doubleButton);
        buttonMap.put(GameEvent.DOUBLE, doubleButton);

        Button insureButton = new Button("保险");
        insureButton.setTranslateY(300);
        insureButton.setTranslateX(550);
        insureButton.setVisible(false);
        insureButton.setStyle(BUTTON_STYLE);
        insureButton.setOnAction((event) -> {
            gameClient.onPlayerInsurance();
            hideButton();
            countdownTimer.stop(dummy -> countdownTimer.setVisible(false));
        });
        mainPane.getChildren().add(insureButton);
        buttonMap.put(GameEvent.INSURANCE, insureButton);

        Button surrenderButton = new Button("投降");
        surrenderButton.setTranslateY(300);
        surrenderButton.setTranslateX(500);
        surrenderButton.setVisible(false);
        surrenderButton.setStyle(BUTTON_STYLE);
        surrenderButton.setOnAction((event) -> {
            gameClient.onPlayerSurrender();
            hideButton();
            countdownTimer.stop(dummy -> countdownTimer.setVisible(false));
        });
        mainPane.getChildren().add(surrenderButton);

        buttonMap.put(GameEvent.SURRENDER, surrenderButton);

        betButton = new Button("下注");
        betButton.setTranslateY(300);
        betButton.setTranslateX(200);
        betButton.setStyle(BUTTON_STYLE);
        betButton.setOnAction((event) -> showBetDialog());
        mainPane.getChildren().add(betButton);

        Label balanceTitle = new Label("本金：");
        balanceTitle.setFont(Font.font("TimesRomes", FontWeight.BOLD, 22));
        balanceTitle.setTranslateY(300);
        balanceTitle.setTranslateX(-500);
        balanceTitle.setTextFill(Color.WHITE);
        mainPane.getChildren().add(balanceTitle);

        balanceContent = new Label("" + currentBalance);
        balanceContent.setFont(Font.font("TimesRomes", FontWeight.BOLD, 22));
        balanceContent.setTranslateY(300);
        balanceContent.setTranslateX(-450);
        balanceContent.setTextFill(Color.WHITE);
        mainPane.getChildren().add(balanceContent);
    }

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        primaryStage.getIcons().add(new Image("icon.png"));
        primaryStage.setTitle("BlackJack");
        primaryStage.setOnCloseRequest(event -> {
            if (gameClient != null && gameClient.getClientOnline().getEventExecutors() != null)
                gameClient.getClientOnline().getEventExecutors().shutdownGracefully();
            System.exit(0);
        });

        initMainPane();
        initTitlePane();

        mainScene = new Scene(mainPane, STAGE_WIDTH, STAGE_HEIGHT);
        titleScene = new Scene(titlePane, STAGE_WIDTH, STAGE_HEIGHT);
        primaryStage.setScene(titleScene);
        primaryStage.setResizable(false);
        primaryStage.sizeToScene();
        primaryStage.show();
    }

    /**
     * 基础下注事件
     *
     * @param playerIndex 玩家索引
     * @param handIndex   手牌索引
     * @param amount      下注金额
     * @param isSelf      是否为自己
     */
    public void renderPlayerBet(int playerIndex, int handIndex, int amount, boolean isSelf) {
        playerStatusList[playerIndex] = PlayerStatus.AFTER_BET;
        ImageView imageView = playerList[playerIndex];
        Image image;
        if (isSelf) {
            image = new Image("playerIcon3.png");
            currentBalance -= amount;
            balanceContent.setText("" + currentBalance);
        } else {
            image = new Image("playerIcon.png");
        }
        imageView.setImage(image);
        addChip(amount, playerIndex, handIndex).play();
    }

    /**
     * 下注时展示对话框
     */
    private void showBetDialog() {
        gameClient.onRestartGame();
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        DialogPane dialogPane = new DialogPane();
        List<String> bets = new ArrayList<>();
        bets.add("100");
        bets.add("200");
        bets.add("500");
        ChoiceDialog<String> choiceDialog = new ChoiceDialog<>("选择金额", bets);
        alert.setDialogPane(dialogPane);
        Optional<String> optional = choiceDialog.showAndWait();
        if (optional.isPresent()) {
            String s = optional.get();
            if (!s.equals("选择金额")) {
                int bet = Integer.parseInt(s);
                gameClient.onPlayerBet(bet);
                betButton.setVisible(false);
                countdownTimer.stop(dummy -> countdownTimer.setVisible(false));
            }
        }
    }

    /**
     * 基础发牌事件
     *
     * @param playerIndex 玩家索引
     * @param newCard     牌面信息
     * @param handIndex   手牌索引
     */
    public void renderPlayerHit(int playerIndex, Card newCard, int handIndex) {
        for (Button value : buttonMap.values()) {
            value.setDisable(true);
        }
        ParallelTransition playerNewCard = createPlayerNewCard(playerIndex, newCard, handIndex);
        initPoint(playerIndex, handIndex, Math.min(newCard.getFaceValue(), 10));
        playerNewCard.setOnFinished((e) -> showButton(gameClient.onPlayerButton()));
        playerNewCard.play();
    }

    /**
     * 基础警告事件
     */
    public static void showErrorDialog(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setContentText(msg);
        alert.show();
    }

    /**
     * 基础庄家摸牌事件
     *
     * @param hand 手牌信息（包含第一张暗牌信息）
     */
    public void renderDealerDeal(Hand hand) {
        ImageView imageView = dealCardList.get(0);
        Card firstCard = hand.getCards().get(0);
        int faceValue = firstCard.getFaceValue();
        String suit = firstCard.getSuit().toString().toLowerCase();
        String cardUrl = "cards/" + faceValue + "_of_" + suit + "s.png";
        Image image = new Image(cardUrl);
        imageView.setImage(image);

        for (int i = 1; i < hand.getCards().size(); i++) {
            cardQueue.add(new Pair<>(-1, hand.getCards().get(i)));
        }
        recursiveInitialDeal(cardQueue, (b) -> {
            isDealerDealFinish = b;
            while (!messageQueue.isEmpty()) {
                Pair<GameEvent, Object> peek = messageQueue.peek();
                if (peek.getKey() == GameEvent.GAME_RESULT) {
                    messageQueue.poll();
                    ChipParam chipParam = (ChipParam) peek.getValue();
                    renderGameResult(chipParam.playerIndex, chipParam.handIndex, chipParam.bet, chipParam.realBet);
                } else {
                    break;
                }
            }
        }, true);
    }

    /**
     * 基础游戏结算事件，用于筹码动画
     *
     * @param playerIndex 玩家索引
     * @param handIndex   手牌索引
     * @param bet         返还金额
     * @param realBet     实际押注金额
     */
    public void renderGameResult(int playerIndex, int handIndex, int bet, int realBet) {
        if (!isDealerDealFinish) {
            messageQueue.add(new Pair<>(GameEvent.GAME_RESULT, new ChipParam(playerIndex, handIndex, bet, realBet)));
        } else {
            Transition transition;
            if (bet == 0) {
                transition = retrieveChipToTop(playerIndex, handIndex);
            } else if (bet == realBet) {
                transition = retrieveChipToBottom(playerIndex, handIndex);
            } else if (bet > realBet) {
                ParallelTransition retrieveChipToBottom = retrieveChipToBottom(playerIndex, handIndex);
                SequentialTransition addChipToBottom = addChip(bet - realBet, 0, -INVISIBLE_OFFSET_Y, CHIP_INIT_X[playerIndex], INVISIBLE_OFFSET_Y);
                transition = new ParallelTransition(retrieveChipToBottom, addChipToBottom);
            } else {
                // bet < realBet，需要分裂筹码
                // 首先移除原先筹码
                List<ImageView> imageViews = playerChipList.get(playerIndex).get(handIndex);
                for (ImageView imageView : imageViews) {
                    mainPane.getChildren().remove(imageView);
                }
                SequentialTransition inTransition = addChip(bet, CHIP_INIT_X[playerIndex], CHIP_INIT_Y[playerIndex], CHIP_INIT_X[playerIndex], INVISIBLE_OFFSET_Y);
                SequentialTransition outTransition = addChip(realBet - bet, CHIP_INIT_X[playerIndex], CHIP_INIT_Y[playerIndex], 0, -INVISIBLE_OFFSET_Y);
                transition = new ParallelTransition(inTransition, outTransition);
            }
            transition.setDelay(duration500);
            transition.setOnFinished((e) -> {
                playerStatusList[playerIndex] = PlayerStatus.AFTER_JOIN;
                restartGame();
            });
            transition.play();
        }
    }

    /**
     * 基础更新余额事件
     *
     * @param bet 金额
     */
    public void renderBalance(int bet) {
        currentBalance += bet;
        balanceContent.setText("" + currentBalance);
    }

    /**
     * 展示/刷新操作按钮状态
     *
     * @param map 按钮可用映射表
     */
    public void showButton(Map<GameEvent, Boolean> map) {
        if (!isInitDealFinish) {
            messageQueue.add(new Pair<>(GameEvent.SHOW_BUTTON, map));
        } else {
            if (map == null) {
                for (Button value : buttonMap.values()) {
                    value.setVisible(false);
                }
            } else {
                for (Map.Entry<GameEvent, Boolean> entry : map.entrySet()) {
                    Button button = buttonMap.get(entry.getKey());
                    button.setVisible(true);
                    button.setDisable(!entry.getValue());
                }
                countdownTimer.setVisible(true);
                countdownTimer.start(dummy -> gameClient.onPlayerStand(),
                        dummy -> countdownTimer.setVisible(false));
            }
        }

    }

    /**
     * 隐藏按钮
     */
    private void hideButton() {
        for (Button value : buttonMap.values()) {
            value.setVisible(false);
        }
    }

    /**
     * 初始化发牌，指为所有玩家和庄家发两张牌
     *
     * @param info 发牌信息
     */
    public void renderInitialDeal(Map<Integer, Hand> info) {
        List<Pair<Integer, Hand>> clonedHand = new ArrayList<>();
        for (Map.Entry<Integer, Hand> integerHandEntry : info.entrySet()) {
            try {
                clonedHand.add(new Pair<>(integerHandEntry.getKey(), (Hand) integerHandEntry.getValue().clone()));
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }
        }
        while (true) {
            boolean flag = true;
            for (Pair<Integer, Hand> pair : clonedHand) {
                if (!pair.getValue().getCards().isEmpty()) {
                    cardQueue.add(new Pair<>(pair.getKey(), pair.getValue().getCards().get(0)));
                    pair.getValue().getCards().remove(0);
                    flag = false;
                }
            }
            if (flag) {
                break;
            }
        }
        recursiveInitialDeal(cardQueue,
                b -> {
                    isInitDealFinish = b;
                    if (!messageQueue.isEmpty()) {
                        Pair<GameEvent, Object> peek = messageQueue.peek();
                        if (peek.getKey() == GameEvent.SHOW_BUTTON) {
                            messageQueue.poll();
                            showButton((Map<GameEvent, Boolean>) peek.getValue());
                        }
                    }
                    if (!pointLabelQueue.isEmpty()) {
                        Pair<Integer, Integer> poll = pointLabelQueue.poll();
                        renderPointLabel(poll.getKey(), poll.getValue());
                    }
                },
                true);
    }

    /**
     * 递归地绑定初始化发牌动画
     *
     * @param queue 发牌消息队列
     */
    private void recursiveInitialDeal(Queue<Pair<Integer, Card>> queue, Consumer<Boolean> consumer,
                                      boolean consumerParam) {
        if (!queue.isEmpty()) {
            ParallelTransition dealerNewCard;
            Pair<Integer, Card> poll = queue.poll();
            if (poll.getKey() == -1) {
                dealerNewCard = createDealerNewCard(poll.getValue());
            } else {
                dealerNewCard = createPlayerNewCard(poll.getKey(), poll.getValue(), 0);
                initPoint(poll.getKey(), 0, Math.min(poll.getValue().getFaceValue(), 10));
            }
            dealerNewCard.setOnFinished(event -> {
                if (!queue.isEmpty()) {
                    recursiveInitialDeal(queue, consumer, consumerParam);
                } else {
                    if (consumer != null)
                        consumer.accept(consumerParam);
                }
            });
            dealerNewCard.play();
        } else {
            if (consumer != null)
                consumer.accept(consumerParam);
        }
    }


    /**
     * 基础分牌事件
     *
     * @param playerIndex 玩家索引
     * @return 分牌动画
     */
    public ParallelTransition renderPlayerSplit(int playerIndex) {
        ParallelTransition parallelTransition = new ParallelTransition();
        createSplitToLeftTransition(playerIndex, CARD_INIT_X[playerIndex] - SPLIT_OFFSET, parallelTransition);
        createSplitToRightTransition(playerIndex, CARD_INIT_X[playerIndex] + SPLIT_OFFSET, parallelTransition);
        createSplitToBottomTransition(playerIndex, CARD_INIT_Y[playerIndex], parallelTransition);
        List<ImageView> firstHand = playerCardList.get(playerIndex).get(0);
        List<ImageView> secondHand = playerCardList.get(playerIndex).get(1);
        secondHand.add(firstHand.remove(1));
        return parallelTransition;
    }

    /**
     * 基础玩家进入事件
     *
     * @param playerIndex 玩家索引
     */
    public void renderPlayerJoin(int playerIndex) {
        Image playerIcon = new Image("playerIcon2.png");
        ImageView playerImageView = new ImageView(playerIcon);
        playerImageView.setFitHeight(80);
        playerImageView.setFitWidth(80);
        playerImageView.setTranslateX(CARD_INIT_X[playerIndex]);
        playerImageView.setTranslateY(CARD_INIT_Y[playerIndex] + PLAYER_OFFSET);
        playerList[playerIndex] = playerImageView;
        playerStatusList[playerIndex] = PlayerStatus.AFTER_JOIN;
        mainPane.getChildren().add(playerImageView);
    }

    /**
     * 基础玩家离开事件
     *
     * @param playerIndex 玩家索引
     */
    public void renderPlayerLeave(int playerIndex) {
        mainPane.getChildren().remove(playerList[playerIndex]);
        playerList[playerIndex] = null;
        playerStatusList[playerIndex] = PlayerStatus.IDLE;
    }

    /**
     * 基础轮次结束事件
     */
    public void renderTurnEnd() {
        hideButton();
    }

    /**
     * 创建庄家发牌动画
     *
     * @param card 牌
     * @return 发牌动画
     */
    private ParallelTransition createDealerNewCard(Card card) {
        int dealCardSize = dealCardList.size();
        Image rawCardBack = new Image("cards/cardBack.png");
        ImageView rawCardBackImageView = new ImageView(rawCardBack);
        rawCardBackImageView.setFitHeight(CARD_HEIGHT / 2);
        rawCardBackImageView.setFitWidth(CARD_WIDTH / 2);
        rawCardBackImageView.setTranslateY(DEALER_OFFSET);

        boolean isRotate;
        String cardUrl;
        if (card.getSuit() == Suit.NONE) {
            cardUrl = "cards/cardBack.png";
            isRotate = false;
        } else {
            int faceValue = card.getFaceValue();
            String suit = card.getSuit().toString().toLowerCase();
            cardUrl = "cards/" + faceValue + "_of_" + suit + "s.png";
            isRotate = true;
        }
        Image rawCardFront = new Image(cardUrl);
        ImageView rawCardFrontImageView = new ImageView(rawCardFront);
        rawCardFrontImageView.setFitHeight(CARD_HEIGHT / 2);
        rawCardFrontImageView.setFitWidth(CARD_WIDTH / 2);
        rawCardFrontImageView.setTranslateY(DEALER_OFFSET);
        rawCardFrontImageView.setVisible(false);
        for (int i = 0; i < dealCardSize; i++) {
            ImageView node = dealCardList.get(i);
            TranslateTransition translateTransition = new TranslateTransition(duration250, node);
            translateTransition.setFromX(node.getTranslateX());
            translateTransition.setToX(-dealCardSize * CARD_OFFSET / 2 + i * CARD_OFFSET);
            translateTransition.play();
        }
        dealCardList.add(rawCardFrontImageView);
        rawCardFrontImageView.setEffect(dropShadow);
        mainPane.getChildren().add(rawCardFrontImageView);
        mainPane.getChildren().add(rawCardBackImageView);
        return createDealTransition(rawCardFrontImageView, rawCardBackImageView, -dealCardSize * CARD_OFFSET / 2 + dealCardSize * CARD_OFFSET,
                (int) rawCardBackImageView.getTranslateY(), isRotate);
    }

    /**
     * 根据玩家索引和手牌索引计算发牌动画中的终点坐标
     *
     * @param playerIndex        玩家索引
     * @param handIndex          手牌索引
     * @param parallelTransition 分牌并行动画，用于向外传参
     * @return 动画终点坐标
     */
    private int[] calculateCardPosition(int playerIndex, int handIndex, ParallelTransition parallelTransition) {
        int[] pos = new int[2];
        boolean isSplit = !playerCardList.get(playerIndex).get(1).isEmpty();
        int toX = CARD_INIT_X[playerIndex];
        int toY = CARD_INIT_Y[playerIndex];
        if (isSplit) {
            if (handIndex == 0) {
                toX -= SPLIT_OFFSET;
            } else if (handIndex == 1) {
                toX += SPLIT_OFFSET;
            }
        } else {
            if (handIndex == 1) {
                createSplitToLeftTransition(playerIndex, CARD_INIT_X[playerIndex] - SPLIT_OFFSET, parallelTransition);
                toX += SPLIT_OFFSET;
            }
        }
        pos[0] = toX;
        pos[1] = toY;
        return pos;
    }

    /**
     * 根据玩家索引和手牌索引计算添加筹码的终点坐标
     *
     * @param playerIndex 玩家索引
     * @param handIndex   手牌索引
     * @return 动画终点坐标
     */
    private int[] calculateChipPosition(int playerIndex, int handIndex) {
        int[] pos = new int[2];
        boolean isSplit = !playerCardList.get(playerIndex).get(1).isEmpty();
        int toX = CHIP_INIT_X[playerIndex];
        int toY = CHIP_INIT_Y[playerIndex];
        if (isSplit) {
            if (handIndex == 0) {
                toX -= SPLIT_OFFSET;
            } else if (handIndex == 1) {
                toX += SPLIT_OFFSET;
            }
        }
        //在原有的筹码上添加一个筹码
        if (playerChipList.get(playerIndex).get(handIndex).size() == 1)
            toY += SPLIT_OFFSET;
        pos[0] = toX;
        pos[1] = toY;
        return pos;
    }

    private int[] calculatePointPosition(int playerIndex, int handIndex) {
        int[] pos = new int[2];
        boolean isSplit = !playerCardList.get(playerIndex).get(1).isEmpty();
        int toX = POINT_INIT_X[playerIndex];
        int toY = POINT_INIT_Y[playerIndex];

        if (isSplit) {
            if (handIndex == 0) {
                toX -= SPLIT_OFFSET;
            } else if (handIndex == 1) {
                toX += SPLIT_OFFSET;
            }
        }
        //计算牌数
        for (Object ignored : playerCardList.get(playerIndex).get(handIndex)) {
            toY -= CARD_OFFSET;
        }
        pos[0] = toX;
        pos[1] = toY;
        return pos;
    }

    /**
     * 创建分牌动画，将主牌堆和筹码向左移动
     *
     * @param playerIndex        玩家索引
     * @param toX                动画终点X坐标
     * @param parallelTransition 分牌并行动画，用于向外传参
     */
    private void createSplitToLeftTransition(int playerIndex, int toX, ParallelTransition parallelTransition) {
        List<ImageView> firstHand = playerCardList.get(playerIndex).get(0);
        for (ImageView imageView : firstHand) {
            TranslateTransition translateTransition = new TranslateTransition(duration250, imageView);
            translateTransition.setFromX(imageView.getTranslateX());
            translateTransition.setToX(toX);
            parallelTransition.getChildren().add(translateTransition);
        }
        List<ImageView> firstChip = playerChipList.get(playerIndex).get(0);
        for (ImageView imageView : firstChip) {
            TranslateTransition translateTransition = new TranslateTransition(duration250, imageView);
            translateTransition.setFromX(imageView.getTranslateX());
            translateTransition.setToX(toX);
            parallelTransition.getChildren().add(translateTransition);
        }
    }

    /**
     * 创建分牌动画，将主牌堆向下移动
     *
     * @param playerIndex        玩家索引
     * @param toY                动画终点X坐标
     * @param parallelTransition 分牌并行动画，用于向外传参
     */
    private void createSplitToBottomTransition(int playerIndex, int toY, ParallelTransition parallelTransition) {
        List<ImageView> firstHand = playerCardList.get(playerIndex).get(0);
        TranslateTransition translateTransition = new TranslateTransition(duration250, firstHand.get(0));
        translateTransition.setFromY(firstHand.get(1).getTranslateY());
        translateTransition.setToY(toY);
        parallelTransition.getChildren().add(translateTransition);
    }

    /**
     * 创建分牌动画，将主牌堆顶牌向右移动
     *
     * @param playerIndex        玩家索引
     * @param toX                动画终点X坐标
     * @param parallelTransition 分牌并行动画，用于向外传参
     */
    private void createSplitToRightTransition(int playerIndex, int toX, ParallelTransition parallelTransition) {
        List<ImageView> firstHand = playerCardList.get(playerIndex).get(0);
        TranslateTransition translateTransition = new TranslateTransition(duration250, firstHand.get(1));
        translateTransition.setFromX(firstHand.get(1).getTranslateX());
        translateTransition.setToX(toX);
        parallelTransition.getChildren().add(translateTransition);
    }

    /**
     * 创建玩家发牌动画
     *
     * @param playerIndex 玩家索引
     * @param card        牌
     * @param handIndex   手牌索引
     * @return 发牌动画
     */
    private ParallelTransition createPlayerNewCard(int playerIndex, Card card, int handIndex) {
        Image rawCardBack = new Image("cards/cardBack.png");
        ImageView rawCardBackImageView = new ImageView(rawCardBack);
        rawCardBackImageView.setFitHeight(CARD_HEIGHT / 2);
        rawCardBackImageView.setFitWidth(CARD_WIDTH / 2);

        ParallelTransition parallelTransition = new ParallelTransition();
        int[] pos = calculateCardPosition(playerIndex, handIndex, parallelTransition);
        int toX = pos[0];
        int toY = pos[1];

        int faceValue = card.getFaceValue();
        String suit = card.getSuit().toString().toLowerCase();
        String cardUrl = "cards/" + faceValue + "_of_" + suit + "s.png";
        Image rawCardFront = new Image(cardUrl);
        ImageView rawCardFrontImageView = new ImageView(rawCardFront);
        rawCardFrontImageView.setFitHeight(CARD_HEIGHT / 2);
        rawCardFrontImageView.setFitWidth(CARD_WIDTH / 2);
        rawCardFrontImageView.setVisible(false);

        playerCardList.get(playerIndex).get(handIndex).forEach(node -> {
            TranslateTransition translateTransition = new TranslateTransition(duration250, node);
            translateTransition.setFromY(node.getTranslateY());
            translateTransition.setToY(node.getTranslateY() - CARD_OFFSET);
            translateTransition.play();
        });
        playerCardList.get(playerIndex).get(handIndex).add(rawCardFrontImageView);
        rawCardFrontImageView.setEffect(dropShadow);
        mainPane.getChildren().add(rawCardFrontImageView);
        mainPane.getChildren().add(rawCardBackImageView);

        ParallelTransition returnParallelTransition = createDealTransition(rawCardFrontImageView, rawCardBackImageView, toX,
                toY, true);
        returnParallelTransition.getChildren().add(parallelTransition);
        return returnParallelTransition;
    }

    /**
     * 创建牌面翻转动画
     *
     * @param rawCardFrontImageView 牌面ImageView
     * @param rawCardBackImageView  牌背ImageView
     * @return 翻转的<b>串行<b/>动画
     */
    private SequentialTransition createRotateTransition(ImageView rawCardFrontImageView,
                                                        ImageView rawCardBackImageView) {
        RotateTransition frontCardRotateTransition = new RotateTransition(duration250, rawCardFrontImageView);
        frontCardRotateTransition.setAxis(Rotate.Y_AXIS);
        frontCardRotateTransition.setFromAngle(-90);
        frontCardRotateTransition.setToAngle(0);

        RotateTransition backCardRotateTransition = new RotateTransition(duration250, rawCardBackImageView);
        backCardRotateTransition.setAxis(Rotate.Y_AXIS);
        backCardRotateTransition.setFromAngle(0);
        backCardRotateTransition.setToAngle(90);
        backCardRotateTransition.setOnFinished(event -> {
            rawCardFrontImageView.setVisible(true);
            rawCardBackImageView.setVisible(false);
        });

        return new SequentialTransition(backCardRotateTransition, frontCardRotateTransition);
    }

    /**
     * 创建基础发牌动画
     *
     * @param rawCardFrontImageView 牌面ImageView
     * @param rawCardBackImageView  牌背ImageView
     * @param toX                   动画X坐标终点
     * @param toY                   动画Y坐标终点
     * @param isRotate              是否牌面翻转
     * @return 发牌的<b>并行<b/>动画
     */
    private ParallelTransition createDealTransition(ImageView rawCardFrontImageView, ImageView rawCardBackImageView, int toX,
                                                    int toY, boolean isRotate) {
        TranslateTransition frontCardTranslateTransition = new TranslateTransition(duration500, rawCardFrontImageView);
        frontCardTranslateTransition.setFromY(DECK_Y);
        frontCardTranslateTransition.setFromX(DECK_X);
        frontCardTranslateTransition.setToY(toY);
        frontCardTranslateTransition.setToX(toX);

        TranslateTransition backCardTranslateTransition = new TranslateTransition(duration500, rawCardBackImageView);
        backCardTranslateTransition.setFromY(DECK_Y);
        backCardTranslateTransition.setFromX(DECK_X);
        backCardTranslateTransition.setToY(toY);
        backCardTranslateTransition.setToX(toX);

        ScaleTransition frontCardScaleTransition = new ScaleTransition(duration500, rawCardFrontImageView);
        frontCardScaleTransition.setToX(2);
        frontCardScaleTransition.setToY(2);

        ScaleTransition backCardScaleTransition = new ScaleTransition(duration500, rawCardBackImageView);
        backCardScaleTransition.setToX(2);
        backCardScaleTransition.setToY(2);

        ParallelTransition parallelTransition;
        if (isRotate) {
            SequentialTransition sequentialTransition = createRotateTransition(rawCardFrontImageView,
                    rawCardBackImageView);
            parallelTransition = new ParallelTransition(
                    frontCardTranslateTransition,
                    backCardTranslateTransition,
                    frontCardScaleTransition,
                    backCardScaleTransition,
                    sequentialTransition);
        } else {
            parallelTransition = new ParallelTransition(
                    frontCardTranslateTransition,
                    backCardTranslateTransition,
                    frontCardScaleTransition,
                    backCardScaleTransition);
            backCardTranslateTransition.setOnFinished(event -> {
                rawCardFrontImageView.setVisible(true);
                rawCardBackImageView.setVisible(false);
            });
        }
        return parallelTransition;
    }


    /**
     * 创建添加筹码动画
     *
     * @param chipImageView 筹码ImageView
     * @param toY           动画Y坐标终点
     * @return 筹码动画
     */
    private Transition createAddChipTransition(ImageView chipImageView, int toY) {
        TranslateTransition chipTransition = new TranslateTransition(duration250, chipImageView);
        chipTransition.setFromY(500);
        chipTransition.setToY(toY);
        return chipTransition;
    }

    /**
     * 添加筹码
     *
     * @param balance     筹码金额
     * @param playerIndex 玩家索引
     * @param handIndex   手牌索引
     * @return 筹码动画
     */
    public Transition addChip(int balance, int playerIndex, int handIndex) {
        Image chip = new Image("chips/" + balance + ".png");
        ImageView chipImageView = new ImageView(chip);
        chipImageView.setFitHeight(50);
        chipImageView.setFitWidth(50);

        int[] pos = calculateChipPosition(playerIndex, handIndex);
        int X = pos[0];
        int Y = pos[1];
        chipImageView.setTranslateX(X);
        chipImageView.setTranslateY(INVISIBLE_OFFSET_Y);

        playerChipList.get(playerIndex).get(handIndex).add(chipImageView);
        chipImageView.setEffect(dropShadow);
        mainPane.getChildren().add(chipImageView);
        return createAddChipTransition(chipImageView, Y);
    }

    /**
     * 创建筹码从桌上飞向顶部的动画（庄家没收玩家筹码）
     *
     * @param playerIndex 玩家索引
     * @param handIndex   手牌索引
     * @return 筹码动画
     */
    private ParallelTransition retrieveChipToTop(int playerIndex, int handIndex) {
        ParallelTransition parallelTransition = new ParallelTransition();
        playerChipList.get(playerIndex).get(handIndex).forEach(node -> {
            TranslateTransition translateTransition = new TranslateTransition(duration500, node);
            translateTransition.setFromY(node.getTranslateY());
            translateTransition.setFromX(node.getTranslateX());
            translateTransition.setToY(-INVISIBLE_OFFSET_Y);
            translateTransition.setToX(0);
            parallelTransition.getChildren().add(translateTransition);
        });
        parallelTransition.setOnFinished(e -> playerChipList.get(playerIndex).get(handIndex).clear());
        return parallelTransition;
    }

    /**
     * 创建筹码从指定地点飞向指定地方，支持多个筹码
     *
     * @param leftBet 筹码金额
     * @param fromX   动画起点X
     * @param fromY   动画起点Y
     * @param toX     动画终点X
     * @param toY     动画终点Y
     * @return 筹码串行动画
     */
    private SequentialTransition addChip(int leftBet, int fromX, int fromY, int toX, int toY) {
        SequentialTransition sequentialTransition = new SequentialTransition();
        for (int chipValue : CHIP_VALUE) {
            while (leftBet >= chipValue) {
                Image image = new Image("chips/" + chipValue + ".png");
                ImageView chipImageView = new ImageView(image);
                chipImageView.setFitHeight(50);
                chipImageView.setFitWidth(50);
                chipImageView.setTranslateX(fromX);
                chipImageView.setTranslateY(fromY);
                mainPane.getChildren().add(chipImageView);
                TranslateTransition translateTransition = new TranslateTransition(duration500, chipImageView);
                translateTransition.setToX(toX);
                translateTransition.setToY(toY);
                translateTransition.setOnFinished((e) -> mainPane.getChildren().remove(chipImageView));
                sequentialTransition.getChildren().add(translateTransition);
                leftBet -= chipValue;
            }
        }
        return sequentialTransition;
    }

    /**
     * 创建筹码从桌上飞向底部的动画（玩家收回投注的筹码）
     *
     * @param playerIndex 玩家索引
     * @param handIndex   手牌索引
     * @return 筹码动画
     */
    private ParallelTransition retrieveChipToBottom(int playerIndex, int handIndex) {
        ParallelTransition parallelTransition = new ParallelTransition();
        playerChipList.get(playerIndex).get(handIndex).forEach(node -> {
            TranslateTransition translateTransition = new TranslateTransition(duration500, node);
            translateTransition.setFromY(node.getTranslateY());
            translateTransition.setToY(INVISIBLE_OFFSET_Y);
            parallelTransition.getChildren().add(translateTransition);
        });
        parallelTransition.setOnFinished(e -> playerChipList.get(playerIndex).get(handIndex).clear());
        return parallelTransition;
    }

    public void connectSuccessHandler() {
        connectController.closeStage();
        primaryStage.setScene(mainScene);
        if (mode == GameMode.JOIN_OTHER) {
            countdownTimer.setVisible(true);
            countdownTimer.start(dummy -> gameClient.getClientOnline().getEventExecutors().shutdownGracefully(),
                    dummy -> countdownTimer.setVisible(false));
        }
    }

    /**
     * title页面开始动画
     */
    public void startGame() {
        switch (this.mode) {
            case PLAY_ALONE:
                startButton.setVisible(true);
                betButton.setVisible(true);
                initLocalGame();
                break;
            case JOIN_OTHER:
                startButton.setVisible(false);
                betButton.setVisible(true);
                initRemoteClientGame();
                break;
            case ROOM_OWNER:
                startButton.setVisible(true);
                betButton.setVisible(true);
                initRemoteServerGame();
                primaryStage.setScene(mainScene);
                break;
        }
    }

    private void restartGame() {
        for (PlayerStatus playerStatus : playerStatusList) {
            if (playerStatus == PlayerStatus.AFTER_BET) {
                return;
            }
        }
        if (mode != GameMode.JOIN_OTHER) {
            startButton.setVisible(true);
        } else {
            countdownTimer.setVisible(true);
            countdownTimer.start(dummy -> gameClient.getClientOnline().getEventExecutors().shutdownGracefully(),
                    dummy -> countdownTimer.setVisible(false));
        }
        betButton.setVisible(true);
        for (List<List<ImageView>> lists : playerCardList) {
            for (List<ImageView> list : lists) {
                Iterator<ImageView> iterator = list.iterator();
                while (iterator.hasNext()) {
                    ImageView imageView = iterator.next();
                    mainPane.getChildren().remove(imageView);
                    iterator.remove();
                }
            }
        }
        for (List<List<ImageView>> lists : playerChipList) {
            for (List<ImageView> list : lists) {
                Iterator<ImageView> iterator = list.iterator();
                while (iterator.hasNext()) {
                    ImageView imageView = iterator.next();
                    mainPane.getChildren().remove(imageView);
                    iterator.remove();
                }
            }
        }
        for (List<Label> labels : playerPointList) {
            Iterator<Label> iterator = labels.iterator();
            while (iterator.hasNext()) {
                Label label = iterator.next();
                mainPane.getChildren().remove(label);
                iterator.remove();
            }
        }
        Iterator<ImageView> iterator = dealCardList.iterator();
        while (iterator.hasNext()) {
            ImageView imageView = iterator.next();
            mainPane.getChildren().remove(imageView);
            iterator.remove();
        }
        isDealerDealFinish = false;
        isInitDealFinish = false;
    }

    /**
     * 初始化点数标签
     *
     * @param playerIndex 玩家索引
     * @param handIndex   手牌索引
     * @param cardPoint   卡牌点数
     */
    public void initPoint(int playerIndex, int handIndex, int cardPoint) {
        Label label;
        //第一次
        if (playerPointList.get(playerIndex).size() <= handIndex) {
            label = new Label();
            playerPointList.get(playerIndex).add(label);
            label.setTranslateX(POINT_INIT_X[playerIndex]);
            label.setTranslateY(POINT_INIT_Y[playerIndex]);
            label.setFont(new Font(20));
            if (pointLabelQueue.isEmpty())
                label.setStyle("-fx-padding: 10; -fx-background-color:grey;-fx-background-radius: 50%;-fx-font-weight: bold;");
            else {
                Pair<Integer, Integer> peek = pointLabelQueue.peek();
                if (peek.getKey() == playerIndex && peek.getValue() == handIndex) {
                    pointLabelQueue.poll();
                    label.setStyle("-fx-padding: 10; -fx-background-color:green;-fx-background-radius: 50%;-fx-font-weight: bold;");
                }
            }
            label.setTextFill(Color.WHITE);
            label.setText("0");
            mainPane.getChildren().add(label);
        }

        label = playerPointList.get(playerIndex).get(handIndex);
        int[] pos = calculatePointPosition(playerIndex, handIndex);
        int toX = pos[0];
        int toY = pos[1];
        int point = Integer.parseInt(label.getText());
        if (point < 11 && cardPoint == 1) {
            point += 10;
        }
        point += cardPoint;
        label.setText(point + "");

        TranslateTransition translateTransition = new TranslateTransition(duration250, label);
        translateTransition.setFromY(label.getTranslateY());
        translateTransition.setFromX(label.getTranslateX());
        translateTransition.setToY(toY);
        translateTransition.setToX(toX);
        translateTransition.play();
    }

    /**
     * 维护更新点数标签
     *
     * @param playerIndex 玩家索引
     * @param handIndex   手牌索引
     * @param point       点数
     */
    public void updatePoint(int playerIndex, int handIndex, int point) {
        Label label = playerPointList.get(playerIndex).get(handIndex);
        int[] pos = calculatePointPosition(playerIndex, handIndex);
        int toX = pos[0];
        int toY = pos[1];
        label.setText(point + "");
        TranslateTransition translateTransition = new TranslateTransition(duration250, label);
        translateTransition.setFromY(label.getTranslateY());
        translateTransition.setFromX(label.getTranslateX());
        translateTransition.setToY(toY);
        translateTransition.setToX(toX);
        translateTransition.play();
    }

    public void renderPointLabel(int playerIndex, int handIndex) {
        if (!isInitDealFinish) {
            pointLabelQueue.add(new Pair<>(playerIndex, handIndex));
            return;
        }
        for (List<Label> labels : playerPointList) {
            for (Label label : labels) {
                label.setStyle("-fx-padding: 10; -fx-background-color:grey;-fx-background-radius: 50%;-fx-font-weight: bold;");
            }
        }
        Label newLabel = playerPointList.get(playerIndex).get(handIndex);
        newLabel.setStyle("-fx-padding: 10; -fx-background-color:green;-fx-background-radius: 50%;-fx-font-weight: bold;");
    }
}
