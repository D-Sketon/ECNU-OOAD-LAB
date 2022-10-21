package blackjack.network;

import blackjack.adapter.RemoteGameAdapter;
import blackjack.game.GameClient;
import blackjack.ui.ConnectController;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import javafx.application.Platform;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 使用netty实现的网络通信客户端
 */
@Slf4j
public class ClientOnline {
    @Getter
    private EventLoopGroup eventExecutors;

    public void initNetty(GameClient gameClient, String ip) {
        eventExecutors = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(eventExecutors) //设置一个线程组
                    .channel(NioSocketChannel.class) //使用NioServerSocketChannel作为服务器通道的实现
                    .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000) //连接超时时间，超过5秒没连上则失败
                    .handler(new ChannelInitializer<SocketChannel>() { //给workGroup的EventLoop对应的管道设置处理器
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            ByteBuf delimiter = Unpooled.copiedBuffer("\n".getBytes());
                            ch.pipeline().addLast(new DelimiterBasedFrameDecoder(8192, delimiter));
                            ch.pipeline().addLast(new StringDecoder()); //添加处理类
                            ch.pipeline().addLast(new StringEncoder()); //添加处理类
                            ch.pipeline().addLast(new IdleStateHandler(5, 5, 5, TimeUnit.SECONDS));
                            ch.pipeline().addLast(new HeartBeatClientHandler());
                            ch.pipeline().addLast(new ClientHandler(gameClient)); //添加处理类
                        }
                    });
            log.info("...Client is ready...");

            //启动客户端连接服务端
            ChannelFuture channelFuture = bootstrap.connect(ip, 6668).sync();
            RemoteGameAdapter communicationAdapter = (RemoteGameAdapter) gameClient.getCommunicationAdapter();
            communicationAdapter.setChannel(channelFuture.channel());
            new Thread(() -> {
                while (channelFuture.channel().isActive()) {
                    String text = "ping\n";
                    try {
                        Thread.sleep(3000);
                        channelFuture.channel().writeAndFlush(text).sync();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
            //给关闭通道进行监听
            channelFuture.channel().closeFuture().sync();
        } catch (Exception e) {
            log.error("获取Channel时有错误发生", e);
            e.printStackTrace();
            //回调函数
            Platform.runLater(() -> ConnectController.setErrorMsg(e.getMessage()));
        } finally {
            eventExecutors.shutdownGracefully();
        }
    }

    static class HeartBeatClientHandler extends SimpleChannelInboundHandler<String> {

        private final AtomicInteger readIdleTimes = new AtomicInteger(0);

        @Override
        public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
            log.info("client idleTimes = " + readIdleTimes );
            IdleStateEvent event;
            if (evt instanceof IdleStateEvent)
                event = (IdleStateEvent) evt;
            else
                return;

            switch (event.state()) {
                case READER_IDLE:
                    readIdleTimes.incrementAndGet(); // 读空闲的计数加1
                    break;
                case WRITER_IDLE:
                case ALL_IDLE:
                    // 不处理
                    break;
            }
            if (readIdleTimes.get() > 3) {
                ctx.channel().close();
            }
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            if ("pong".equals(msg)) {
                this.channelRead0(ctx, (String) msg);
            } else {
                ctx.fireChannelRead(msg);
            }
        }

        @Override
        protected void channelRead0(ChannelHandlerContext channelHandlerContext, String s) throws Exception {

        }
    }
}



