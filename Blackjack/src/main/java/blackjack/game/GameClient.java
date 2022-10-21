package blackjack.game;

import blackjack.adapter.CommunicationAdapter;
import blackjack.entity.ActionParam;
import blackjack.entity.Card;
import blackjack.entity.Hand;
import blackjack.enums.GameEvent;
import blackjack.network.ClientOnline;
import blackjack.player.AbstractPlayer;
import blackjack.player.Dealer;
import blackjack.player.Player;
import blackjack.ui.MainController;
import javafx.animation.ParallelTransition;
import javafx.application.Platform;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@Slf4j
public class GameClient extends AbstractGameEventHandler {

    private int currentHand;
    private int playerId;
    private MainController mainController;
    private CommunicationAdapter communicationAdapter;
    private ClientOnline clientOnline;

    private boolean isTurn;

    public GameClient() {
        this.gameContext = new GameContext();
        this.gameContext.getPlayers().put(-1, new Dealer());
        this.currentHand = 0;
        clientOnline = new ClientOnline();
    }

    public GameClient(MainController mainController, CommunicationAdapter communicationAdapter) {
        this();
        this.mainController = mainController;
        this.communicationAdapter = communicationAdapter;
    }

    private void communicate(GameEvent event, Object data) {
        log.info("PlayerId = " + playerId + " communicates to Server with " + event + " as " + data);
        communicationAdapter.sendEvent(event, data);
    }

    @Override
    public void onPlayerJoin(Player player) {
        log.info("PlayerId = " + player.getId() + " onPlayerJoin");
        gameContext.getPlayers().put(player.getId(), player);
        Platform.runLater(() -> mainController.renderPlayerJoin(player.getId()));

    }

    @Override
    public void onPlayerLeave(Player player) {
        log.info("PlayerId = " + player.getId() + " onPlayerLeave");
        gameContext.getPlayers().remove(player.getId());
        Platform.runLater(() -> mainController.renderPlayerLeave(player.getId()));
    }

    @Override
    public void onPlayerReset(Player player) {
        log.info("PlayerId = " + player.getId() + " onPlayerReset");
        gameContext.getPlayers().remove(player.getId());
        gameContext.getPlayers().put(player.getId(), player);
    }

    @Override
    public void onGameResult(int playerId, int handIndex, int bet) {
        int balance = gameContext.getPlayers().get(playerId).getBalance();
        gameContext.getPlayers().get(playerId).setBalance(balance + bet);
        if (playerId == this.playerId) {
            Platform.runLater(() -> mainController.renderBalance(bet));
        }
        System.out.println(gameContext.getPlayers().get(playerId).getHands());
        Hand hand = gameContext.getPlayers().get(playerId).getHands().get(handIndex);
        int realBet = hand.getBet();
        int insurance = 0;
        if (hand.isInsured()) insurance = realBet / 2;
        if (hand.isDoubled()) realBet *= 2;
        realBet += insurance;
        final int realBetFinal = realBet;
        log.info("PlayerId = " + playerId + " Bet = " + bet + " RealBet =" + realBet + " onGameResult");
        Platform.runLater(() -> mainController.renderGameResult(playerId, handIndex, bet, realBetFinal));
    }

    @Override
    public void onTurnStart(int playerId) {
        log.info("PlayerId = " + playerId + " onTurnStart");
        if (playerId == this.playerId) {
            isTurn = true;
            Platform.runLater(() -> mainController.showButton(onPlayerButton()));
        }
        Platform.runLater(() -> mainController.renderPointLabel(playerId, 0));
    }

    @Override
    public void onTurnEnd(int playerId) {
        log.info("PlayerId = " + playerId + " onTurnEnd");
        if (playerId == this.playerId) {
            isTurn = false;
            Platform.runLater(() -> mainController.renderTurnEnd());
        }
    }

    @Override
    public void onError(int playerId) {
        log.error("Receive ERROR message...");
        Platform.runLater(() -> MainController.showErrorDialog("异常请求！"));
    }

    @Override
    public void onPlayerHit(int playerId, int handIndex, Card newCard) {
        log.info("PlayerId = " + playerId + " HandIndex = " + handIndex + " NewCard = " + newCard + " onPlayerHit");
        AbstractPlayer player = gameContext.getPlayers().get(playerId);
        Hand hand = player.getHands().get(handIndex);
        hand.getCards().add(newCard);
        hand.setStart(true);
        Platform.runLater(() -> mainController.renderPlayerHit(playerId, newCard, handIndex));
        Platform.runLater(() -> mainController.updatePoint(playerId, handIndex, GameUtil.near221(hand)));
    }

    @Override
    public void onPlayerStand(int playerId, int handIndex) {
        log.info("PlayerId = " + playerId + " HandIndex = " + handIndex + " onPlayerStand");
        AbstractPlayer player = gameContext.getPlayers().get(playerId);
        Hand hand = player.getHands().get(handIndex);
        hand.setStood(true);
    }

    @Override
    public void onPlayerSplit(int playerId, int handIndex) {
        log.info("PlayerId = " + playerId + " HandIndex = " + handIndex + " onPlayerSplit");
        AbstractPlayer player = gameContext.getPlayers().get(playerId);
        Hand hand = player.getHands().get(handIndex);
        Hand newHand = new Hand();

        newHand.getCards().add(hand.getCards().remove(1));

        newHand.setBet(hand.getBet());
        newHand.setStart(true);
        hand.setStart(true);
        player.getHands().add(newHand);
        player.setBalance(player.getBalance() - hand.getBet());
        Platform.runLater(() -> {
            ParallelTransition parallelTransition = mainController.renderPlayerSplit(playerId);
            parallelTransition.setOnFinished((e) -> {
                mainController.updatePoint(playerId, 0, GameUtil.near221(hand));
                mainController.initPoint(playerId, 1, GameUtil.near221(hand));
                mainController.addChip(hand.getBet(), playerId, 1).play();
                mainController.showButton(onPlayerButton());
                mainController.renderBalance(-hand.getBet());
            });
            parallelTransition.play();
        });
    }

    @Override
    public void onPlayerDouble(int playerId, int handIndex) {
        log.info("PlayerId = " + playerId + " HandIndex = " + handIndex + " onPlayerDouble");
        AbstractPlayer player = gameContext.getPlayers().get(playerId);
        Hand hand = player.getHands().get(handIndex);
        player.setBalance(player.getBalance() - hand.getBet());
        hand.setDoubled(true);
        hand.setStart(true);
        Platform.runLater(() -> mainController.addChip(hand.getBet(), playerId, handIndex).play());
        Platform.runLater(() -> mainController.renderBalance(-hand.getBet()));
        Platform.runLater(() -> mainController.showButton(onPlayerButton()));
    }

    @Override
    public void onPlayerInsurance(int playerId, int handIndex) {
        log.info("PlayerId = " + playerId + " HandIndex = " + handIndex + " onPlayerInsurance");
        Hand hand = gameContext.getPlayers().get(playerId).getHands().get(handIndex);
        hand.setInsured(true);
        hand.setStart(true);
        Platform.runLater(() -> mainController.addChip(hand.getBet() / 2, playerId, handIndex).play());
        Platform.runLater(() -> mainController.renderBalance(-hand.getBet() / 2));
        Platform.runLater(() -> mainController.showButton(onPlayerButton()));
    }

    @Override
    public void onPlayerBet(int playerId, int handIndex, int amount) {
        log.info("PlayerId = " + playerId + " HandIndex = " + handIndex + " Amount = " + amount + " onPlayerBet");
        AbstractPlayer player = gameContext.getPlayers().get(playerId);
        player.setBalance(player.getBalance() - amount);
        gameContext.getPlayers().get(playerId).getHands().get(handIndex).setBet(amount);
        Platform.runLater(() -> mainController.renderPlayerBet(playerId, handIndex, amount, playerId == this.playerId));
    }

    @Override
    public void onPlayerSurrender(int playerId, int handIndex) {
        AbstractPlayer player = gameContext.getPlayers().get(playerId);
        Hand hand = player.getHands().get(handIndex);
        log.info("PlayerId = " + playerId + " HandIndex = " + handIndex + " onPlayerSurrender");
        hand.setSurrender(true);
        hand.setStart(true);
        if (playerId == this.playerId) {
            Platform.runLater(() -> mainController.showButton(onPlayerButton()));
        }
    }

    @Override
    public void initEvent(Map<Integer, Hand> info) {
        log.info("initEvent");
        Map<Integer, Hand> afterProcessInfo = new HashMap<>();
        for (Map.Entry<Integer, Hand> integerHandEntry : info.entrySet()) {
            try {
                Hand newHand = (Hand) integerHandEntry.getValue().clone();
                gameContext.getPlayers().get(integerHandEntry.getKey()).getHands().set(0, newHand);
                afterProcessInfo.put(integerHandEntry.getKey(), integerHandEntry.getValue());
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }
        }
        Platform.runLater(() -> mainController.renderInitialDeal(afterProcessInfo));
    }

    @Override
    public void onPlayerList(Player player) {
        gameContext.getPlayers().put(player.getId(), player);
        Platform.runLater(() -> mainController.renderPlayerJoin(player.getId()));
        if (player.getHands().get(0).getBet() != 0) {
            Platform.runLater(() -> mainController.renderPlayerBet(player.getId(), 0, player.getHands().get(0).getBet(), false));
        }

    }

    @Override
    public void onNewHand(int playerId) {
        log.info("PlayerId = " + playerId + " onNewHand");
        if (this.playerId == playerId) {
            currentHand++;
        }
        Platform.runLater(() -> mainController.showButton(onPlayerButton()));
        Platform.runLater(() -> mainController.renderPointLabel(playerId, 1));
    }

    @Override
    public void onDealerDeal(Map<Integer, Hand> info) {
        log.info("CardInfo = " + info + " onDealerDeal");
        Hand infoHand = info.get(-1);
        List<Card> cards = gameContext.getPlayers().get(-1).getHands().get(0).getCards();
        cards.set(0, infoHand.getCards().get(0));
        for (int i = 1; i < infoHand.getCards().size(); i++) {
            cards.add(infoHand.getCards().get(i));
        }
        Platform.runLater(() -> mainController.renderDealerDeal(infoHand));
    }

    @Override
    public void onSendId(int id) {
        this.playerId = id;
        Platform.runLater(() -> mainController.connectSuccessHandler());
    }

    public void onPlayerHit() {
        if (GameUtil.isBust(gameContext.getPlayers().get(playerId).getHands().get(currentHand))) {
            if (currentHand + 1 == gameContext.getPlayers().get(playerId).getHands().size())
                return;
        }
        communicate(GameEvent.HIT, new ActionParam(playerId, currentHand, null, null));
    }


    public void onPlayerStand() {
        communicate(GameEvent.STAND, new ActionParam(playerId, currentHand, null, null));
    }

    public void onPlayerSplit() {
        communicate(GameEvent.SPLIT, new ActionParam(playerId, currentHand, null, null));
    }

    public void onPlayerDouble() {
        communicate(GameEvent.DOUBLE, new ActionParam(playerId, currentHand, null, null));
    }

    public void onPlayerInsurance() {
        communicate(GameEvent.INSURANCE, new ActionParam(playerId, currentHand, null, null));
    }

    public void onPlayerSurrender() {
        communicate(GameEvent.SURRENDER, new ActionParam(playerId, currentHand, null, null));
    }

    public void onPlayerBet(int bet) {
        communicate(GameEvent.BET, new ActionParam(playerId, currentHand, null, bet));
    }

    public void onRestartGame() {
        currentHand = 0;
    }

    public Map<GameEvent, Boolean> onPlayerButton() {
        if (!this.isTurn)
            return null;

        Map<GameEvent, Boolean> result = new HashMap<>();
        AbstractPlayer player = gameContext.getPlayers().get(playerId);
        Hand hand = player.getHands().get(currentHand);
        int balance = player.getBalance();

        //已经投降或者停牌，什么都不显示
        if (hand.isSurrender() || hand.isStood())
            return null;

        /*保险*/
        if (gameContext.getPlayers().get(-1).getHands().get(0).getCards().get(1).getFaceValue() != 1 || hand.isStart() || balance < hand.getBet() / 2) {
            result.put(GameEvent.INSURANCE, false);
        } else {
            result.put(GameEvent.INSURANCE, true);
        }

        /*摸牌*/
        if (GameUtil.near221(hand) >= 21) {
            result.put(GameEvent.HIT, false);
        } else {
            result.put(GameEvent.HIT, true);
        }

        /*停牌*/
        result.put(GameEvent.STAND, true);

        /*投降*/
        if (hand.isStart()) {
            result.put(GameEvent.SURRENDER, false);
        } else {
            result.put(GameEvent.SURRENDER, true);
        }

        /*加倍*/
        if (balance < hand.getBet() || hand.isStart()) {
            result.put(GameEvent.DOUBLE, false);
        } else {
            result.put(GameEvent.DOUBLE, true);
        }

        /*分牌*/
        if (balance < hand.getBet() || hand.isStart() || !GameUtil.canSplit(hand.getCards())) {
            result.put(GameEvent.SPLIT, false);
        } else {
            result.put(GameEvent.SPLIT, true);
        }

        return result;
    }
}
