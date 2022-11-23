package blackjack.adapter;

import blackjack.entity.ActionParam;
import blackjack.entity.PlayerHandsInfo;
import blackjack.entity.RemoteParam;
import blackjack.enums.GameEvent;
import blackjack.game.GameClient;
import blackjack.game.GameEventAware;
import blackjack.player.Player;
import blackjack.ui.MainController;
import com.google.gson.Gson;
import io.netty.channel.Channel;
import lombok.Getter;
import lombok.Setter;

/**
 * 远程C/S通信适配器，通过channel进行通信
 */
@Setter
@Getter
public class RemoteGameAdapter implements CommunicationAdapter {

    private GameEventAware self;

    private Channel channel;

    @Override
    public void sendEvent(GameEvent event, Object data) {
        if (!channel.isActive()) {
            if (self instanceof GameClient)
                MainController.showErrorDialog("连接中断");
            return;
        }
        String json = new Gson().toJson(data);
        RemoteParam remoteParam = new RemoteParam(event, json);
        try {
            channel.writeAndFlush(new Gson().toJson(remoteParam) + "\n").sync();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void receiveEvent(GameEvent event, String json) {
        Player player;
        ActionParam param;
        PlayerHandsInfo info;
        boolean next = self.beforeEvent(event, json);

        if (next) {
            switch (event) {
                case PLAYER_JOIN:
                    player = new Gson().fromJson(json, Player.class);
                    self.onPlayerJoin(player);
                    break;
                case PLAYER_LEAVE:
                    player = new Gson().fromJson(json, Player.class);
                    self.onPlayerLeave(player);
                    break;
                case PLAYER_RESET:
                    player = new Gson().fromJson(json, Player.class);
                    self.onPlayerReset(player);
                    break;
                case GAME_RESULT:
                    param = new Gson().fromJson(json, ActionParam.class);
                    self.onGameResult(param.getPlayerId(), param.getHandIndex(), param.getBet());
                    break;

                // 回合开始和结束只有服务端会发送
                case TURN_START:
                    param = new Gson().fromJson(json, ActionParam.class);
                    self.onTurnStart(param.getPlayerId());
                    break;
                case TURN_END:
                    param = new Gson().fromJson(json, ActionParam.class);
                    self.onTurnEnd(param.getPlayerId());
                    break;

                case NEW_HAND:
                    param = new Gson().fromJson(json, ActionParam.class);
                    self.onNewHand(param.getPlayerId());
                    break;
                case INIT_CARD:
                    info = new Gson().fromJson(json, PlayerHandsInfo.class);
                    self.initEvent(info.getPlayerHandsInfo());
                    break;
                case LIST_CARD:
                    player = new Gson().fromJson(json, Player.class);
                    self.onPlayerList(player);
                    break;
                case ERROR_REQUEST:
                    param = new Gson().fromJson(json, ActionParam.class);
                    self.onError(param.getPlayerId());
                    break;
                case DEALER_DEAL:
                    info = new Gson().fromJson(json, PlayerHandsInfo.class);
                    self.onDealerDeal(info.getPlayerHandsInfo());
                    break;
                case BET:
                    param = new Gson().fromJson(json, ActionParam.class);
                    self.onPlayerBet(param.getPlayerId(), param.getHandIndex(), param.getBet());
                    break;
                case HIT:
                    param = new Gson().fromJson(json, ActionParam.class);
                    self.onPlayerHit(param.getPlayerId(), param.getHandIndex(), param.getCard());
                    break;
                case SPLIT:
                    param = new Gson().fromJson(json, ActionParam.class);
                    self.onPlayerSplit(param.getPlayerId(), param.getHandIndex());
                    break;
                case STAND:
                    param = new Gson().fromJson(json, ActionParam.class);
                    self.onPlayerStand(param.getPlayerId(), param.getHandIndex());
                    break;
                case DOUBLE:
                    param = new Gson().fromJson(json, ActionParam.class);
                    self.onPlayerDouble(param.getPlayerId(), param.getHandIndex());
                    break;
                case INSURANCE:
                    param = new Gson().fromJson(json, ActionParam.class);
                    self.onPlayerInsurance(param.getPlayerId(), param.getHandIndex());
                    break;
                case SURRENDER:
                    param = new Gson().fromJson(json, ActionParam.class);
                    self.onPlayerSurrender(param.getPlayerId(), param.getHandIndex());
                    break;
                case SENDID:
                    int id = Integer.parseInt(json);
                    self.onSendId(id);
                    break;
                default:
                    break;
            }
        }
        self.afterEvent(event, json);
    }
}
