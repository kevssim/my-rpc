package github.kevssim.transport.Server;


import github.kevssim.protocol.MessageDecoder;
import github.kevssim.protocol.MessageEncoder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.util.concurrent.DefaultEventExecutorGroup;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

public class RpcServer {
    private static Channel sc;

    private Map<String, Object> handlerMap = new HashMap<>();

    public void addImpl(String serviceNameAndVersion, Object impl) {
        handlerMap.put(serviceNameAndVersion, impl);
    }

    public void startServer(InetSocketAddress serviceAddress) {
        startServer0(serviceAddress);
    }

    //NettyServer的初始化和启动
    private void startServer0(InetSocketAddress serviceAddress) {
        NioEventLoopGroup bossGroup = new NioEventLoopGroup();
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();
        //使用handlerGroup去执行方法调用，避免方法调用执行时间过长影响网络IO性能
        DefaultEventExecutorGroup handlerGroup = new DefaultEventExecutorGroup(1);

        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap
                    .group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {

                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline pipeline = ch.pipeline();
                            pipeline.addLast(new LengthFieldBasedFrameDecoder(1024, 12, 4, 0, 0));
                            pipeline.addLast(new MessageDecoder());
                            pipeline.addLast(new MessageEncoder());
                            //业务处理器
                            pipeline.addLast(handlerGroup, new RpcServerHandler(handlerMap));
                        }
                    });
            ChannelFuture channelFuture = serverBootstrap.bind(serviceAddress).sync();
            System.out.println("服务提供方开始提供服务");
            //处理断开连接
            sc = channelFuture.channel();
            sc.closeFuture().sync();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }
}
