package ant.game.lifecycle;

/**
 * 游戏生命周期回调函数
 */
public interface GameLifecycle {

    /**
     * 游戏启动前调用onCreate方法
     */
    void onCreate();

    /**
     * 每个游戏tick会调用每个回到函数的onUpdate方法
     */
    void onUpdate();

    /**
     * 游戏结束时会调用onDestroy方法
     */
    void onDestroy();
}
