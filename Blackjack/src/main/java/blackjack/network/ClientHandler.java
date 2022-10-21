package blackjack.network;

import blackjack.adapter.RemoteGameAdapter;
import blackjack.entity.RemoteParam;
import blackjack.game.GameClient;
import blackjack.ui.MainController;
import com.google.gson.Gson;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import javafx.application.Platform;

/**
 * 基础客户端入站处理器
 */
public class ClientHandler extends ChannelInboundHandlerAdapter {

    RemoteGameAdapter adapter;

    public ClientHandler(GameClient gameClient) {
        adapter = (RemoteGameAdapter) gameClient.getCommunicationAdapter();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        String json = msg.toString();
        if (json.equals("pong"))
            return;
        RemoteParam remoteParam = new Gson().fromJson(json, RemoteParam.class);
        this.adapter.receiveEvent(remoteParam.getGameEvent(), remoteParam.getParamJson());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        Platform.runLater(() -> MainController.showErrorDialog("连接中断"));
    }
}
