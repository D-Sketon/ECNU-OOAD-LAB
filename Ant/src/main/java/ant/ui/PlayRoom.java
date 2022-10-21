package ant.ui;

import ant.game.core.CreepingGame;
import ant.game.core.GameContext;
import ant.game.entity.Ant;
import ant.game.entity.GameObject;
import ant.game.entity.Rod;
import ant.game.lifecycle.AbstractGameLifecycle;
import ant.game.lifecycle.CreepGameLifecycle;
import ant.game.lifecycle.PrintGameLifecycle;
import ant.game.lifecycle.RenderGameLifecycle;
import ant.game.physics.Collider;
import ant.game.physics.Vector2D;
import javafx.application.Platform;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

/**
 * 程序入口
 */
public class PlayRoom {

    private long maxTicks = 0;
    private long minTicks = Integer.MAX_VALUE;
    /**
     * 用于保存游戏是否被迫结束（停止）
     */
    private boolean isTerminate = false;
    private final MainController mainController;
    private CreepingGame creepingGame;

    public PlayRoom(MainController mainController) {
        this.mainController = mainController;
    }

    /**
     * 构建蚂蚁的基本信息
     *
     * @param runs       次数，没有则为-1
     * @param directions 方向数组，没有则为null
     * @return 蚂蚁列表
     */
    private List<Ant> buildAnts(int runs, int[] directions) {
        List<Ant> res = new ArrayList<>();

        BiConsumer<GameObject, GameObject> collisionCallback = (o1, o2) -> {
            Ant ant = (Ant) o1;
            ant.setVelocity(new Vector2D(-ant.getVelocity().getX(), ant.getVelocity().getY()));
            //更新UI
            Platform.runLater(() -> mainController.collideGame(ant.getId()));
        };
        int colliderSize = 0;
        Collider boxCollider = new Collider(Vector2D.zero(), new Vector2D(colliderSize, colliderSize), collisionCallback);

        int[][] pos = {{30, 0}, {80, 0}, {110, 0}, {160, 0}, {250, 0}};
        for (int i = 0; i < 5; i++) {
            // 使用速度表示面向，正数向x正方向，负数向x负方向
            int direction;
            // 如果directions不为空，则使用数组设置速度
            if (directions != null) {
                direction = directions[i];
            } else {
                direction = ((0x1 << i) & runs) == 0 ? 1 : -1;
            }
            res.add(
                    Ant.builder()
                            .id(i + "")
                            .velocity(new Vector2D(5 * direction, 0))
                            .position(new Vector2D(pos[i][0], pos[i][1]))
                            .collider(boxCollider).build());
        }
        return res;
    }

    /**
     * 自动模式的入口
     *
     * @param speed 倍速信息
     */
    public void newGame(double speed) {
        maxTicks = 0;
        minTicks = Integer.MAX_VALUE;
        isTerminate = false;
        newGame(0, speed);
    }

    /**
     * 手动模式的入口
     *
     * @param speed      倍速信息
     * @param directions 方向数组
     */
    public void newSingleGame(double speed, int[] directions) {
        maxTicks = 0;
        minTicks = Integer.MAX_VALUE;
        isTerminate = false;
        GameContext gameContext = new GameContext();
        gameContext.setTicksPerSecond((int) (4 * speed));
        gameContext.addAnt(buildAnts(-1, directions));
        gameContext.setGameId("0");
        Rod rod = new Rod(150);
        rod.setPosition(new Vector2D(rod.getHalfLength(), 0));
        gameContext.setRod(rod);

        // 游戏驱动类
        creepingGame = new CreepingGame(gameContext);
        creepingGame.registerLifecycleHook(new CreepGameLifecycle(gameContext, creepingGame));
        creepingGame.registerLifecycleHook(new RenderGameLifecycle(gameContext, creepingGame, mainController));
        creepingGame.registerLifecycleHook(new PrintGameLifecycle(gameContext, creepingGame));
        creepingGame.registerLifecycleHook(new AbstractGameLifecycle(gameContext, creepingGame) {

            @Override
            public void onDestroy() {
                long finishTicks = gameContext.getTimeTicks();
                Platform.runLater(() -> mainController.updateTicks(finishTicks, finishTicks));
                System.out.println("Run no." + "0" + " has finished, total ticks: " + finishTicks);
                Platform.runLater(mainController::endGame);
            }
        });

        // 运行游戏
        creepingGame.launch();
    }

    /**
     * 依次枚举每个蚂蚁地方向并模拟蚂蚁运动，一次模拟结束之后继续下一次枚举模拟
     *
     * @param runs  模拟第runs轮
     * @param speed 倍速信息
     */
    public void newGame(int runs, double speed) {
        // 游戏配置
        GameContext gameContext = new GameContext();
        gameContext.setTicksPerSecond((int) (4 * speed));
        gameContext.addAnt(buildAnts(runs, null));
        gameContext.setGameId(String.valueOf(runs + 1));
        Rod rod = new Rod(150);
        rod.setPosition(new Vector2D(rod.getHalfLength(), 0));
        gameContext.setRod(rod);

        // 游戏驱动类
        creepingGame = new CreepingGame(gameContext);
        creepingGame.registerLifecycleHook(new CreepGameLifecycle(gameContext, creepingGame));
        creepingGame.registerLifecycleHook(new RenderGameLifecycle(gameContext, creepingGame, mainController));
        creepingGame.registerLifecycleHook(new PrintGameLifecycle(gameContext, creepingGame));
        creepingGame.registerLifecycleHook(new AbstractGameLifecycle(gameContext, creepingGame) {

            @Override
            public void onDestroy() {
                // 记录每次跑完最大最小ticks
                long finishTicks = gameContext.getTimeTicks();
                maxTicks = Math.max(finishTicks, maxTicks);
                minTicks = Math.min(finishTicks, minTicks);
                //更新最长最少时间
                Platform.runLater(() -> mainController.updateTicks(maxTicks, minTicks));

                // 这个是用来每次结束后枚举下一个情况的
                System.out.println("Run no." + runs + " has finished, total ticks: " + finishTicks);
                if (runs < 31 && !isTerminate) {
                    newGame(runs + 1, speed);
                } else {
                    System.out.println("Max ticks: " + maxTicks + ", min ticks: " + minTicks);
                    Platform.runLater(mainController::endGame);
                }
            }
        });

        // 运行游戏
        creepingGame.launch();
    }

    /**
     * 继续游戏
     */
    public void resumeGame() {
        creepingGame.resume();
    }

    /**
     * 暂停游戏
     */
    public void suspendGame() {
        creepingGame.suspend();
    }

    /**
     * 终止游戏
     */
    public void terminateGame() {
        isTerminate = true;
        creepingGame.terminate();
    }
}
