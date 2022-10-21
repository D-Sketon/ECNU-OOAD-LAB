package ant.game.lifecycle;

import ant.game.core.CreepingGame;
import ant.game.core.GameContext;

/**
 * 生命周期回调函数的抽象类，保存游戏上下文信息以及游戏驱动类成员供子类使用
 */
public abstract class AbstractGameLifecycle implements GameLifecycle {

    protected GameContext gameContext;

    protected CreepingGame creepingGame;

    public AbstractGameLifecycle(GameContext gameContext, CreepingGame creepingGame) {
        this.gameContext = gameContext;
        this.creepingGame = creepingGame;
    }

    @Override
    public void onCreate() {

    }

    @Override
    public void onUpdate() {

    }

    @Override
    public void onDestroy() {

    }

}
