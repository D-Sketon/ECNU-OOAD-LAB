package blackjack.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 手牌信息参数类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlayerHandsInfo {

    private Map<Integer, Hand> playerHandsInfo;

}
