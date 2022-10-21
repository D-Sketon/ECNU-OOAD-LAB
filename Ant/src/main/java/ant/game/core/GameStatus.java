package ant.game.core;

/**
 * 游戏状态
 */
public enum GameStatus {
    /**
     * 刚new完的状态
     */
    CREATED,
    /**
     * 准备运行状态
     */
    READY,
    /**
     * 正在运行
     */
    RUNNING,
    /**
     * 游戏结束
     */
    TERMINATED,
    /**
     * 游戏暂停
     */
    SUSPENDED
}
