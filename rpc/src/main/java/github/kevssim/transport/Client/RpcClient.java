package github.kevssim.transport.Client;

import github.kevssim.protocol.message.RpcRequest;
import github.kevssim.protocol.message.RpcResponse;
import github.kevssim.protocol.MessageDecoder;
import github.kevssim.protocol.MessageEncoder;
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

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;


public class RpcClient extends SimpleChannelInboundHandler<RpcResponse> {
    private static final Logger logger = LoggerFactory.getLogger(RpcClient.class);

    private final InetSocketAddress address;

    private int timeout = 0;

    private RpcResponse response;

    private Exception timeoutException;

    public RpcClient(InetSocketAddress address, int timeout) {
        this.address = address;
        this.timeout = timeout;
    }

    public RpcResponse send(RpcRequest request) throws Exception {
        NioEventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .handler(new ChannelInitializer<NioSocketChannel>() {
                        @Override
                        protected void initChannel(NioSocketChannel channel) throws Exception {
                            ChannelPipeline pipeline = channel.pipeline();
                            pipeline.addLast(new IdleStateHandler(timeout, 0, 0, TimeUnit.SECONDS));
                            pipeline.addLast(new LengthFieldBasedFrameDecoder(1024, 12, 4, 0, 0));
                            pipeline.addLast(new MessageDecoder());
                            pipeline.addLast(new MessageEncoder());
                            pipeline.addLast(RpcClient.this);
                        }
                    });
            ChannelFuture future = bootstrap.connect(address).sync();
            Channel channel = future.channel();
            channel.writeAndFlush(request).sync();
            channel.closeFuture().sync();
            if (timeoutException != null) {
                throw timeoutException;
            }
            return response;
        } finally {
            group.shutdownGracefully();
        }
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcResponse rpcResponse) throws Exception {
        this.response = rpcResponse;
        ctx.channel().close();
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
            if (event.state() == IdleState.READER_IDLE) {
                //读空闲
                throw new Exception("请求服务超时");
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        timeoutException = new TimeoutException("请求超时");
        ctx.channel().close();
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }
}
