package github.kevssim.registry.zk;

import github.kevssim.registry.ServiceRegistry;
import github.kevssim.registry.zk.utils.CuratorUtils;
import org.apache.curator.framework.CuratorFramework;

import java.net.InetSocketAddress;

/**
 * 服务注册
 */
public class ZkServiceRegistry implements ServiceRegistry {
    @Override
    public void registerService(String rpcServiceName, InetSocketAddress inetSocketAddress) {
        // eg. /my-rpc/github.kevssim.HelloService/127.0.0.1:9999
        String servicePath = CuratorUtils.ZK_REGISTER_ROOT_PATH + "/" + rpcServiceName + inetSocketAddress.toString();
        CuratorFramework zkClient = CuratorUtils.getZkClient();
        //创建节点
        CuratorUtils.createNode(zkClient, servicePath);
    }
}
