package blackjack.adapter;

import blackjack.enums.GameEvent;

public interface CommunicationAdapter {

    /**
     * 除了开始游戏、结束游戏、玩家加入，玩家离开事件等，其他情况data都传{@link blackjack.entity.ActionParam}
     *
     * @param event 游戏事件
     * @param data 事件数据
     */
    void sendEvent(GameEvent event, Object data);

}
