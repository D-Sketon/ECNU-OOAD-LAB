package blackjack.adapter;

import blackjack.entity.ActionParam;
import blackjack.entity.PlayerHandsInfo;
import blackjack.enums.GameEvent;
import blackjack.game.GameContext;
import blackjack.game.GameEventAware;
import blackjack.player.Player;

/**
 * 本地C/S通信适配器，通过直接调用C/S的方法完成通信
 */
public class LocalGameAdapter implements CommunicationAdapter {

    private final GameEventAware target;

    public LocalGameAdapter(GameEventAware target) {
        this.target = target;
    }

    @Override
    public void sendEvent(GameEvent event, Object data) {
        ActionParam param = null;
        PlayerHandsInfo info = null;
        Player cloned = null;
        if (data instanceof ActionParam) {
            param = (ActionParam) data;
        } else if (data instanceof PlayerHandsInfo) {
            info = (PlayerHandsInfo) data;
        } else if (data instanceof Player) {
            try {
                cloned = (Player) ((Player) data).clone();
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }
        }

        boolean next = target.beforeEvent(event, data);

        if (next) {
            switch (event) {
                case PLAYER_JOIN:
                    target.onPlayerJoin(data instanceof Player ? cloned : null);
                    break;
                case PLAYER_LEAVE:
                    target.onPlayerLeave(data instanceof Player ? cloned : null);
                    break;
                case PLAYER_RESET:
                    target.onPlayerReset(data instanceof Player ? cloned : null);
                    break;
                case GAME_RESULT:
                    target.onGameResult(param.getPlayerId(), param.getHandIndex(), param.getBet());
                    break;

                case TURN_START:
                    target.onTurnStart(param.getPlayerId());
                    break;
                case TURN_END:
                    target.onTurnEnd(param.getPlayerId());
                    break;

                case NEW_HAND:
                    target.onNewHand(param.getPlayerId());
                    break;
                case INIT_CARD:
                    target.initEvent(info.getPlayerHandsInfo());
                    break;
                case LIST_CARD:
                    target.onPlayerList(data instanceof Player ? cloned : null);
                    break;
                case ERROR_REQUEST:
                    target.onError(param.getPlayerId());
                    break;
                case DEALER_DEAL:
                    target.onDealerDeal(info.getPlayerHandsInfo());
                    break;

                case BET:
                    target.onPlayerBet(param.getPlayerId(), param.getHandIndex(), param.getBet());
                    break;
                case HIT:
                    target.onPlayerHit(param.getPlayerId(), param.getHandIndex(), param.getCard());
                    break;
                case SPLIT:
                    target.onPlayerSplit(param.getPlayerId(), param.getHandIndex());
                    break;
                case STAND:
                    target.onPlayerStand(param.getPlayerId(), param.getHandIndex());
                    break;
                case DOUBLE:
                    target.onPlayerDouble(param.getPlayerId(), param.getHandIndex());
                    break;
                case INSURANCE:
                    target.onPlayerInsurance(param.getPlayerId(), param.getHandIndex());
                    break;
                case SURRENDER:
                    target.onPlayerSurrender(param.getPlayerId(), param.getHandIndex());
                    break;
                default:
                    break;
            }
        }
        target.afterEvent(event, data);
    }

}
