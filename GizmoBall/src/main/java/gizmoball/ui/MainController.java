package gizmoball.ui;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class MainController extends Application {

    private final int STAGE_WIDTH = 900;
    private final int STAGE_HEIGHT = 600;

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("GizmoBall");
        primaryStage.setOnCloseRequest(event -> {
            System.exit(0);
        });
        StackPane mainPane = new StackPane();
        Scene mainScene = new Scene(mainPane, STAGE_WIDTH, STAGE_HEIGHT);
        primaryStage.setScene(mainScene);
        primaryStage.setResizable(false);
        primaryStage.sizeToScene();
        primaryStage.show();
    }
}
