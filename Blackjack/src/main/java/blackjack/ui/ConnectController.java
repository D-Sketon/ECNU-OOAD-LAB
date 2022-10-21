package blackjack.ui;

import blackjack.enums.GameMode;
import blackjack.game.GameUtil;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.Setter;

import java.util.Random;

/**
 * 用于连接的子窗口
 */
public class ConnectController {
    @Setter
    MainController mainController;

    private GameMode mode;

    @Getter
    private String ip;

    private Stage stage;
    private final int STAGE_WIDTH = 1000;
    private final int STAGE_HEIGHT = 500;

    private final StackPane mainPane = new StackPane();
    private final StackPane connectPane = new StackPane();
    private final DropShadow shadow = new DropShadow();

    private Scene mainScene;
    private Scene connectScene;

    private static Label connectLabel;
    private static Label connectErrorMsg;
    private static Button returnButton;
    private final static String BUTTON_STYLE = "-fx-background-color:grey;" +
            "-fx-text-fill:#FFFFF0;" +
            "-fx-border-radius:3;" +
            "-fx-border-color:#000000;" +
            "-fx-border-width:2;" +
            "-fx-border-insets:-1";

    private final static String[] phrase = {"小富靠勤，大富靠命", "不要向前看，要向钱看", "傻人有傻福", "小赌怡情，大赌伤身"};

    public void display(String title) {
        mainPane.setStyle("-fx-background-image: url(deskWithBlur.png);-fx-background-position: center;-fx-background-size: 1000 500");
        connectPane.setStyle("-fx-background-image: url(deskWithBlur.png);-fx-background-position: center;-fx-background-size: 1000 500");

        Label label = new Label();
        label.setFont(new Font(20));
        label.setTranslateX(0);
        label.setTranslateY(-100);
        connectPane.getChildren().add(label);

        Label ipLabel = new Label("请输入合法ip");
        ipLabel.setFont(new Font(20));
        ipLabel.setTranslateX(0);
        ipLabel.setTranslateY(-50);
        ipLabel.setVisible(false);
        ipLabel.setStyle("-fx-text-fill:#FF0000;");
        mainPane.getChildren().add(ipLabel);

        TextField ipTxt = new TextField();
        ipTxt.setLayoutX(100);
        ipTxt.setLayoutY(24);
        ipTxt.setMaxWidth(200);
        ipTxt.setMaxHeight(35);
        ipTxt.setPromptText("请输入ip地址");
        ipTxt.setFont(new Font(20));
        mainPane.getChildren().add(ipTxt);

        connectLabel = new Label("连接中...");
        connectLabel.setFont(new Font(20));
        connectLabel.setTextFill(Color.WHITE);
        connectPane.getChildren().add(connectLabel);

        connectErrorMsg = new Label();
        connectErrorMsg.setTranslateY(30);
        connectErrorMsg.setFont(new Font(20));
        connectErrorMsg.setTextFill(Color.WHITE);
        connectPane.getChildren().add(connectErrorMsg);

        returnButton = new Button("返回");
        returnButton.setFont(new Font(18));
        returnButton.setTranslateY(75);
        returnButton.setStyle(BUTTON_STYLE);
        returnButton.setVisible(false);
        returnButton.setOnAction((e) -> {
            stage.setScene(mainScene);
            returnButton.setVisible(false);
            connectErrorMsg.setText("");
            connectLabel.setText("连接中...");
        });
        connectPane.getChildren().add(returnButton);

        Button btn1 = new Button("加入房间");
        btn1.setFont(new Font(18));
        btn1.setStyle(BUTTON_STYLE);
        btn1.setTranslateX(-300);
        btn1.setTranslateY(150);
        btn1.setPrefSize(200, 50);
        btn1.setOnMouseClicked(event -> {
            mode = GameMode.JOIN_OTHER;
            ip = ipTxt.getText();
            if (GameUtil.ipCheck(ip)) {
                label.setText(phrase[new Random().nextInt(4)]);
                label.setTextFill(Color.WHITE);
                stage.setScene(connectScene);
                ipLabel.setVisible(false);
                mainController.setIp(ip);
                mainController.setMode(mode);
                mainController.startGame();
            } else {
                ipLabel.setVisible(true);
            }
        });
        btn1.addEventHandler(MouseEvent.MOUSE_ENTERED, (MouseEvent e) -> btn1.setEffect(shadow));
        btn1.addEventHandler(MouseEvent.MOUSE_EXITED, (MouseEvent e) -> btn1.setEffect(null));
        mainPane.getChildren().add(btn1);

        Button btn2 = new Button("创建房间");
        btn2.setFont(new Font(18));
        btn2.setStyle(BUTTON_STYLE);
        btn2.setTranslateX(300);
        btn2.setTranslateY(150);
        btn2.setPrefSize(200, 50);
        btn2.setOnMouseClicked(event -> {
            stage.setScene(connectScene);
            mode = GameMode.ROOM_OWNER;
            mainController.setMode(mode);
            mainController.startGame();
            stage.close();
        });
        btn2.addEventHandler(MouseEvent.MOUSE_ENTERED, (MouseEvent e) -> btn2.setEffect(shadow));
        btn2.addEventHandler(MouseEvent.MOUSE_EXITED, (MouseEvent e) -> btn2.setEffect(null));
        mainPane.getChildren().add(btn2);

        mainScene = new Scene(mainPane, STAGE_WIDTH, STAGE_HEIGHT);
        connectScene = new Scene(connectPane, STAGE_WIDTH, STAGE_HEIGHT);
        stage = new Stage();
        stage.getIcons().add(new Image("icon.png"));
        stage.setResizable(false);
        stage.sizeToScene();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setScene(mainScene);
        stage.setTitle(title);
        stage.showAndWait();
    }

    public void closeStage() {
        stage.close();
    }

    public static void setErrorMsg(String s) {
        connectLabel.setText("无法连接至服务器");
        connectErrorMsg.setText(s);
        returnButton.setVisible(true);
    }
}

