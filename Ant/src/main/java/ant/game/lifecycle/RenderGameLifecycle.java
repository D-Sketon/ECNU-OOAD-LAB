package ant.game.lifecycle;

import ant.game.core.CreepingGame;
import ant.game.core.GameContext;
import ant.game.entity.Ant;
import ant.ui.MainController;
import javafx.application.Platform;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 主要负责更新UI，需要注册在其他生命周期函数之后来更新UI界面
 */
public class RenderGameLifecycle extends AbstractGameLifecycle {

    private MainController mainController;

    public RenderGameLifecycle(GameContext gameContext, CreepingGame creepingGame, MainController mainController) {
        super(gameContext, creepingGame);
        this.mainController = mainController;
    }

    public RenderGameLifecycle(GameContext gameContext, CreepingGame creepingGame) {
        super(gameContext, creepingGame);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Platform.runLater(() -> mainController.initGame(getDirection(), getGameId()));
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        Platform.runLater(() -> mainController.updateGame(getCurrentPosition(), getCurrentTick()));
    }

    /**
     * 获得当前蚂蚁位置
     *
     * @return 蚂蚁位置
     */
    private Map<String, Double> getCurrentPosition() {
        List<Ant> ants = this.gameContext.getAnts();
        Map<String, Double> currentPos = new HashMap<>();
        ants.forEach(ant -> currentPos.put(ant.getId(), ant.getPosition().getX()));
        return currentPos;
    }

    /**
     * 获得当前蚂蚁方向
     *
     * @return 蚂蚁方向
     */
    private Map<String, Integer> getDirection() {
        Map<String, Integer> direction = new HashMap<>();
        List<Ant> ants = this.gameContext.getAnts();
        ants.forEach(ant -> direction.put(ant.getId(), (int) ant.getVelocity().getX()));
        return direction;
    }

    /**
     * 获得当前时间刻
     *
     * @return 当前时间刻
     */
    private Long getCurrentTick() {
        return this.gameContext.getTimeTicks();
    }

    /**
     * 获得游戏id
     *
     * @return 游戏id
     */
    private String getGameId() {
        return this.gameContext.getGameId();
    }

}
