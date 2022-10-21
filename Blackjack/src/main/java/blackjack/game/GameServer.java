package blackjack.game;

import blackjack.adapter.CommunicationAdapter;
import blackjack.adapter.LocalGameAdapter;
import blackjack.adapter.RemoteGameAdapter;
import blackjack.entity.*;
import blackjack.enums.GameEvent;
import blackjack.network.ServerOnline;
import blackjack.player.AbstractPlayer;
import blackjack.player.Dealer;
import blackjack.player.Player;
import com.google.gson.Gson;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class GameServer extends AbstractGameEventHandler {
    @Getter
    private final Map<Integer, CommunicationAdapter> adapterMap;

    private final List<AbstractPlayer> playerOrder;

    private final ServerOnline serverOnline;

    private final int lsto = 25;

    private int currentPlayerIndex;

    private int timer;

    private boolean timerStart;

    private AbstractPlayer currentPlayer;

    private final boolean[] PLAYER_SLOT = {false, false, false, false, false};

    private final int PLAYER_NUM = 5;

    @Getter
    private boolean isGameStart;

    public GameServer() {
        GameContext serverContext = new GameContext();
        serverContext.createDeck(4);
        this.gameContext = serverContext;
        this.adapterMap = new ConcurrentHashMap<>();
        this.playerOrder = new ArrayList<>();
        this.currentPlayerIndex = 0;
        this.serverOnline = new ServerOnline(this);
        this.timer = 0;
        this.timerStart = false;
        this.isGameStart = false;

    }

    /**
     * 本地玩家加入游戏（通过直接调用方法）
     *
     * @param communicator 用于通信
     */
    public void joinGameLocal(GameEventAware communicator) {
        log.info("New client tries to joinGameLocal...");
        int playerId;
        for (playerId = 0; playerId < PLAYER_NUM; playerId++) {
            if (!PLAYER_SLOT[playerId]) {
                PLAYER_SLOT[playerId] = true;
                break;
            }
        }
        Player player = new Player(playerId, 1000);
        gameContext.getPlayers().put(player.getId(), player);

        GameClient gameClient = (GameClient) communicator;
        gameClient.setPlayerId(player.getId());

        LocalGameAdapter adapter = new LocalGameAdapter(communicator);

        onPlayerList(adapter);
        adapterMap.put(player.getId(), adapter);

        broadcast(GameEvent.PLAYER_JOIN, player);

    }

    /**
     * 远程玩家加入游戏（通过netty）
     *
     * @param adapter 远程适配器
     */
    public Player joinGameRemote(RemoteGameAdapter adapter) {
        log.info("New client tries to joinGameRemote...");
        int playerId;
        for (playerId = 0; playerId < PLAYER_NUM; playerId++) {
            if (!PLAYER_SLOT[playerId]) {
                PLAYER_SLOT[playerId] = true;
                break;
            }
        }
        Player player = new Player(playerId, 1000);
        gameContext.getPlayers().put(player.getId(), player);

        sendPlayerId(player.getId(), adapter);
        onPlayerList(adapter);
        adapterMap.put(player.getId(), adapter);

        broadcast(GameEvent.PLAYER_JOIN, player);

        return player;
    }

    /**
     * 仅用于远程连接有玩家加入时回传玩家Id
     *
     * @param id      玩家Id
     * @param adapter 适配器
     */
    private void sendPlayerId(Integer id, CommunicationAdapter adapter) {
        RemoteGameAdapter remoteGameAdapter = (RemoteGameAdapter) adapter;
        String json = id.toString();
        RemoteParam param = new RemoteParam();
        param.setGameEvent(GameEvent.SENDID);
        param.setParamJson(json);
        try {
            remoteGameAdapter.getChannel().writeAndFlush(new Gson().toJson(param) + "\n").sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 开启端口，在局域网内部启动在线服务器
     */
    public void startServer() {
        try {
            serverOnline.initNetty();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 给指定玩家发消息
     *
     * @param playerId 玩家ID
     * @param event    事件
     * @param data     事件的参数
     */
    private void sendToPlayer(int playerId, GameEvent event, Object data) {
        log.info("Server communicates to PlayerId = " + playerId + " with " + event + " as " + data);
        adapterMap.get(playerId).sendEvent(event, data);
    }

    /**
     * 向所有玩家广播消息
     *
     * @param event 游戏事件
     * @param data  事件消息
     */
    private void broadcast(GameEvent event, Object data) {
        log.info("Server broadcasts to all clients with " + event + " as " + data);
        adapterMap.forEach((playerId, adapter) -> adapter.sendEvent(event, data));
    }

    /**
     * 开启一局游戏，包含加入庄家，洗牌，初始发牌和通知第一名玩家
     */
    public void startGame() {
        log.info("Try to start...");
        isGameStart = true;
        joinDealer();
        shuffle();
        initialDeal();
        startFromFirstPlayer();
        initTimer();
        log.info("Finish start...");
    }

    /**
     * 获取下一个玩家信息并广播通知玩家
     *
     * @return null如果所有玩家的回合已经结束（到庄家）
     */
    private AbstractPlayer notifyAndGetNextPlayer() {
        // 如果没有下个玩家，则庄家摸牌到17点后结算所有
        if (++currentPlayerIndex >= playerOrder.size()) {
            dealerDeal();
            onGameResult(-1, -1, -1);
            timerStart = false;
            timer = 0;
            currentPlayer = null;
            return null;
        }
        AbstractPlayer nextPlayer = playerOrder.get(currentPlayerIndex);
        int points = GameUtil.near221(nextPlayer.getHands().get(0));
        if (points == 21) {
            // 初始摸牌21点应该直接跳过
            nextPlayer.getHands().get(0).setStart(true);
            return notifyAndGetNextPlayer();
        }
        ActionParam param = new ActionParam();
        param.setPlayerId(nextPlayer.getId());
        timerStart = true;
        timer = 0;
//        adapterMap.get(nextPlayer.getId()).sendEvent(GameEvent.TURN_START, param);
        broadcast(GameEvent.TURN_START, param);
        currentPlayer = nextPlayer;
        return nextPlayer;
    }

    /**
     * 从第一名玩家开始
     */
    private void startFromFirstPlayer() {
        gameContext.getPlayers().forEach((integer, abstractPlayer) -> playerOrder.add(abstractPlayer));
        notifyAndGetNextPlayer();
    }

    /**
     * 在游戏中加入庄家
     */
    private void joinDealer() {
        Dealer dealer = new Dealer();
        gameContext.getPlayers().put(dealer.getId(), dealer);
    }

    /**
     * 给新加入的玩家发送一份玩家列表，用于同步
     */
    public void onPlayerList(CommunicationAdapter adapter) {
        for (AbstractPlayer player : gameContext.getPlayers().values()) {
            adapter.sendEvent(GameEvent.LIST_CARD, player);
        }
    }

    /**
     * 基础洗牌
     */
    private void shuffle() {
        log.info("Try to shuffle...");
        Collections.shuffle(gameContext.deck);
    }

    /**
     * 系统初始发牌
     */
    private void initialDeal() {
        log.info("Try to initialDeal...");
        Map<Integer, AbstractPlayer> players = gameContext.getPlayers();
        for (int i = 0; i < 2; i++) {
            for (Map.Entry<Integer, AbstractPlayer> entry : players.entrySet()) {
                AbstractPlayer player = entry.getValue();
                Card card = deal();
                player.getHands().get(0).getCards().add(card);
            }
        }
        Map<Integer, Hand> info = new TreeMap<>();
        players.forEach((playerId, player) -> {
            try {
                Hand newhand = (Hand) player.getHands().get(0).clone();
                if (player.getId() == -1) {
                    player.getHands().get(0).getCards().get(0).setFaceValue(9);
                    newhand.getCards().set(0, Card.FACE_DOWN_CARD);
                }
                info.put(player.getId(), newhand);
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }
        });
        broadcast(GameEvent.INIT_CARD, new PlayerHandsInfo(info));
    }

    /**
     * 从总牌堆中摸出一张牌
     */
    private Card deal() {
        List<Card> deck = gameContext.deck;
        return deck.remove(deck.size() - 1);
    }

    @Override
    public void onPlayerJoin(Player player) {
        log.info("Player = " + player + " onPlayerJoin");
        gameContext.getPlayers().put(player.getId(), player);
    }

    @Override
    public void onPlayerLeave(Player player) {
        if (player.getHands().get(0).getBet() > 0) {
            player.setHasLeave(true);
            adapterMap.remove(player.getId());
            return;
        }
        log.info("Player = " + player + " onPlayerLeave");
        gameContext.getPlayers().remove(player.getId());
        adapterMap.remove(player.getId());
        PLAYER_SLOT[player.getId()] = false;
        broadcast(GameEvent.PLAYER_LEAVE, player);
    }

    @Override
    public void onGameResult(int playerId, int handIndex, int bet) {
        log.info("PlayerId = " + playerId + " Bet = " + bet + " onGameResult");
        AbstractPlayer dealer = playerOrder.get(0);
        for (AbstractPlayer player : playerOrder) {
            if (player.getId() == -1) {
                continue;
            }
            for (int i = 0; i < player.getHands().size(); i++) {
                calculateHandResult(player, dealer, i);
            }
        }
        isGameStart = false;
        log.info("Try to restart...");
        GameContext serverContext = new GameContext();
        serverContext.createDeck(4);
        for (AbstractPlayer player : this.gameContext.getPlayers().values()) {
            if (player.isHasLeave()) {
                PLAYER_SLOT[player.getId()] = true;
                this.gameContext.getPlayers().remove(player.getId());
                broadcast(GameEvent.PLAYER_LEAVE, player);
            } else {
                Player newPlayer = new Player(player.getId(), player.getBalance());
                serverContext.getPlayers().put(newPlayer.getId(), newPlayer);
                broadcast(GameEvent.PLAYER_RESET, newPlayer);
            }
        }
        playerOrder.clear();
        currentPlayerIndex = 0;
        this.gameContext = serverContext;
        log.info("Finish restart...");
    }

    /**
     * 计算玩家该手牌的最终结果
     *
     * @param player    玩家
     * @param dealer    庄家
     * @param handIndex 手牌索引
     */
    private void calculateHandResult(AbstractPlayer player, AbstractPlayer dealer, int handIndex) {
        Hand hand = player.getHands().get(handIndex);
        int insurance = 0;
        int otherBet = 0;
        Hand dealHand = dealer.getHands().get(0);
        if (hand.isSurrender()) {
            otherBet = hand.getBet() / 2;
            broadcast(GameEvent.GAME_RESULT, new ActionParam(player.getId(), handIndex, null, otherBet + insurance));
            player.setBalance(player.getBalance() + otherBet + insurance);
            return;
        }
        if (hand.isInsured()) {
            if (GameUtil.isBlackJack(dealHand)) {
                insurance = 3 * hand.getBet() / 2;
            }
        }
        if (hand.isBusted() ||
                !GameUtil.isBlackJack(hand) && GameUtil.isBlackJack(dealHand) ||
                !GameUtil.isBust(dealHand) && GameUtil.near221(dealHand) > GameUtil.near221(hand)) {
            otherBet = 0;
        } else if (GameUtil.isBlackJack(hand) && GameUtil.isBlackJack(dealHand) ||
                GameUtil.near221(dealHand) == GameUtil.near221(hand)) {
            otherBet = hand.getBet();
        } else {
            if (GameUtil.isBlackJack(hand)) {
                otherBet = 3 * hand.getBet();
            } else {
                otherBet = 2 * hand.getBet();
            }
        }
        if (hand.isDoubled())
            otherBet *= 2;
        player.setBalance(player.getBalance() + otherBet + insurance);
        broadcast(GameEvent.GAME_RESULT, new ActionParam(player.getId(), handIndex, null, otherBet + insurance));
    }

    @Override
    public void onPlayerHit(int playerId, int handIndex, Card newCard) {
        List<Hand> hands = gameContext.getPlayers().get(playerId).getHands();
        Hand hand = hands.get(handIndex);
        if (GameUtil.near221(hand) >= 21 || hand.isStood()) {
            log.error("PlayerId = " + playerId + " HandIndex = " + handIndex + " onPlayerHit");
            sendToPlayer(playerId, GameEvent.ERROR_REQUEST, new ActionParam(playerId, null, null, null));
            return;
        }
        log.info("PlayerId = " + playerId + " HandIndex = " + handIndex + " onPlayerHit");
        hand.setStart(true);
        resetTimer(playerId, false);
        Card card = deal();
        hand.getCards().add(card);
        ActionParam param = new ActionParam(playerId, handIndex, card, null);
        broadcast(GameEvent.HIT, param);
        // 判断21点直接跳过
        if (GameUtil.near221(hand) == 21) {
            standOrBustHandler(hands, playerId, handIndex);
        } else if (GameUtil.isBust(hand)) {
            hand.setBusted(true);
            standOrBustHandler(hands, playerId, handIndex);
        }
    }

    @Override
    public void onPlayerStand(int playerId, int handIndex) {
        AbstractPlayer player = gameContext.getPlayers().get(playerId);
        List<Hand> hands = player.getHands();
        Hand hand = hands.get(handIndex);
        if (hand.isStood()) {
            log.error("PlayerId = " + playerId + " HandIndex = " + handIndex + " onPlayerHit");
            sendToPlayer(playerId, GameEvent.ERROR_REQUEST, new ActionParam(playerId, null, null, null));
            return;
        }
        log.info("PlayerId = " + playerId + " HandIndex = " + handIndex + " onPlayerStand");
        resetTimer(playerId, true);
        hand.setStood(true);
        standOrBustHandler(hands, playerId, handIndex);
    }

    @Override
    public void onPlayerSplit(int playerId, int handIndex) {
        AbstractPlayer player = gameContext.getPlayers().get(playerId);
        Hand hand = player.getHands().get(handIndex);
        int balance = player.getBalance();
        if (hand.isStart()) {
            log.error("PlayerId = " + playerId + " HandIndex = " + handIndex + " onPlayerSplit");
            sendToPlayer(playerId, GameEvent.ERROR_REQUEST, new ActionParam(playerId, null, null, null));
            return;
        }
        // 判断资产是否足够
        if (balance < hand.getBet()) {
            log.error("PlayerId = " + playerId + " HandIndex = " + handIndex + " onPlayerSplit");
            sendToPlayer(playerId, GameEvent.ERROR_REQUEST, new ActionParam(playerId, null, null, null));
            return;
        }
        // 判断牌数是否为2且面值相同
        if (!GameUtil.canSplit(hand.getCards())) {
            log.error("PlayerId = " + playerId + " HandIndex = " + handIndex + " onPlayerSplit");
            sendToPlayer(playerId, GameEvent.ERROR_REQUEST, new ActionParam(playerId, null, null, null));
            return;
        }
        log.info("PlayerId = " + playerId + " HandIndex = " + handIndex + " onPlayerSplit");
        resetTimer(playerId, false);
        player.setBalance(balance - hand.getBet());
        Hand newHand = new Hand();
        newHand.getCards().add(hand.getCards().get(1));
        hand.getCards().remove(1);
        newHand.setBet(hand.getBet());
        player.getHands().add(newHand);
        hand.setStart(true);
        newHand.setStart(true);
        broadcast(GameEvent.SPLIT, new ActionParam(playerId, handIndex, null, null));
    }

    @Override
    public void onPlayerInsurance(int playerId, int handIndex) {
        if (gameContext.getPlayers().get(-1).getHands().get(0).getCards().get(1).getFaceValue() != 1) {
            log.error("PlayerId = " + playerId + " HandIndex = " + handIndex + " onPlayerInsurance");
            sendToPlayer(playerId, GameEvent.ERROR_REQUEST, new ActionParam(playerId, null, null, null));
            return;
        }
        log.info("PlayerId = " + playerId + " HandIndex = " + handIndex + " onPlayerInsurance");
        AbstractPlayer player = gameContext.getPlayers().get(playerId);
        Hand hand = gameContext.getPlayers().get(playerId).getHands().get(handIndex);

        if (hand.isStart()) {
            log.error("PlayerId = " + playerId + " HandIndex = " + handIndex + " onPlayerDouble");
            sendToPlayer(playerId, GameEvent.ERROR_REQUEST, new ActionParam(playerId, null, null, null));
            return;
        }
        int balance = player.getBalance();
        // 判断资产是否足够
        if (balance < hand.getBet()) {
            log.error("PlayerId = " + playerId + " HandIndex = " + handIndex + " onPlayerDouble");
            sendToPlayer(playerId, GameEvent.ERROR_REQUEST, new ActionParam(playerId, null, null, null));
            return;
        }
        resetTimer(playerId, false);
        player.setBalance(player.getBalance() - balance);
        hand.setInsured(true);
        hand.setStart(true);
        broadcast(GameEvent.INSURANCE, new ActionParam(playerId, handIndex, null, null));
    }

    @Override
    public void onPlayerDouble(int playerId, int handIndex) {
        AbstractPlayer player = gameContext.getPlayers().get(playerId);
        Hand hand = player.getHands().get(handIndex);
        if (hand.isStart()) {
            log.error("PlayerId = " + playerId + " HandIndex = " + handIndex + " onPlayerDouble");
            sendToPlayer(playerId, GameEvent.ERROR_REQUEST, new ActionParam(playerId, null, null, null));
            return;
        }
        int balance = player.getBalance();
        // 判断资产是否足够
        if (balance < hand.getBet()) {
            log.error("PlayerId = " + playerId + " HandIndex = " + handIndex + " onPlayerDouble");
            sendToPlayer(playerId, GameEvent.ERROR_REQUEST, new ActionParam(playerId, null, null, null));
            return;
        }




        log.info("PlayerId = " + playerId + " HandIndex = " + handIndex + " onPlayerDouble");
        player.setBalance(player.getBalance() - hand.getBet());
        hand.setDoubled(true);
        hand.setStart(true);
        broadcast(GameEvent.DOUBLE, new ActionParam(playerId, handIndex, null, null));

        // 加倍后再抽一次牌面
        this.onPlayerHit(playerId, handIndex, null);
    }

    @Override
    public void onPlayerBet(int playerId, int handIndex, int amount) {
        AbstractPlayer player = gameContext.getPlayers().get(playerId);
        int balance = player.getBalance();
        // 判断资产是否足够
        if (balance < amount) {
            log.error("PlayerId = " + playerId + " HandIndex = " + handIndex + " Amount = " + amount + " onPlayerBet");
            sendToPlayer(playerId, GameEvent.ERROR_REQUEST, new ActionParam(playerId, null, null, null));
            return;
        }

        log.info("PlayerId = " + playerId + " HandIndex = " + handIndex + " Amount = " + amount + " onPlayerBet");
        player.setBalance(player.getBalance() - amount);
        gameContext.getPlayers().get(playerId).getHands().get(handIndex).setBet(amount);

        broadcast(GameEvent.BET, new ActionParam(playerId, handIndex, null, amount));
    }

    @Override
    public void onPlayerSurrender(int playerId, int handIndex) {
        AbstractPlayer player = gameContext.getPlayers().get(playerId);
        Hand hand = player.getHands().get(handIndex);
        if (hand.isStart()) {
            log.error("PlayerId = " + playerId + " HandIndex = " + handIndex + " onPlayerSurrender");
            sendToPlayer(playerId, GameEvent.ERROR_REQUEST, new ActionParam(playerId, null, null, null));
            return;
        }

        log.info("PlayerId = " + playerId + " HandIndex = " + handIndex + " onPlayerSurrender");
        hand.setSurrender(true);
        hand.setStart(true);
        broadcast(GameEvent.SURRENDER, new ActionParam(playerId, handIndex, null, null));
        standOrBustHandler(gameContext.getPlayers().get(playerId).getHands(), playerId, handIndex);
    }

    /**
     * 用于处理玩家停牌，投降，21点或爆牌的处理器
     *
     * @param hands     玩家的手牌列表
     * @param playerId  玩家Id
     * @param handIndex 手牌索引
     */
    private void standOrBustHandler(List<Hand> hands, int playerId, int handIndex) {
        // 判断该玩家是否还有分牌
        for (int i = 0; i < hands.size(); i++) {
            if (i == handIndex) {
                continue;
            }
            if (hands.get(i).isStood() || hands.get(i).isBusted() || GameUtil.near221(hands.get(i)) == 21) {
                continue;
            }
            // 玩家存在分牌
            timer = 0;
            broadcast(GameEvent.NEW_HAND, new ActionParam(playerId, null, null, null));
            return;
        }
        // 否则说明不存在分牌，玩家该轮结束
        broadcast(GameEvent.TURN_END, new ActionParam(playerId, null, null, null));
        notifyAndGetNextPlayer();
    }

    /**
     * 庄家在所有人结束后反复摸牌直到大于等于17点
     */
    private void dealerDeal() {
        Hand hand = gameContext.getPlayers().get(-1).getHands().get(0);
        Map<Integer, Hand> info = new HashMap<>();
        Hand infoHand = new Hand();
        // 未翻开的牌面应该被翻转
        infoHand.getCards().add(gameContext.getPlayers().get(-1).getHands().get(0).getCards().get(0));
        while (GameUtil.near221(hand) < 17) {
            Card card = deal();
            hand.getCards().add(card);
            infoHand.getCards().add(card);
        }
        info.put(-1, infoHand);
        broadcast(GameEvent.DEALER_DEAL, new PlayerHandsInfo(info));
    }

    /**
     * 初始化服务端计时器
     */
    private void initTimer() {
        new Thread(() -> {
            TimerTask timerTask = new TimerTask() {
                @Override
                public void run() {
                    if (currentPlayer == null) {
                        cancel();
                        return;
                    }
                    if (timerStart) {
                        timer++;
                    }
                    if (timer > lsto) {
                        List<Hand> hands = currentPlayer.getHands();
                        for (Hand hand : hands) {
                            hand.setStood(true);
                        }
                        notifyAndGetNextPlayer();
                    }
                }
            };
            Timer timer = new Timer();
            timer.schedule(timerTask, 1000, 1000);
        }).start();
    }

    /**
     * 重置计时器
     *
     * @param playerId 玩家Id
     * @param isClose  计时器是否关闭
     */
    private void resetTimer(int playerId, boolean isClose) {
        if (playerId != currentPlayer.getId())
            return;
        timer = 0;
        timerStart = !isClose;
    }
}
