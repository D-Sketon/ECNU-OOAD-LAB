package blackjack.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 基础游戏活动参数类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActionParam {

    private Integer playerId;

    private Integer handIndex;

    private Card card;

    private Integer bet;
}
