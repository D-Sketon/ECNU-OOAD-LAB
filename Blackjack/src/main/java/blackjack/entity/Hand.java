package blackjack.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 手牌实体类
 */
@Data
@AllArgsConstructor
public class Hand implements Cloneable {

    /**
     * 手牌里的牌
     */
    private List<Card> cards;

    /**
     * 当前手牌的赌注
     */
    private int bet;

    /**
     * 是否加倍，加倍时不改变初始赌注
     */
    private boolean isDoubled;

    /**
     * 是否保险，保险时不改变初始赌注
     */
    private boolean isInsured;

    /**
     * 是否停牌
     */
    private boolean isStood;

    /**
     * 是否爆牌
     */
    private boolean isBusted;

    /**
     * 是否投降
     */
    private boolean isSurrender;

    /**
     * 是否已经摸了一张牌，用于判断是否可以加倍，保险，投降，分牌
     */
    private boolean isStart;

    public Hand() {
        this.cards = new ArrayList<>();
        this.bet = 0;
        this.isDoubled = false;
        this.isInsured = false;
        this.isStood = false;
        this.isBusted = false;
        this.isSurrender = false;
        this.isStart = false;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        Hand hand = (Hand) super.clone();
        cards = new ArrayList<>(cards);
        return hand;
    }
}
