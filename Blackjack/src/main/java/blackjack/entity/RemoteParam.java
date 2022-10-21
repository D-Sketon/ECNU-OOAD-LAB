package blackjack.entity;

import blackjack.enums.GameEvent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 远程通信参数类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RemoteParam {

    private GameEvent gameEvent;

    private String paramJson;
}
