package blackjack.enums;

/**
 * 玩家状态，应用于UI层
 */
public enum PlayerStatus {

    /**
     * 空闲，指没有玩家加入的空位
     */
    IDLE,
    /**
     * 玩家已加入但未下注
     */
    AFTER_JOIN,
    /**
     * 玩家已加入且下注
     */
    AFTER_BET,
}
