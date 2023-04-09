package github.kevssim.provider;

import github.kevssim.registry.ServiceRegistry;
import github.kevssim.registry.zk.ZkServiceRegistry;
import github.kevssim.service.HelloServiceImpl;
import github.kevssim.transport.Server.RpcServer;

import java.net.InetSocketAddress;

//启动一个服务提供者,即NettyServer
public class ServerBootstrap1 {
    private static final ServiceRegistry serviceRegistry = new ZkServiceRegistry();

    public static void main(String[] args) {
        //注册服务到注册中心
        InetSocketAddress serviceAddress = new InetSocketAddress("127.0.0.1", 7001);
        ServerBootstrap1.serviceRegistry.registerService("github.kevssim.service.HelloService", serviceAddress);
        //创建netty服务端
        RpcServer server = new RpcServer();
        server.addImpl("github.kevssim.service.HelloService-1.0", new HelloServiceImpl());
        server.startServer(serviceAddress);
    }
}
