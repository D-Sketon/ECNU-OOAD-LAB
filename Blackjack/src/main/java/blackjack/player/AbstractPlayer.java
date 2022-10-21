package blackjack.player;

import blackjack.entity.Hand;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
public abstract class AbstractPlayer implements Cloneable {

    /**
     * 总共筹码
     */
    protected int balance;

    /**
     * 玩家ID
     */
    protected int id;

    /**
     * 玩家可以有多手牌，每手牌有多张牌
     */
    protected List<Hand> hands;

    /**
     * 玩家是否离开
     */
    protected boolean hasLeave;

    public AbstractPlayer() {
        this.balance = 0;
        this.id = -1;
        this.hands = new ArrayList<>();
        this.hands.add(new Hand());
        this.hasLeave = false;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        AbstractPlayer player = (AbstractPlayer) super.clone();
        player.hands = hands.stream().map(hand -> {
            try {
                return (Hand) hand.clone();
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }
            return null;
        }).collect(Collectors.toList());
        return player;
    }
}
