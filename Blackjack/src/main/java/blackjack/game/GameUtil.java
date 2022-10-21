package blackjack.game;

import blackjack.entity.Card;
import blackjack.entity.Hand;

import java.util.ArrayList;
import java.util.List;

/**
 * 处理游戏逻辑的工具类
 */
public class GameUtil {

    public static boolean isBlackJack(Hand hand) {
        List<Card> cards = hand.getCards();
        if (cards.size() != 2)
            return false;
        return getPoint(hand).contains(21);
    }


    public static List<Integer> getPoint(Hand hand) {
        List<Integer> points = new ArrayList<>();
        List<Card> cards = hand.getCards();
        boolean hasAce = false;
        int point = 0;
        for (Card card : cards) {
            if (card.getFaceValue() == 1) {
                hasAce = true;
            }
            point += Math.min(card.getFaceValue(), 10);
        }
        points.add(point);
        if (hasAce && point + 10 < 22) {
            points.add(point + 10);
        }
        return points;
    }

    public static boolean isBust(Hand hand) {
        int point = 0;
        for (Card card : hand.getCards()) {
            point += Math.min(card.getFaceValue(), 10);
        }
        return point > 21;
    }

    public static boolean canSplit(List<Card> cards) {
        if (cards.size() != 2)
            return false;

        int value1 = Math.min(cards.get(0).getFaceValue(), 10);
        int value2 = Math.min(cards.get(1).getFaceValue(), 10);

        return value1 == value2;
    }

    public static int near221(Hand hand) {
        boolean hasAce = false;
        int point = 0;

        for (Card card : hand.getCards()) {
            point += Math.min(card.getFaceValue(), 10);
            if (card.getFaceValue() == 1)
                hasAce = true;
        }

        if (hasAce && point <= 11)
            point += 10;
        return point;
    }

    /**
     * 判断ip是否合法
     *
     * @param text ip文本
     * @return 是否合法
     */
    public static boolean ipCheck(String text) {
        if (text != null && !text.isEmpty()) {
            String regex = "^(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|[1-9])\\."
                    + "(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)\\."
                    + "(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)\\."
                    + "(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)$";
            return text.matches(regex);
        }
        return false;
    }

}
