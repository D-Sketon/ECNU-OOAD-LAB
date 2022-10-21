package ant.game.core;

import ant.game.entity.Ant;
import ant.game.entity.Rod;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

/**
 * <p>游戏场景，即一次游戏的上下文信息</p>
 * <p>包含了游戏内的对象信息，游戏ID，游戏运行速度，游戏的状态，经过的时间</p>
 */
public class GameContext {

    /**
     * 游戏ID，创建对象时随机分配
     */
    @Setter
    @Getter
    private String gameId;

    /**
     * 每秒更新几次游戏
     */
    @Setter
    @Getter
    private int ticksPerSecond;

    /**
     * 蚂蚁对象
     */
    @Getter
    private final List<Ant> ants;

    /**
     * 杆子对象
     */
    @Setter
    @Getter
    private Rod rod;

    /**
     * 当前游戏经过的tick
     */
    @Getter
    long timeTicks;

    /**
     * 游戏状态
     */
    @Getter
    GameStatus gameStatus;

    public GameContext() {
        ants = new ArrayList<>();
        timeTicks = 0;
        gameStatus = GameStatus.CREATED;
    }

    public GameContext(int ticksPerSecond) {
        this.ticksPerSecond = ticksPerSecond;
        ants = new ArrayList<>();
        timeTicks = 0;
        gameStatus = GameStatus.CREATED;
    }

    public void addAnt(Ant ant) {
        ants.add(ant);
    }

    public void addAnt(Collection<Ant> ants) {
        this.ants.addAll(ants);
    }

    public void removeAnt(Ant ant) {
        ants.remove(ant);
    }

    public void removeAnts(Collection<Ant> ants) {
        this.ants.removeAll(ants);
    }

}
