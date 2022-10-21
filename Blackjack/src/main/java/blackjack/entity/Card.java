package blackjack.entity;

import blackjack.enums.Suit;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 纸牌实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Card {

    /**
     * 暗牌
     */
    public static final Card FACE_DOWN_CARD = new Card(-1, Suit.NONE);

    /**
     * 卡面面值
     */
    private int faceValue;
    /**
     * 花色
     */
    private Suit suit;

    public void setFaceValue(int faceValue) {
        if (faceValue >= 1 && faceValue <= 13) {
            this.faceValue = faceValue;
        }
    }
}
