package blackjack.game;

import blackjack.entity.Card;
import blackjack.entity.Hand;
import blackjack.enums.GameEvent;
import blackjack.player.Player;

import java.util.Map;

public abstract class AbstractGameEventHandler implements GameEventAware {

    protected GameContext gameContext;

    @Override
    public void onPlayerJoin(Player player) {

    }

    @Override
    public void onPlayerLeave(Player player) {

    }

    @Override
    public void onGameResult(int playerId, int handIndex, int bet) {

    }

    @Override
    public void onTurnStart(int playerId) {

    }

    @Override
    public void onTurnEnd(int playerId) {

    }

    @Override
    public void onError(int playerId) {

    }

    @Override
    public void onPlayerHit(int playerId, int handIndex, Card newCard) {

    }

    @Override
    public void onPlayerStand(int playerId, int handIndex) {

    }

    @Override
    public void onPlayerSplit(int playerId, int handIndex) {

    }

    @Override
    public void onPlayerDouble(int playerId, int handIndex) {

    }

    @Override
    public void onPlayerInsurance(int playerId, int handIndex) {

    }

    @Override
    public void onPlayerBet(int playerId, int handIndex, int amount) {

    }

    @Override
    public void onPlayerSurrender(int playerId, int handIndex) {

    }

    @Override
    public void onPlayerList(Player player) {
    }

    @Override
    public void onNewHand(int playerId) {

    }

    @Override
    public void onDealerDeal(Map<Integer, Hand> info) {

    }

    @Override
    public void onSendId(int id) {
    }


    @Override
    public void initEvent(Map<Integer, Hand> info) {

    }

    @Override
    public void onPlayerReset(Player player) {

    }

    @Override
    public boolean beforeEvent(GameEvent event, Object data) {
        return true;
    }

    @Override
    public void afterEvent(GameEvent event, Object data) {

    }

}
