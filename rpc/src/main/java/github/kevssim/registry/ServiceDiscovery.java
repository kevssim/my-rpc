package github.kevssim.registry;

import java.net.InetSocketAddress;

//服务发现
public interface ServiceDiscovery {

    InetSocketAddress lookupService(String rpcServiceName) throws Exception;
}
