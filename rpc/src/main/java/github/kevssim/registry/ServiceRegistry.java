package github.kevssim.registry;

import java.net.InetSocketAddress;

//服务注册
public interface ServiceRegistry {
    void registerService(String rpcServiceName, InetSocketAddress inetSocketAddress);
}
