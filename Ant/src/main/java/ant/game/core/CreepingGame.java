package ant.game.core;

import ant.game.lifecycle.GameLifecycle;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 游戏驱动类，需要{@link GameContext}作为游戏的上下文信息 <br/>
 * 负责启动游戏，结束游戏，暂停游戏，恢复游戏以及注册生命周期{@link GameLifecycle}的回调函数
 */
public class CreepingGame implements Runnable {

    /**
     * 游戏上下文信息
     */
    private final GameContext gameContext;

    /**
     * 生命周期回调函数
     */
    private final List<GameLifecycle> lifecycles;

    /**
     * 线程池，负责运行游戏
     */
    private final ScheduledThreadPoolExecutor executor;

    /**
     * 是否请求结束游戏，请求之后会在执行完所有生命周期回调的update方法后结束游戏
     */
    private boolean terminationRequested;

    /**
     * 是否暂停游戏，如果暂停了则不会执行回调函数的update方法
     */
    private boolean suspend = false;

    public CreepingGame(GameContext gameContext) {
        this.gameContext = gameContext;
        lifecycles = new ArrayList<>();
        executor = new ScheduledThreadPoolExecutor(1);
    }

    /**
     * 启动游戏线程
     */
    public void launch() {
        lifecycles.forEach(GameLifecycle::onCreate);

        gameContext.gameStatus = GameStatus.READY;
        executor.scheduleAtFixedRate(this, 0, 1000 / gameContext.getTicksPerSecond(), TimeUnit.MILLISECONDS);
        gameContext.gameStatus = GameStatus.RUNNING;
    }

    /**
     * 请求结束游戏线程，游戏会在跑完一个tick后结束
     */
    public void terminate() {
        terminationRequested = true;
    }

    /**
     * 请求暂停游戏线程
     */
    public void suspend() {
        suspend = true;
        gameContext.gameStatus = GameStatus.SUSPENDED;
    }

    /**
     * 请求恢复游戏线程
     */
    public void resume() {
        suspend = false;
        gameContext.gameStatus = GameStatus.RUNNING;
    }

    /**
     * 注册生命周期对象
     *
     * @param lifecycle /
     */
    public void registerLifecycleHook(GameLifecycle lifecycle) {
        lifecycles.add(lifecycle);
    }

    /**
     * 移除生命周期对象
     *
     * @param lifecycle /
     */
    public void removeLifecycleHook(GameLifecycle lifecycle) {
        lifecycles.remove(lifecycle);
    }

    @Override
    public void run() {
        if(!suspend) {
            gameContext.timeTicks++;
            lifecycles.forEach(GameLifecycle::onUpdate);
            if (terminationRequested) {
                executor.remove(this);
                executor.shutdown();
                //暂停2tick，等待动画结束，否则无法正确初始化
                try {
                    Thread.sleep(2000 / gameContext.getTicksPerSecond());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                // 终止游戏回调
                lifecycles.forEach(GameLifecycle::onDestroy);

                gameContext.gameStatus = GameStatus.TERMINATED;
            }
        }

    }
}
