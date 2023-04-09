package github.kevssim.transport.Client;

import github.kevssim.protocol.MessageDecoder;
import github.kevssim.protocol.MessageEncoder;
import github.kevssim.protocol.message.RpcRequest;
import github.kevssim.protocol.message.RpcResponse;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.ws.Response;
import java.net.InetSocketAddress;
import java.util.concurrent.*;

public class ShareClient {
    private static final Logger logger = LoggerFactory.getLogger(RpcClient.class);

    private RpcResponse response;

    private Channel channel;

    private boolean isInit;

    private ShareClientHandler shareClientHandler;

    public boolean isInited() {
        return isInit;
    }


    public void init(InetSocketAddress address) {
        NioEventLoopGroup group = new NioEventLoopGroup();
        shareClientHandler = new ShareClientHandler();
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .handler(new ChannelInitializer<NioSocketChannel>() {
                        @Override
                        protected void initChannel(NioSocketChannel channel) throws Exception {
                            ChannelPipeline pipeline = channel.pipeline();
                            pipeline.addLast(new IdleStateHandler(10, 0, 0, TimeUnit.SECONDS));
                            pipeline.addLast(new LengthFieldBasedFrameDecoder(1024, 12, 4, 0, 0));
                            pipeline.addLast(new MessageDecoder());
                            pipeline.addLast(new MessageEncoder());
                            pipeline.addLast(shareClientHandler);
                        }
                    });
            ChannelFuture future = bootstrap.connect(address).sync();
            this.channel = future.channel();
            isInit = true;
            ChannelFuture channelFuture = this.channel.closeFuture();
            //异步关闭连接
            channelFuture.addListener((ChannelFutureListener) future1 -> group.shutdownGracefully());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public RpcResponse send(RpcRequest request) throws InterruptedException, ExecutionException {
        CompletableFuture<RpcResponse> completableFuture = shareClientHandler.send(request);
        return completableFuture.get();
    }


    public void close() {
        channel.close();
    }
}
