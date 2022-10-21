package blackjack.game;

import blackjack.entity.Card;
import blackjack.enums.Suit;
import blackjack.player.AbstractPlayer;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

/**
 * 游戏上下文，包含一局游戏的基本信息
 */
@Getter
@Setter
public class GameContext {

    /**
     * 游戏ID
     */
    String gameId;

    /**
     * 加入牌局的玩家
     */
    Map<Integer, AbstractPlayer> players;

    /**
     * 牌堆
     */
    List<Card> deck;

    public GameContext() {
        this.gameId = UUID.randomUUID().toString().substring(8);
        this.players = new TreeMap<>();
        this.deck = new ArrayList<>();
    }

    /**
     * 创建牌堆
     * 
     * @param deckNum 牌堆数
     */
    public void createDeck(int deckNum) {
        Suit[] suits = Suit.values();
        for (Suit suit : suits) {
            if (!suit.equals(Suit.NONE)) {
                for (int i = 0; i < deckNum; i++) {
                    for (int j = 1; j <= 13; j++) {
                        deck.add(new Card(j, suit));
                    }
                }
            }
        }
    }

}
