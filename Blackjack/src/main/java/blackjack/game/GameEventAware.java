package blackjack.game;

import blackjack.entity.Card;
import blackjack.entity.Hand;
import blackjack.enums.GameEvent;
import blackjack.player.Player;

import java.util.Map;

/**
 * << callback >>
 * 游戏事件回调
 */
public interface GameEventAware {


    /**
     * 玩家加入回调函数
     *
     * @param player 玩家信息
     */
    void onPlayerJoin(Player player);

    /**
     * 玩家离开回调函数
     *
     * @param player 玩家信息
     */
    void onPlayerLeave(Player player);

    /**
     * 游戏重启，玩家重置
     *
     * @param player 玩家信息
     */
    void onPlayerReset(Player player);

    /**
     * 游戏结束回调函数
     *
     * @param playerId  摸牌的玩家ID
     * @param handIndex 玩家的第几手牌摸牌
     * @param bet       返回给玩家的金额
     */
    void onGameResult(int playerId, int handIndex, int bet);

    /**
     * playerId玩家回合开始
     *
     * @param playerId 开始回合的玩家
     */
    void onTurnStart(int playerId);

    /**
     * playerId玩家回合结束（停牌，爆掉）
     *
     * @param playerId 回合结束的玩家
     */
    void onTurnEnd(int playerId);

    /**
     * 错误请求的回调
     *
     * @param playerId 玩家
     */
    void onError(int playerId);

    /**
     * playerId玩家摸牌，如果是他人摸牌，card的{@link blackjack.enums.Suit}为Suit.NONE
     *
     * @param playerId  摸牌的玩家ID
     * @param handIndex 玩家的第几手牌摸牌
     * @param newCard   新摸的牌
     */
    void onPlayerHit(int playerId, int handIndex, Card newCard);

    /**
     * 玩家停牌
     *
     * @param playerId  停牌的玩家ID
     * @param handIndex 停牌的第几手牌
     */
    void onPlayerStand(int playerId, int handIndex);

    /**
     * 玩家分牌
     *
     * @param playerId  分牌的玩家ID
     * @param handIndex 分牌的第几手牌
     */
    void onPlayerSplit(int playerId, int handIndex);

    /**
     * 玩家加倍
     *
     * @param playerId  加倍的玩家ID
     * @param handIndex 加倍的第几手牌
     */
    void onPlayerDouble(int playerId, int handIndex);

    /**
     * 玩家上保险
     *
     * @param playerId  上保险的玩家ID
     * @param handIndex 上保险的第几手牌
     */
    void onPlayerInsurance(int playerId, int handIndex);

    /**
     * 玩家放弃手牌
     *
     * @param playerId  放弃的玩家ID
     * @param handIndex 放弃的第几手牌
     */
    void onPlayerSurrender(int playerId, int handIndex);

    /**
     * 给新加入的玩家发送一份玩家列表
     *
     * @param player 玩家
     */
    void onPlayerList(Player player);

    /**
     * 玩家下注
     *
     * @param playerId  下注玩家ID
     * @param handIndex 下注的手牌索引
     * @param amount    下注的量
     */
    void onPlayerBet(int playerId, int handIndex, int amount);

    /**
     * 初始化发牌
     *
     * @param info 初始化信息
     */
    void initEvent(Map<Integer, Hand> info);

    /**
     * 从第一手牌切换到第二手牌
     *
     * @param playerId 玩家ID
     */
    void onNewHand(int playerId);

    void onSendId(int id);

    /**
     * 庄家结算前的反复摸牌
     *
     * @param info 庄家摸牌信息
     */
    void onDealerDeal(Map<Integer, Hand> info);

    /**
     * 触发事件之前回调函数，如果返回false则不会执行实际事件的触发直接
     *
     * @param event 事件
     * @param data  事件参数
     * @return false则不会触发实际事件
     */
    boolean beforeEvent(GameEvent event, Object data);

    /**
     * 触发事件之后的回调函数，无论事件是否实际被触发
     *
     * @param event 事件
     * @param data  事件参数
     */
    void afterEvent(GameEvent event, Object data);


}
