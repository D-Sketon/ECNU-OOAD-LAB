package blackjack.enums;

/**
 * 基础的游戏事件，应用于适配器通信和UI层
 */
public enum GameEvent {

    /**
     * 玩家加入
     */
    PLAYER_JOIN,
    /**
     * 玩家离开游戏
     */
    PLAYER_LEAVE,
    /**
     * 游戏结束，结算
     */
    GAME_RESULT,
    /**
     * 玩家回合开始
     */
    TURN_START,
    /**
     * 玩家回合结束
     */
    TURN_END,
    /**
     * 初始发牌
     */
    INIT_CARD,
    /**
     * 切换到分牌后的新手牌
     */
    NEW_HAND,
    /**
     * 更新上下文玩家列表
     */
    LIST_CARD,
    /**
     * 错误请求
     */
    ERROR_REQUEST,
    /**
     * 庄家结算前的反复摸牌
     */
    DEALER_DEAL,
    /**
     * 仅仅用于前端显示和刷新按钮，适用于所有消息发生之后
     */
    SHOW_BUTTON,
    /**
     * 重置客户端玩家数据，适用于单局游戏结束后
     */
    PLAYER_RESET,

    // 操作
    /**
     * 摸牌
     */
    HIT,
    /**
     * 停牌
     */
    STAND,
    /**
     * 分牌
     */
    SPLIT,
    /**
     * 加倍
     */
    DOUBLE,
    /**
     * 保险
     */
    INSURANCE,
    /**
     * 投降
     */
    SURRENDER,
    /**
     * 玩家下注
     */
    BET,
    /**
     * 发送玩家Id
     */
    SENDID,
}
